import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { SongRankingItem } from '@simple-music/interfaces';

export class SongRankingItemDto implements SongRankingItem {
  @ApiProperty({ description: 'ID пісні' })
  id!: string;

  @ApiProperty({ description: 'Назва пісні' })
  title!: string;

  @ApiPropertyOptional({ description: 'Обкладинка' })
  cover!: string | null;

  @ApiProperty({ description: 'Id автору'})
  authorId!: string;

  @ApiProperty({ description: 'Логін автору'})
  authorLogin!: string;

  @ApiPropertyOptional({ description: 'Оригінальний автор' })
  originalAuthor?: string;
}
