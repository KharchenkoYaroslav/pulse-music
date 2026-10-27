import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Song } from '@simple-music/interfaces';

export class SongResponseDto implements Song {
  @ApiProperty({ description: 'ID пісні' })
  id!: string;

  @ApiProperty({ description: 'ID автора' })
  authorId!: string;

  @ApiProperty({ description: 'Логін автора' })
  authorLogin!: string;

  @ApiProperty({ description: 'Категорії' })
  categoryIds!: string[];

  @ApiProperty({ description: 'Кількість лайків' })
  likesCount!: number;

  @ApiProperty({ description: 'Чи лайкнув поточний користувач' })
  hasLiked!: boolean;

  @ApiProperty({ description: 'Кількість переглядів' })
  viewsCount!: number;

  @ApiProperty({ description: 'Назва пісні' })
  title!: string;

  @ApiProperty({ description: 'Опис пісні' })
  description!: string;

  @ApiPropertyOptional({ description: 'Файл пісні (якщо є)' })
  song!: string | null;

  @ApiPropertyOptional({ description: 'Обкладинка (якщо є)' })
  cover!: string | null;

  @ApiPropertyOptional({ description: 'Оригінальний автор (опційно)' })
  originalAuthor?: string;
}
