import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  BadRequestException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { Song, SongDocument } from './song.schema';
import {
  CreateExternalSong,
  UpdateExternalSong,
} from '@simple-music/interfaces';
import { SongMetricsService } from './song-metrics.service';
import { EventEmitter2, OnEvent } from '@nestjs/event-emitter';
import { CategoriesService } from '@simple-music/categories';

@Injectable()
export class ExternalSongService {
  constructor(
    @InjectModel(Song.name) private songBuilder: Model<SongDocument>,
    private readonly songMetricsService: SongMetricsService,
    private readonly eventEmitter: EventEmitter2,
    private readonly categoriesService: CategoriesService,
  ) {}

  private isValidAudiusUrl(url: string): boolean {
    return /^https:\/\/(www\.)?audius\.co\/.*$/.test(url);
  }

  private async fetchAudiusTrackData(url: string) {
    try {
      const response = await fetch(`https://discoveryprovider.audius.co/v1/resolve?url=${encodeURIComponent(url)}&app_name=simple_music`);
      if (!response.ok) {
        throw new Error(`Failed to resolve Audius URL: ${response.statusText}`);
      }
      const { data } = await response.json();
      if (!data || !data.id) {
        throw new Error('Track not found');
      }
      return {
        title: data.title,
        description: data.description || '',
        originalAuthor: data.user?.name || data.user?.handle || 'Unknown',
        song: `https://discoveryprovider.audius.co/v1/tracks/${data.id}/stream?app_name=simple_music`,
        cover: data.artwork?.['480x480'] || data.artwork?.['150x150'] || data.artwork?.['1000x1000'] || null,
      };
    } catch {
      throw new BadRequestException('Invalid Audius track URL or track not found');
    }
  }

  async createExternalSong(
    userId: string,
    params: CreateExternalSong,
  ): Promise<{ id: string }> {
    if (!this.isValidAudiusUrl(params.externalUrl)) {
      throw new BadRequestException(
        'Invalid Audius URL. Supported format: https://audius.co/...',
      );
    }

    const uniqueCategoryIds = params.categoryIds ? [...new Set(params.categoryIds)] : [];
    if (uniqueCategoryIds.length > 0) {
      const areCategoriesValid = await this.categoriesService.areCategoriesValid(uniqueCategoryIds);
      if (!areCategoriesValid) {
        throw new BadRequestException('One or more specified categories do not exist');
      }
    }

    const trackData = await this.fetchAudiusTrackData(params.externalUrl);

    const newSong = new this.songBuilder({
      authorId: userId,
      ...trackData,
      categoryIds: uniqueCategoryIds.map((id) => new Types.ObjectId(id)),
    });

    const savedSong = await newSong.save();
    const songId = savedSong._id.toString();

    await this.songMetricsService.initSongMetrics(songId, userId, uniqueCategoryIds, savedSong.createdAt.getTime());
    return { id: songId };
  }

  async updateExternalSong(
    songId: string,
    userId: string,
    params: UpdateExternalSong,
  ): Promise<void> {
    const song = await this.checkAccessAndGetSong(songId, userId);
    const updateData: Partial<Song> = {};

    if (params.categoryIds !== undefined) {
      const uniqueCategoryIds = [...new Set(params.categoryIds)];
      if (uniqueCategoryIds.length > 0) {
        const areCategoriesValid = await this.categoriesService.areCategoriesValid(uniqueCategoryIds);
        if (!areCategoriesValid) {
          throw new BadRequestException('One or more specified categories do not exist');
        }
      }
    }

    if (params.externalUrl !== undefined) {
      if (!this.isValidAudiusUrl(params.externalUrl)) {
        throw new BadRequestException(
          'Invalid Audius URL. Supported format: https://audius.co/...',
        );
      }
      const trackData = await this.fetchAudiusTrackData(params.externalUrl);
      Object.assign(updateData, trackData);
    }

    if (params.categoryIds !== undefined) {
      const oldCategoryIds =
        song.categoryIds?.map((cat) => cat.toString()) || [];
      const newCategoryIds = [...new Set(params.categoryIds)];

      const isCategoriesChanged = !(
        newCategoryIds.length === oldCategoryIds.length &&
        newCategoryIds.every((id) => oldCategoryIds.includes(id))
      );

      if (isCategoriesChanged) {
        updateData.categoryIds = newCategoryIds.map(
          (id) => new Types.ObjectId(id),
        );
        await this.songMetricsService.updateCategoryRankings(
          songId,
          newCategoryIds,
          oldCategoryIds,
        );
      }
    }

    if (Object.keys(updateData).length > 0) {
      await this.songBuilder
        .updateOne({ _id: songId }, { $set: updateData })
        .exec();
    }
  }

  async deleteExternalSong(songId: string, userId: string): Promise<void> {
    const song = await this.checkAccessAndGetSong(songId, userId);

    await this.songBuilder.deleteOne({ _id: songId }).exec();

    const categoryIds = song.categoryIds?.map((id) => id.toString()) || [];
    await this.songMetricsService.cleanupSongMetrics(
      songId,
      userId,
      categoryIds,
    );
    this.eventEmitter.emit('song.deleted', { songId });
  }

  @OnEvent('user.deleted')
  async handleUserDeleted(payload: { userId: string }): Promise<void> {
    await this.deleteAllUserExternalSongs(payload.userId);
  }

  async deleteAllUserExternalSongs(userId: string): Promise<void> {
    const userSongs = await this.songBuilder
      .find({ authorId: userId })
      .lean()
      .exec();

    for (const song of userSongs) {
      await this.deleteExternalSong(song._id.toString(), userId);
    }
  }

  private async checkAccessAndGetSong(songId: string, userId: string) {
    const song = await this.songBuilder.findById(songId).lean().exec();
    if (!song) {
      throw new NotFoundException(`Song with ID ${songId} not found`);
    }
    if (song.authorId.toString() !== userId) {
      throw new ForbiddenException('You are not the author of this song');
    }
    return song;
  }
}
