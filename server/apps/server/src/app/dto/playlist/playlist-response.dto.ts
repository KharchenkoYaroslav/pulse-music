import { ApiProperty } from '@nestjs/swagger';
import { SongRankingItemDto } from '../song/song-ranking-item.dto';

export class PlaylistResponseDto {
  @ApiProperty({ description: 'ID плейлісту' })
  id!: string;

  @ApiProperty({ description: 'Назва плейлісту' })
  name!: string;

  @ApiProperty({ description: 'ID користувача' })
  userId!: string;

  @ApiProperty({ description: 'Логін користувача' })
  userLogin!: string;

  @ApiProperty({ type: [SongRankingItemDto], description: 'Пісні у плейлісті' })
  songs!: SongRankingItemDto[];
}
