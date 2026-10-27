import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsNotEmpty, IsOptional, IsArray, IsUrl, IsMongoId } from 'class-validator';
import { CreateExternalSong } from '@simple-music/interfaces';

export class CreateExternalSongDto implements CreateExternalSong {
  @ApiProperty({ description: 'URL пісні з зовнішнього джерела ( https://audius.co/...)' })
  @IsString()
  @IsNotEmpty()
  @IsUrl()
  externalUrl!: string;

  @ApiPropertyOptional({ description: 'Категорії пісні' })
  @IsOptional()
  @IsArray()
  @IsMongoId({ each: true })
  categoryIds?: string[];
}

