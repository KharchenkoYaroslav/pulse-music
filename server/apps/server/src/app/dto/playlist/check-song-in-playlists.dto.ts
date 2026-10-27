import { ApiProperty } from '@nestjs/swagger';
import { IsArray, IsString } from 'class-validator';

export class CheckSongInPlaylistsDto {
  @ApiProperty({ description: 'Масив ID плейлістів', type: [String] })
  @IsArray()
  @IsString({ each: true })
  playlistIds: string[];
}
