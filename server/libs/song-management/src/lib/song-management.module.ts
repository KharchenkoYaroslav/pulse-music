import { Module } from '@nestjs/common';
import { SongMetricsService } from './song-metrics.service';
import { LocalSongService } from './local-song.service';
import { ExternalSongService } from './external-song.service';
import { SongService } from './song.service';
import { MongooseModule } from '@nestjs/mongoose';
import { Song, SongSchema } from './song.schema';
import { RedisModule } from '@simple-music/redis';
import { StorageModule } from '@simple-music/minio';
import { CategoriesModule } from '@simple-music/categories';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Song.name, schema: SongSchema }
    ]),
    RedisModule,
    StorageModule,
    CategoriesModule,
  ],
  providers: [
    SongMetricsService,
    LocalSongService,
    ExternalSongService,
    SongService,
  ],
  exports: [
    SongMetricsService,
    LocalSongService,
    ExternalSongService,
    SongService,
  ],
})
export class SongManagementModule {}
