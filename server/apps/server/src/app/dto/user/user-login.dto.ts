import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty } from 'class-validator';
import { UserLogin } from '@simple-music/interfaces';

export class UserLoginDto implements UserLogin {
  @ApiProperty({ description: 'Логін користувача' })
  @IsString()
  @IsNotEmpty()
  login!: string;
}
