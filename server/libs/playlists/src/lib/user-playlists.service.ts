import { Injectable, NotFoundException, BadRequestException, ForbiddenException} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Playlist, PlaylistDocument } from './playlist.schema';
import { SongService } from '@simple-music/song-management';
import { PlaylistResponse, SongRankingItem } from '@simple-music/interfaces';
import { InjectRedis } from '@nestjs-modules/ioredis';
import Redis from 'ioredis';
import { OnEvent } from '@nestjs/event-emitter';
import { UserService } from '@simple-music/user';

@Injectable()
export class UserPlaylistsService {
  constructor(
    @InjectModel(Playlist.name) private playlistModel: Model<PlaylistDocument>,
    private readonly songService: SongService,
    private readonly userService: UserService,
    @InjectRedis() private readonly redis: Redis,
  ) {}

  @OnEvent('song.deleted')
  async handleSongDeleted(payload: { songId: string }) {
    await this.playlistModel.updateMany({ songIds: payload.songId }, { $pull: { songIds: payload.songId } }).exec();
  }

  @OnEvent('user.deleted')
  async handleUserDeleted(payload: { userId: string }): Promise<void> {
    const userPlaylists = await this.playlistModel.find({ userId: payload.userId }).lean().exec();
    for (const playlist of userPlaylists) {
      await this.deletePlaylist(playlist._id.toString(), payload.userId);
    }
  }

  async createPlaylist(userId: string, name: string): Promise<string> {
    const newPlaylist = new this.playlistModel({
      name,
      userId,
      songIds: [],
    });
    const saved = await newPlaylist.save();
    return saved._id.toString();
  }

  async updatePlaylistName(playlistId: string, name: string, userId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(playlistId).exec();
    if (!playlist) throw new NotFoundException('Плейліст не знайдено');
    if (playlist.userId.toString() !== userId) throw new ForbiddenException('Немає доступу до плейлісту');
    playlist.name = name;
    await playlist.save();
  }

  async deletePlaylist(playlistId: string, userId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(playlistId).exec();
    if (!playlist) throw new NotFoundException('Плейліст не знайдено');
    if (playlist.userId.toString() !== userId) throw new ForbiddenException('Немає доступу до плейлісту');
    await this.playlistModel.deleteOne({ _id: playlistId }).exec();
  }

  async addSongToPlaylist(playlistId: string, songId: string, userId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(playlistId).exec();
    if (!playlist) throw new NotFoundException('Плейліст не знайдено');
    if (playlist.userId.toString() !== userId) throw new ForbiddenException('Немає доступу до плейлісту');

    const isValid = await this.songService.isSongValid(songId);
    if (!isValid) throw new BadRequestException('Пісня не знайдена або недійсна');

    if (!playlist.songIds.includes(songId)) {
      playlist.songIds.push(songId);
      await playlist.save();
    }
  }

  async removeSongFromPlaylist(playlistId: string, songId: string, userId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(playlistId).exec();
    if (!playlist) throw new NotFoundException('Плейліст не знайдено');
    if (playlist.userId.toString() !== userId) throw new ForbiddenException('Немає доступу до плейлісту');

    if (playlist.songIds.includes(songId)) {
      playlist.songIds = playlist.songIds.filter(id => id !== songId);
      await playlist.save();
    }
  }

  async getPlaylist(playlistId: string, userId: string): Promise<PlaylistResponse> {
    const playlist = await this.playlistModel.findById(playlistId).lean().exec();
    if (!playlist) throw new NotFoundException('Плейліст не знайдено');

    if (playlist.userId.toString() !== userId) throw new ForbiddenException('Немає доступу до плейлісту');

    const songIds = playlist.songIds || [];

    const user = await this.userService.findById(playlist.userId.toString());
    const userLogin = user?.login || '';

    if (songIds.length === 0) {
      return {
        id: playlist._id.toString(),
        name: playlist.name,
        userId: playlist.userId.toString(),
        userLogin,
        songs: [],
      };
    }

    const songsMap = await this.songService.fetchSongDocsAndMap(songIds);

    const songs: SongRankingItem[] = songIds.map(statId => {
      const songDoc = songsMap.get(statId);
      if (!songDoc) return null;

      return {
        id: statId,
        authorId: songDoc.authorId.toString(),
        authorLogin: '',
        originalAuthor: songDoc.originalAuthor,
        title: songDoc.title || '',
        cover: songDoc.cover || null,
      } as SongRankingItem;
    }).filter((item): item is SongRankingItem => item !== null);

    return {
      id: playlist._id.toString(),
      name: playlist.name,
      userId: playlist.userId.toString(),
      userLogin,
      songs,
    };
  }

  async getUserPlaylists(userId: string, search?: string): Promise<{ id: string; name: string }[]> {
    if (!search) {
      const playlists = await this.playlistModel.find({ userId }).select('_id name').lean().exec();
      return playlists.map(p => ({ id: p._id.toString(), name: p.name }));
    }

    const playlists = await this.playlistModel.find({ userId }).select('_id name songIds').lean().exec();
    const matchedSongIds = await this.songService.findSongIdsBySearch(search, []);
    const matchedSet = new Set(matchedSongIds);

    const regex = new RegExp(search, 'i');
    return playlists
      .filter(p =>
        regex.test(p.name) ||
        (p.songIds || []).some(id => matchedSet.has(id)),
      )
      .map(p => ({ id: p._id.toString(), name: p.name }));
  }

  async checkSongInPlaylists(songId: string, playlistIds: string[], userId: string): Promise<Record<string, boolean>> {
    const playlists = await this.playlistModel.find({
      _id: { $in: playlistIds },
      userId
    }).select('_id songIds').lean().exec();

    const result: Record<string, boolean> = {};
    for (const id of playlistIds) {
      result[id] = false;
    }

    for (const playlist of playlists) {
      const idStr = playlist._id.toString();
      if (playlist.songIds && playlist.songIds.includes(songId)) {
        result[idStr] = true;
      }
    }

    return result;
  }
}
