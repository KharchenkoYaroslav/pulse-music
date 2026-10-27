import { ApiProperty } from '@nestjs/swagger';
import { RefreshRequest, RefreshUser } from '@simple-music/interfaces';

export class RefreshUserDto implements RefreshUser {
  @ApiProperty({ description: 'ID користувача' })
  sub!: string;

  @ApiProperty({ description: 'Refresh токен' })
  refreshToken!: string;
}

export class RefreshRequestDto implements RefreshRequest {
  @ApiProperty({ type: RefreshUserDto, description: 'Інформація про користувача' })
  user!: RefreshUserDto;
}
