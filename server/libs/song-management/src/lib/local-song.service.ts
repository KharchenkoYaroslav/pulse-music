import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  BadRequestException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { Song, SongDocument } from './song.schema';
import { StorageService } from '@simple-music/minio';
import {
  CreateLocalSong,
  UpdateLocalSong,
} from '@simple-music/interfaces';
import { SongMetricsService } from './song-metrics.service';
import { EventEmitter2, OnEvent } from '@nestjs/event-emitter';
import { CategoriesService } from '@simple-music/categories';

@Injectable()
export class LocalSongService {
  constructor(
    @InjectModel(Song.name) private songBuilder: Model<SongDocument>,
    private readonly storageService: StorageService,
    private readonly songMetricsService: SongMetricsService,
    private readonly eventEmitter: EventEmitter2,
    private readonly categoriesService: CategoriesService,
  ) {}

  async createLocalSong(
    userId: string,
    params: CreateLocalSong,
    songFile?: Express.Multer.File,
    coverFile?: Express.Multer.File,
  ): Promise<{ id: string }> {
    const uniqueCategoryIds = params.categoryIds ? [...new Set(params.categoryIds)] : [];
    if (uniqueCategoryIds.length > 0) {
      const areCategoriesValid = await this.categoriesService.areCategoriesValid(uniqueCategoryIds);
      if (!areCategoriesValid) {
        throw new BadRequestException('One or more specified categories do not exist');
      }
    }

    let songFilename: string | null = null;
    let coverFilename: string | null = null;

    if (songFile) {
      const { filename } = await this.storageService.uploadFile(songFile);
      songFilename = filename;
    }

    if (coverFile) {
      const { filename } = await this.storageService.uploadFile(coverFile);
      coverFilename = filename;
    }

    const newSong = new this.songBuilder({
      title: params.title,
      description: params.description || '',
      authorId: userId,
      originalAuthor: params.originalAuthor || '',
      categoryIds: uniqueCategoryIds.map(id => new Types.ObjectId(id)),
      song: songFilename,
      cover: coverFilename,
    });

    const savedSong = await newSong.save();
    const songId = savedSong._id.toString();

    await this.songMetricsService.initSongMetrics(songId, userId, uniqueCategoryIds, savedSong.createdAt.getTime());
    return { id: songId };
  }

  async deleteLocalSong(songId: string, userId: string): Promise<void> {
    const song = await this.checkAccessAndGetSong(songId, userId);

    const filesToDelete: string[] = [];

    const songFilename = song.song || null;
    if (songFilename) {
      filesToDelete.push(songFilename);
    }
    const coverFilename = song.cover || null;
    if (coverFilename) {
      filesToDelete.push(coverFilename);
    }

    if (filesToDelete.length > 0) {
      await Promise.all(
        filesToDelete.map((filename) =>
          this.storageService.deleteFile(filename).catch((err) => {
            console.error(`Failed to delete file ${filename}:`, err);
          }),
        ),
      );
    }

    await this.songBuilder.deleteOne({ _id: songId }).exec();

    const categoryIds = song.categoryIds?.map((id) => id.toString()) || [];
    await this.songMetricsService.cleanupSongMetrics(songId, userId, categoryIds);
    this.eventEmitter.emit('song.deleted', { songId });
  }

  @OnEvent('user.deleted')
  async handleUserDeleted(payload: { userId: string }): Promise<void> {
    await this.deleteAllUserLocalSongs(payload.userId);
  }

  async deleteAllUserLocalSongs(userId: string): Promise<void> {
    const userSongs = await this.songBuilder
      .find({ authorId: userId })
      .lean()
      .exec();

    for (const song of userSongs) {
      await this.deleteLocalSong(song._id.toString(), userId);
    }
  }

  async updateLocalSong(
    songId: string,
    userId: string,
    params: UpdateLocalSong,
    songFile?: Express.Multer.File,
    coverFile?: Express.Multer.File,
  ): Promise<void> {
    const song = await this.checkAccessAndGetSong(songId, userId);
    const updateData: Partial<Song> = {};
    const filesToDelete: string[] = [];

    if (params.title !== undefined) updateData.title = params.title;
    if (params.description !== undefined) updateData.description = params.description;
    if (params.originalAuthor !== undefined) updateData.originalAuthor = params.originalAuthor;

    if (params.categoryIds !== undefined) {
      const oldCategoryIds = song.categoryIds?.map((cat) => cat.toString()) || [];
      const newCategoryIds = [...new Set(params.categoryIds)];

      const isCategoriesChanged =
        !(newCategoryIds.length === oldCategoryIds.length && newCategoryIds.every((id) => oldCategoryIds.includes(id)));

      if (isCategoriesChanged) {
        if (newCategoryIds.length > 0) {
          const areCategoriesValid = await this.categoriesService.areCategoriesValid(newCategoryIds);
          if (!areCategoriesValid) {
            throw new BadRequestException('One or more specified categories do not exist');
          }
        }
        updateData.categoryIds = newCategoryIds.map((id) => new Types.ObjectId(id));
        await this.songMetricsService.updateCategoryRankings(songId, newCategoryIds, oldCategoryIds);
      }
    }

    if (songFile) {
      const { filename } = await this.storageService.uploadFile(songFile);
      updateData.song = filename;
      if (song.song) filesToDelete.push(song.song);
    }

    if (coverFile) {
      const { filename } = await this.storageService.uploadFile(coverFile);
      updateData.cover = filename;
      if (song.cover) filesToDelete.push(song.cover);
    }

    if (Object.keys(updateData).length > 0) {
      await this.songBuilder.updateOne({ _id: songId }, { $set: updateData }).exec();
    }

    if (filesToDelete.length > 0) {
      await Promise.all(
        filesToDelete.map((filename) =>
          this.storageService.deleteFile(filename).catch((err) => {
            console.error(`Failed to delete file ${filename}:`, err);
          }),
        ),
      );
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

