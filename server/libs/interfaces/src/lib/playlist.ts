import { SongRankingItem } from './song';

export interface Playlist {
  id: string;
  name: string;
  userId: string;
  songIds: string[];
}

export interface PlaylistResponse {
  id: string;
  name: string;
  userId: string;
  userLogin: string;
  songs: SongRankingItem[];
}

