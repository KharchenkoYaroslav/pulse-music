import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Song, SongDocument } from './song.schema';
import { Song as SongInterface } from '@simple-music/interfaces';
import { SongMetricsService } from './song-metrics.service';

@Injectable()
export class SongService {
  constructor(
    @InjectModel(Song.name) private songBuilder: Model<SongDocument>,
    private readonly songMetricsService: SongMetricsService,
  ) {}

  async getSongById(id: string, userId?: string): Promise<SongInterface | null> {
    const song = await this.songBuilder.findById(id).lean().exec();

    if (!song) {
      return null;
    }

    const metrics = await this.songMetricsService.getSongMetrics(id, userId);

    const Song = {
      id: song._id.toString(),
      title: song.title || '',
      description: song.description || '',
      song: song.song || null,
      cover: song.cover || null,
      authorId: song.authorId.toString(),
      authorLogin: '', // Додамо в контролері щоб не викликати кругову залежність з UserService
      originalAuthor: song.originalAuthor || '',
      categoryIds: song.categoryIds?.map((catId) => catId.toString()) || [],
      likesCount: metrics.likesCount,
      hasLiked: metrics.hasLiked,
      viewsCount: metrics.viewsCount,
    };

    return Song;
  }

  async fetchSongDocsAndMap(songIds: string[]) {
    const filter = { _id: { $in: songIds } };
    const songsFromDb = await this.songBuilder
      .find(filter)
      .select('title cover originalAuthor authorId')
      .lean()
      .exec();
    return new Map(songsFromDb.map((song) => [song._id.toString(), song]));
  }

  async isSongValid(songId: string): Promise<boolean> {
    return (await this.songBuilder.exists({ _id: songId })) !== null;
  }

  async findSongIdsBySearch(searchQuery: string, matchedAuthorIds: string[]): Promise<string[]> {
    const filter = {
      $or: [
        { title: { $regex: searchQuery, $options: 'i' } },
        { originalAuthor: { $regex: searchQuery, $options: 'i' } },
        { authorId: { $in: matchedAuthorIds } }
      ]
    };
    const songs = await this.songBuilder.find(filter).select('_id').lean().exec();
    return songs.map(s => s._id.toString());
  }
}
