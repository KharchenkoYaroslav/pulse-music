import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { SongHistoryItem } from '@simple-music/interfaces';

export class SongHistoryItemDto implements SongHistoryItem {
  @ApiProperty({ description: 'ID пісні' })
  id!: string;

  @ApiProperty({ description: 'Назва пісні' })
  title!: string;

  @ApiPropertyOptional({ description: 'Обкладинка' })
  cover!: string | null;

  @ApiProperty({ description: 'Таймстемп останнього перегляду користувачем' })
  viewedAt!: number;

  @ApiProperty({ description: 'Id автору'})
  authorId!: string;

  @ApiProperty({ description: 'Логін автору'})
  authorLogin!: string;

  @ApiPropertyOptional({ description: 'Оригінальний автор' })
  originalAuthor?: string;
}
