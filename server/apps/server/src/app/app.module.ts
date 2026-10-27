import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { EventEmitterModule } from '@nestjs/event-emitter';
import { StorageModule } from '@simple-music/minio';
import { AuthModule } from '@simple-music/auth';
import { UsersModule } from '@simple-music/user';
import { PlaylistsModule } from '@simple-music/playlists';
import { SongManagementModule } from '@simple-music/song-management';
import { RecommendationsModule } from '@simple-music/recommendations';
import { DatabaseModule } from '@simple-music/database';
import { RedisModule } from '@simple-music/redis';
import { CategoriesModule } from '@simple-music/categories';
import { AuthController } from './auth.controller';
import { UsersController } from './user.controller';
import { CategoriesController } from './categories.controller';
import { SongController } from './song.controller';
import { LocalSongController } from './local-song.controller';
import { ExternalSongController } from './external-song.controller';
import { RecommendationsController } from './recommendations.controller';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),
    EventEmitterModule.forRoot(),
    DatabaseModule,
    StorageModule,
    RedisModule,
    AuthModule,
    UsersModule,
    PlaylistsModule,
    SongManagementModule,
    RecommendationsModule,
    CategoriesModule,
  ],
  controllers: [
    AuthController,
    UsersController,
    CategoriesController,
    SongController,
    LocalSongController,
    ExternalSongController,
    RecommendationsController,
  ],
})
export class AppModule {}
