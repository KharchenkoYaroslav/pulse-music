import { Injectable } from '@nestjs/common';
import { InjectRedis } from '@nestjs-modules/ioredis';
import Redis from 'ioredis';
import { SongRankingItem, SongHistoryItem } from '@simple-music/interfaces';
import { SongService } from '@simple-music/song-management';
import { OnEvent } from '@nestjs/event-emitter';

/**
 * Схема даних Redis:
 *
 * --- Рейтинги (Sorted Sets / ZSET) ---
 * - `songs:likes_ranking` : Загальний рейтинг всіх пісень за лайками (score = кількість лайків).
 * - `songs:views_ranking` : Загальний рейтинг всіх пісень за переглядами (score = кількість переглядів).
 * - `author:${authorId}:likes_ranking` : Рейтинг пісень конкретного автора за лайками.
 * - `author:${authorId}:views_ranking` : Рейтинг пісень конкретного автора за переглядами.
 * - `category:${categoryId}:likes_ranking` : Рейтинг пісень у конкретній категорії за лайками.
 * - `category:${categoryId}:views_ranking` : Рейтинг пісень у конкретній категорії за переглядами.
 *
 * --- Інформація про пісню ---
 * - `song:${songId}:info` [Hash] : Зберігає метадані пісні (authorId, categoryIds у форматі JSON) для швидкого оновлення відповідних рейтингів.
 * - `song:${songId}:likes` [Set] : Множина ID користувачів, які лайкнули пісню (дозволяє рахувати кількість лайків та перевіряти, чи ставив юзер лайк).
 *
 * --- Дані користувача ---
 * - `user:${userId}:likes` [ZSET] : Список пісень, які вподобав користувач (score = timestamp, member = songId, для сортування від нових до старих).
 * - `user:${userId}:history` [ZSET] : Історія переглядів пісень користувачем (score = timestamp, member = `${songId}:${uuid}`).
 *
 * --- Інше ---
 * - `view:cooldown:${userId}:${songId}` [String] : Тимчасовий ключ з TTL (300 сек) для запобігання накрутці переглядів (cooldown).
 */

import { UserService } from './user.service';

@Injectable()
export class UserMetricsService {
  constructor(
    @InjectRedis() private readonly redis: Redis,
    private readonly songService: SongService,
    private readonly userService: UserService,
  ) {}

  @OnEvent('user.deleted')
  async handleUserDeleted(payload: { userId: string }): Promise<void> {
    await this.removeAllUserLikes(payload.userId);
    await this.removeAllUserViews(payload.userId);
  }

  private async getSearchCacheKey(search: string): Promise<string> {
    const cacheKey = `search_cache:${search.toLowerCase()}`;
    if (await this.redis.exists(cacheKey)) {
      return cacheKey;
    }

    const matchedAuthorIds = await this.userService.findUserIdsByLoginMatch(search);
    const matchedSongIds = await this.songService.findSongIdsBySearch(search, matchedAuthorIds);

    const pipeline = this.redis.pipeline();
    if (matchedSongIds.length > 0) {
      pipeline.sadd(cacheKey, ...matchedSongIds);
    } else {
      pipeline.sadd(cacheKey, 'NONE');
    }
    pipeline.expire(cacheKey, 600);
    await pipeline.exec();

    return cacheKey;
  }

  async removeAllUserLikes(userId: string): Promise<void> {
    const userLikes = await this.redis.zrange(`user:${userId}:likes`, '0', '-1');
    if (userLikes.length === 0) return;

    const infoPipeline = this.redis.pipeline();
    for (const songId of userLikes) {
      infoPipeline.hgetall(`song:${songId}:info`);
    }
    const infos = await infoPipeline.exec();

    const deletePipeline = this.redis.pipeline();
    deletePipeline.del(`user:${userId}:likes`);

    userLikes.forEach((songId, index) => {
      deletePipeline.srem(`song:${songId}:likes`, userId);
      deletePipeline.zincrby(`songs:likes_ranking`, -1, songId);

      const errAndResult = infos?.[index];
      const infoResult = errAndResult ? (errAndResult[1] as Record<string, string>) : null;

      if (infoResult) {
        if (infoResult['authorId']) {
          deletePipeline.zincrby(`author:${infoResult['authorId']}:likes_ranking`, -1, songId);
        }
        if (infoResult['categoryIds']) {
          try {
            const categoryIds: string[] = JSON.parse(infoResult['categoryIds']);
            categoryIds.forEach((catId) => {
              deletePipeline.zincrby(`category:${catId}:likes_ranking`, -1, songId);
            });
          } catch {
            // ignore
          }
        }
      }
    });

    await deletePipeline.exec();
  }

  async removeAllUserViews(userId: string): Promise<void> {
    await this.redis.del(`user:${userId}:history`);
  }

  async getUserViewHistoryCount(userId: string, search?: string): Promise<number> {
    if (search) {
      const allIds = await this.redis.zrevrange(`user:${userId}:history`, 0, -1);
      const cacheKey = await this.getSearchCacheKey(search);
      const matchedSongIds = await this.redis.smembers(cacheKey);
      const matchedSet = new Set(matchedSongIds);
      return allIds.map(entry => entry.split(':')[0]).filter(id => matchedSet.has(id)).length;
    }
    return this.redis.zcard(`user:${userId}:history`);
  }

  async getUserLikedSongsCount(userId: string, search?: string): Promise<number> {
    const rankingKey = `user:${userId}:likes`;
    if (search) {
      const searchKey = await this.getSearchCacheKey(search);
      const tempKey = `temp_inter:${Date.now()}:${Math.random()}`;
      await this.redis.zinterstore(tempKey, 2, rankingKey, searchKey, 'WEIGHTS', 1, 0);
      const count = await this.redis.zcard(tempKey);
      await this.redis.del(tempKey);
      return count;
    }
    return this.redis.zcard(rankingKey);
  }

  async getUserViewHistory(
    userId: string,
    from: number,
    to: number,
    search?: string,
  ): Promise<SongHistoryItem[]> {
    const historyKey = `user:${userId}:history`;

    let redisResult: string[];
    if (search) {
      const allEntries = await this.redis.zrevrange(historyKey, 0, -1, 'WITHSCORES');
      const cacheKey = await this.getSearchCacheKey(search);
      const matchedSongIds = await this.redis.smembers(cacheKey);
      const matchedSet = new Set(matchedSongIds);
      
      const filteredEntries: string[] = [];
      for (let i = 0; i < allEntries.length; i += 2) {
        const entry = allEntries[i];
        const songId = entry.split(':')[0];
        if (matchedSet.has(songId)) {
          filteredEntries.push(allEntries[i], allEntries[i + 1]);
        }
      }
      redisResult = filteredEntries.slice(from * 2, (to + 1) * 2);
    } else {
      redisResult = await this.redis.zrevrange(historyKey, from, to, 'WITHSCORES');
    }

    if (redisResult.length === 0) return [];

    const historyItems: { songId: string; viewedAt: number }[] = [];
    for (let i = 0; i < redisResult.length; i += 2) {
      const entry = redisResult[i];
      const songId = entry.split(':')[0];
      historyItems.push({
        songId,
        viewedAt: parseInt(redisResult[i + 1], 10),
      });
    }

    const songIds = Array.from(new Set(historyItems.map((item) => item.songId)));

    const songsMap = await this.songService.fetchSongDocsAndMap(songIds);

    return historyItems
      .map((item) => {
        const songDoc = songsMap.get(item.songId);
        if (!songDoc) return null;

        return {
          id: item.songId,
          authorId: songDoc.authorId.toString(),
          authorLogin: '',
          originalAuthor: songDoc.originalAuthor,
          title: songDoc.title || '',
          cover: songDoc.cover || null,
          viewedAt: item.viewedAt,
        } as SongHistoryItem;
      })
      .filter((item): item is SongHistoryItem => item !== null);
  }

  async getUserLikedSongs(
    userId: string,
    from: number,
    to: number,
    search?: string,
  ): Promise<SongRankingItem[]> {
    let songIds: string[];
    if (search) {
      const searchKey = await this.getSearchCacheKey(search);
      const tempKey = `temp_inter:${Date.now()}:${Math.random()}`;
      await this.redis.zinterstore(tempKey, 2, `user:${userId}:likes`, searchKey, 'WEIGHTS', 1, 0);
      songIds = await this.redis.zrevrange(tempKey, from, to);
      await this.redis.del(tempKey);
    } else {
      songIds = await this.redis.zrevrange(`user:${userId}:likes`, from, to);
    }
    if (songIds.length === 0) return [];

    const songsMap = await this.songService.fetchSongDocsAndMap(songIds);

    return songIds
      .map((songId) => {
        const songDoc = songsMap.get(songId);
        if (!songDoc) return null;

        return {
          id: songId,
          authorId: songDoc.authorId.toString(),
          authorLogin: '',
          originalAuthor: songDoc.originalAuthor,
          title: songDoc.title || '',
          cover: songDoc.cover || null,
        } as SongRankingItem;
      })
      .filter((item): item is SongRankingItem => item !== null);
  }
}

