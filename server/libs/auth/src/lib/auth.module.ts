import { Module, MiddlewareConsumer, NestModule, RequestMethod } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { AuthService } from './auth.service';
import { AuthGuard } from './auth.guard';
import { RefreshTokenGuard } from './refresh.guard';
import { AuthMiddleware } from './auth.middleware';
import { UsersModule } from '@simple-music/user';

@Module({
  imports: [UsersModule, JwtModule.register({})],
  providers: [AuthService, AuthGuard, RefreshTokenGuard],
  exports: [AuthService, AuthGuard, RefreshTokenGuard, JwtModule],
})
export class AuthModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(AuthMiddleware)
      .exclude(
        { path: 'api/auth/refresh', method: RequestMethod.POST },
        { path: 'auth/refresh', method: RequestMethod.POST }
      )
      .forRoutes('*');
  }
}
