import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, MaxLength } from 'class-validator';

export class CreatePlaylistDto {
  @ApiProperty({ description: 'Назва плейлісту', example: 'Моя музика' })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  name!: string;
}

