import { ApiProperty } from '@nestjs/swagger';

export class UserPlaylistDto {
  @ApiProperty({ description: 'ID плейлісту' })
  id!: string;

  @ApiProperty({ description: 'Назва плейлісту' })
  name!: string;
}

