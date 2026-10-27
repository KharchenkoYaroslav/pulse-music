import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, MinLength } from 'class-validator';
import { AuthCredentials } from '@simple-music/interfaces';

export class AuthCredentialsDto implements AuthCredentials {
  @ApiProperty({ description: 'Логін користувача' })
  @IsString()
  @IsNotEmpty()
  login!: string;

  @ApiProperty({ description: 'Пароль користувача' })
  @IsString()
  @IsNotEmpty()
  @MinLength(6)
  password!: string;
}

