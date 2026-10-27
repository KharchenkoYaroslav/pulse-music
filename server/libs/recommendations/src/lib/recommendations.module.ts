import { Module } from '@nestjs/common';
import { RecommendationsService } from './recommendations.service';
import { RedisModule } from '@simple-music/redis';
import { SongManagementModule } from '@simple-music/song-management';
import { UsersModule } from '@simple-music/user';

@Module({
  imports: [RedisModule, SongManagementModule, UsersModule],
  controllers: [],
  providers: [RecommendationsService],
  exports: [RecommendationsService],
})
export class RecommendationsModule {}
