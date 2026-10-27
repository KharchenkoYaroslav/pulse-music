import { Injectable } from '@nestjs/common';
import { InjectRedis } from '@nestjs-modules/ioredis';
import Redis from 'ioredis';
import { randomUUID } from 'crypto';

/**
 * Схема даних Redis:
 *
 * --- Рейтинги (Sorted Sets / ZSET) ---
 * - `songs:likes_ranking` : Загальний рейтинг всіх пісень за лайками (score = кількість лайків).
 * - `songs:views_ranking` : Загальний рейтинг всіх пісень за переглядами (score = кількість переглядів).
 * - `songs:newest_ranking` : Загальний рейтинг всіх пісень за датою додавання (score = createdAt timestamp ms).
 * - `author:${authorId}:likes_ranking` : Рейтинг пісень конкретного автора за лайками.
 * - `author:${authorId}:views_ranking` : Рейтинг пісень конкретного автора за переглядами.
 * - `author:${authorId}:newest_ranking` : Рейтинг пісень конкретного автора за датою додавання.
 * - `category:${categoryId}:likes_ranking` : Рейтинг пісень у конкретній категорії за лайками.
 * - `category:${categoryId}:views_ranking` : Рейтинг пісень у конкретній категорії за переглядами.
 * - `category:${categoryId}:newest_ranking` : Рейтинг пісень у конкретній категорії за датою додавання.
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

@Injectable()
export class SongMetricsService {
  constructor(
    @InjectRedis() private readonly redis: Redis,
  ) {}

  async initSongMetrics(
    songId: string,
    authorId: string,
    categoryIds: string[] = [],
    createdAt?: number,
  ): Promise<void> {
    const timestamp = createdAt ?? Date.now();
    const pipeline = this.redis.pipeline();

    pipeline.zadd(`songs:likes_ranking`, 0, songId);
    pipeline.zadd(`author:${authorId}:likes_ranking`, 0, songId);
    pipeline.zadd(`songs:views_ranking`, 0, songId);
    pipeline.zadd(`author:${authorId}:views_ranking`, 0, songId);
    pipeline.zadd(`songs:newest_ranking`, timestamp, songId);
    pipeline.zadd(`author:${authorId}:newest_ranking`, timestamp, songId);

    categoryIds.forEach((catId) => {
      pipeline.zadd(`category:${catId}:likes_ranking`, 0, songId);
      pipeline.zadd(`category:${catId}:views_ranking`, 0, songId);
      pipeline.zadd(`category:${catId}:newest_ranking`, timestamp, songId);
    });

    pipeline.hmset(`song:${songId}:info`, {
      authorId: authorId,
      categoryIds: JSON.stringify(categoryIds),
    });

    await pipeline.exec();
  }

  async cleanupSongMetrics(songId: string, authorId: string, categoryIds: string[]): Promise<void> {
    const pipeline = this.redis.pipeline();

    pipeline.zrem(`songs:likes_ranking`, songId);
    pipeline.zrem(`author:${authorId}:likes_ranking`, songId);
    pipeline.zrem(`songs:views_ranking`, songId);
    pipeline.zrem(`author:${authorId}:views_ranking`, songId);
    pipeline.zrem(`songs:newest_ranking`, songId);
    pipeline.zrem(`author:${authorId}:newest_ranking`, songId);

    categoryIds.forEach((catId) => {
      pipeline.zrem(`category:${catId}:likes_ranking`, songId);
      pipeline.zrem(`category:${catId}:views_ranking`, songId);
      pipeline.zrem(`category:${catId}:newest_ranking`, songId);
    });

    pipeline.del(`song:${songId}:info`);
    pipeline.del(`song:${songId}:likes`);

    await pipeline.exec();
  }

  async updateCategoryRankings(
    songId: string,
    newCategoryIds: string[],
    oldCategoryIds: string[],
  ): Promise<void> {
    if (
      newCategoryIds.length === oldCategoryIds.length &&
      newCategoryIds.every((id) => oldCategoryIds.includes(id))
    ) {
      return;
    }

    const pipeline = this.redis.pipeline();

    pipeline.hset(
      `song:${songId}:info`,
      'categoryIds',
      JSON.stringify(newCategoryIds),
    );

    const [likesCount, viewsCount] = await Promise.all([
      this.getLikesCount(songId),
      this.getViewsCount(songId),
    ]);

    oldCategoryIds.forEach((catId) => {
      if (!newCategoryIds.includes(catId)) {
        pipeline.zrem(`category:${catId}:likes_ranking`, songId);
        pipeline.zrem(`category:${catId}:views_ranking`, songId);
        pipeline.zrem(`category:${catId}:newest_ranking`, songId);
      }
    });

    const newestScoreRaw = await this.redis.zscore('songs:newest_ranking', songId);
    const newestScore = newestScoreRaw ? Number(newestScoreRaw) : Date.now();

    newCategoryIds.forEach((catId) => {
      if (!oldCategoryIds.includes(catId)) {
        pipeline.zadd(`category:${catId}:likes_ranking`, likesCount, songId);
        pipeline.zadd(`category:${catId}:views_ranking`, viewsCount, songId);
        pipeline.zadd(`category:${catId}:newest_ranking`, newestScore, songId);
      }
    });

    await pipeline.exec();
  }

  async getLikesCount(songId: string): Promise<number> {
    const likesCountRaw = await this.redis.zscore('songs:likes_ranking', songId);
    return likesCountRaw ? parseInt(likesCountRaw, 10) : 0;
  }

  async getViewsCount(songId: string): Promise<number> {
    const viewsCountRaw = await this.redis.zscore('songs:views_ranking', songId);
    return viewsCountRaw ? parseInt(viewsCountRaw, 10) : 0;
  }


  async hasUserLiked(songId: string, userId?: string): Promise<boolean> {
    if (!userId) return false;
    const result = await this.redis.sismember(`song:${songId}:likes`, userId);
    return result === 1;
  }

  async getSongMetrics(songId: string, userId?: string) {
    const [likesCount, hasLiked, viewsCount] = await Promise.all([
      this.getLikesCount(songId),
      this.hasUserLiked(songId, userId),
      this.getViewsCount(songId),
    ]);

    return {
      likesCount,
      hasLiked,
      viewsCount,
    };
  }

  async toggleLike(songId: string, userId: string): Promise<boolean> {
    const isLiked = await this.hasUserLiked(songId, userId);
    const pipeline = this.redis.pipeline();

    const info = await this.redis.hgetall(`song:${songId}:info`);
    const authorId = info['authorId'];
    let categoryIds: string[] = [];
    if (info['categoryIds']) {
      try {
        categoryIds = JSON.parse(info['categoryIds']);
      } catch {
        // ignore
      }
    }

    if (isLiked) {
      pipeline.srem(`song:${songId}:likes`, userId);
      pipeline.zrem(`user:${userId}:likes`, songId);

      pipeline.zincrby(`songs:likes_ranking`, -1, songId);
      if (authorId) {
        pipeline.zincrby(`author:${authorId}:likes_ranking`, -1, songId);
      }
      categoryIds.forEach((catId) => {
        pipeline.zincrby(`category:${catId}:likes_ranking`, -1, songId);
      });

      await pipeline.exec();
      return false;
    } else {
      pipeline.sadd(`song:${songId}:likes`, userId);
      pipeline.zadd(`user:${userId}:likes`, Date.now(), songId);

      pipeline.zincrby(`songs:likes_ranking`, 1, songId);
      if (authorId) {
        pipeline.zincrby(`author:${authorId}:likes_ranking`, 1, songId);
      }
      categoryIds.forEach((catId) => {
        pipeline.zincrby(`category:${catId}:likes_ranking`, 1, songId);
      });

      await pipeline.exec();
      return true;
    }
  }

  async addView(songId: string, userId: string): Promise<void> {
    const cooldownKey = `view:cooldown:${userId}:${songId}`;
    const isOnCooldown = await this.redis.exists(cooldownKey);

    if (isOnCooldown) {
      return;
    }

    const timestamp = Date.now();
    const historyEntry = `${songId}:${randomUUID()}`;

    const info = await this.redis.hgetall(`song:${songId}:info`);
    const authorId = info['authorId'];
    let categoryIds: string[] = [];
    if (info['categoryIds']) {
      try {
        categoryIds = JSON.parse(info['categoryIds']);
      } catch {
        // ignore
      }
    }

    const pipeline = this.redis.pipeline();

    pipeline.zadd(`user:${userId}:history`, timestamp, historyEntry);
    pipeline.set(cooldownKey, '1', 'EX', 300);

    pipeline.zincrby(`songs:views_ranking`, 1, songId);

    if (authorId) {
      pipeline.zincrby(`author:${authorId}:views_ranking`, 1, songId);
    }

    categoryIds.forEach((catId) => {
      pipeline.zincrby(`category:${catId}:views_ranking`, 1, songId);
    });

    await pipeline.exec();
  }
}
