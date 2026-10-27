import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { Playlist, PlaylistSchema } from './playlist.schema';
import { UserPlaylistsService } from './user-playlists.service';
import { RedisModule } from '@simple-music/redis';
import { SongManagementModule } from '@simple-music/song-management';
import { UsersModule } from '@simple-music/user';

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Playlist.name, schema: PlaylistSchema }]),
    RedisModule,
    SongManagementModule,
    UsersModule,
  ],
  providers: [UserPlaylistsService],
  exports: [UserPlaylistsService],
})
export class PlaylistsModule {}
