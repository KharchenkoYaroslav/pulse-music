export interface Song {
  id: string;
  title: string;
  description: string;
  authorId: string;
  authorLogin: string;
  originalAuthor?: string;
  categoryIds: string[];
  likesCount: number;
  hasLiked: boolean;
  viewsCount: number;
  song: string | null;
  cover: string | null;
}

export interface SongRankingItem {
  id: string;
  authorId: string;
  authorLogin: string;
  originalAuthor?: string;
  title: string;
  cover: string | null;
}

export type SongHistoryItem = SongRankingItem & {
  viewedAt: number;
};

export interface CreateLocalSong {
  title: string;
  originalAuthor?: string;
  description?: string;
  categoryIds?: string[];
}

export interface UpdateLocalSong {
  title?: string;
  originalAuthor?: string;
  description?: string;
  categoryIds?: string[];
}

export interface CreateExternalSong {
  externalUrl: string;
  categoryIds?: string[];
}

export interface UpdateExternalSong {
  externalUrl?: string;
  categoryIds?: string[];
}
