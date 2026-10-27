import { ApiProperty } from '@nestjs/swagger';
import { AuthResponse } from '@simple-music/interfaces';

export class AuthResponseDto implements AuthResponse {
  @ApiProperty({ description: 'Токен доступу (JWT)' })
  accessToken!: string;

  @ApiProperty({ description: 'Refresh токен (JWT)' })
  refreshToken!: string;
}
