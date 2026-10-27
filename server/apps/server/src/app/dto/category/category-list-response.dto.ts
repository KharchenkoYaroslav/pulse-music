import { ApiProperty } from '@nestjs/swagger';
import { CategoryListResponse } from '@simple-music/interfaces';

export class CategoryListResponseDto implements CategoryListResponse {
  @ApiProperty()
  id!: string;

  @ApiProperty()
  title!: string;
}

