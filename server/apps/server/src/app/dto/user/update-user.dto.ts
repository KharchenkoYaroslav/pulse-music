import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, MinLength } from 'class-validator';
import { UpdateUser } from '@simple-music/interfaces';

export class UpdateUserDto implements UpdateUser {
  @ApiPropertyOptional({ description: 'Поточний пароль (обов\'язковий для зміни пароля)' })
  @IsString()
  @IsOptional()
  currentPassword?: string;

  @ApiPropertyOptional({ description: 'Новий логін' })
  @IsString()
  @IsOptional()
  login?: string;

  @ApiPropertyOptional({ description: 'Новий пароль' })
  @IsString()
  @IsOptional()
  @MinLength(6)
  password?: string;
}
