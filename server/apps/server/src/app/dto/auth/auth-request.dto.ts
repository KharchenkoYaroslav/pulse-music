import { ApiProperty } from '@nestjs/swagger';
import { AuthRequest, AuthUser } from '@simple-music/interfaces';

export class AuthUserDto implements AuthUser {
  @ApiProperty({ description: 'ID користувача' })
  sub!: string;
}

export class AuthRequestDto implements AuthRequest {
  @ApiProperty({ type: AuthUserDto, description: 'Інформація про користувача' })
  user!: AuthUserDto;
}
