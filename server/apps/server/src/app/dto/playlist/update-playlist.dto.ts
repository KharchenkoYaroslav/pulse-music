import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, MaxLength } from 'class-validator';

export class UpdatePlaylistDto {
  @ApiProperty({ description: 'Нова назва плейлісту', example: 'Моя нова музика' })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  name!: string;
}
