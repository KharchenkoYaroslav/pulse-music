import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsArray, IsUrl, IsMongoId } from 'class-validator';
import { UpdateExternalSong } from '@simple-music/interfaces';

export class UpdateExternalSongParamsDto implements UpdateExternalSong {
  @ApiPropertyOptional({ description: 'URL пісні з зовнішнього джерела (https://audius.co/...)' })
  @IsOptional()
  @IsString()
  @IsUrl()
  externalUrl?: string;

  @ApiPropertyOptional({ description: 'Категорії пісні' })
  @IsOptional()
  @IsArray()
  @IsMongoId({ each: true })
  categoryIds?: string[];
}

