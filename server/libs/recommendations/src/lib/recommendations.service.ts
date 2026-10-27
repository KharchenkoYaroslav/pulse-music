import { Injectable } from '@nestjs/common';
import { InjectRedis } from '@nestjs-modules/ioredis';
import Redis from 'ioredis';
import { SongRankingItem } from '@simple-music/interfaces';
import { SongService } from '@simple-music/song-management';
import { UserService } from '@simple-music/user';

@Injectable()
export class RecommendationsService {
  constructor(
    @InjectRedis() private readonly redis: Redis,
    private readonly songService: SongService,
    private readonly userService: UserService,
  ) {}

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

  async getSongsCountByLikes(categoryId?: string, authorId?: string, search?: string): Promise<number> {
    let rankingKey = `songs:likes_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:likes_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:likes_ranking`;
    }

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

  async getSongsCountByViews(categoryId?: string, authorId?: string, search?: string): Promise<number> {
    let rankingKey = `songs:views_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:views_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:views_ranking`;
    }

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

  async getSongsCountByNewest(categoryId?: string, authorId?: string, search?: string): Promise<number> {
    let rankingKey = `songs:newest_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:newest_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:newest_ranking`;
    }

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

  async getSongsSortedByLikes(
    from: number,
    to: number,
    categoryId?: string,
    authorId?: string,
    search?: string,
  ): Promise<SongRankingItem[]> {
    let rankingKey = `songs:likes_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:likes_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:likes_ranking`;
    }

    let songIds: string[];
    if (search) {
      const searchKey = await this.getSearchCacheKey(search);
      const tempKey = `temp_inter:${Date.now()}:${Math.random()}`;
      await this.redis.zinterstore(tempKey, 2, rankingKey, searchKey, 'WEIGHTS', 1, 0);
      songIds = await this.redis.zrevrange(tempKey, from, to);
      await this.redis.del(tempKey);
    } else {
      songIds = await this.redis.zrevrange(rankingKey, from, to);
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

  async getSongsSortedByViews(
    from: number,
    to: number,
    categoryId?: string,
    authorId?: string,
    search?: string,
  ): Promise<SongRankingItem[]> {
    let rankingKey = `songs:views_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:views_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:views_ranking`;
    }

    let songIds: string[];
    if (search) {
      const searchKey = await this.getSearchCacheKey(search);
      const tempKey = `temp_inter:${Date.now()}:${Math.random()}`;
      await this.redis.zinterstore(tempKey, 2, rankingKey, searchKey, 'WEIGHTS', 1, 0);
      songIds = await this.redis.zrevrange(tempKey, from, to);
      await this.redis.del(tempKey);
    } else {
      songIds = await this.redis.zrevrange(rankingKey, from, to);
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

  async getSongsSortedByNewest(
    from: number,
    to: number,
    categoryId?: string,
    authorId?: string,
    search?: string,
  ): Promise<SongRankingItem[]> {
    let rankingKey = `songs:newest_ranking`;
    if (authorId) {
      rankingKey = `author:${authorId}:newest_ranking`;
    } else if (categoryId) {
      rankingKey = `category:${categoryId}:newest_ranking`;
    }

    let songIds: string[];
    if (search) {
      const searchKey = await this.getSearchCacheKey(search);
      const tempKey = `temp_inter:${Date.now()}:${Math.random()}`;
      await this.redis.zinterstore(tempKey, 2, rankingKey, searchKey, 'WEIGHTS', 1, 0);
      songIds = await this.redis.zrevrange(tempKey, from, to);
      await this.redis.del(tempKey);
    } else {
      songIds = await this.redis.zrevrange(rankingKey, from, to);
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
