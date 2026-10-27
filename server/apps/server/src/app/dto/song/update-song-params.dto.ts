import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsArray, IsMongoId } from 'class-validator';
import { Transform } from 'class-transformer';
import { UpdateLocalSong } from '@simple-music/interfaces';

export class UpdateSongParamsDto implements UpdateLocalSong {
  @ApiPropertyOptional({ description: 'Назва пісні (для локальних)' })
  @IsOptional()
  @IsString()
  title?: string;

  @ApiPropertyOptional({ description: 'Опис пісні (для локальних)' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ description: 'Оригінальний автор пісні (для локальних)' })
  @IsOptional()
  @IsString()
  originalAuthor?: string;

  @ApiPropertyOptional({ description: 'Категорії пісні' })
  @IsOptional()
  @IsArray()
  @IsMongoId({ each: true })
  @Transform(({ value }) => {
    if (Array.isArray(value)) return value;
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) return [];
      return trimmed.split(',').map(s => s.trim());
    }
    return value;
  })
  categoryIds?: string[];

  @ApiPropertyOptional({ type: 'string', format: 'binary', description: 'Аудіофайл пісні' })
  song: Express.Multer.File;

  @ApiPropertyOptional({ type: 'string', format: 'binary', description: 'Обкладинка пісні' })
  cover: Express.Multer.File;
}
