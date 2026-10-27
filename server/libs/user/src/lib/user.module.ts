import { Module } from '@nestjs/common';
import { UserService } from './user.service';
import { MongooseModule } from '@nestjs/mongoose';
import { User, UserSchema } from './user.schema';
import { UserMetricsService } from './user-metrics.service';
import { RedisModule } from '@simple-music/redis';
import { SongManagementModule } from '@simple-music/song-management';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: User.name, schema: UserSchema }
    ]),
    RedisModule,
    SongManagementModule,
  ],
  providers: [UserService, UserMetricsService],
  exports: [
    UserService,
    UserMetricsService,
  ],
})
export class UsersModule {}

