import { Controller, Get, Query, HttpCode, HttpStatus } from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiQuery,
  ApiOkResponse,
} from '@nestjs/swagger';
import { RecommendationsService } from '@simple-music/recommendations';
import { UserService } from '@simple-music/user';
import { SongRankingItemDto } from './dto/song/song-ranking-item.dto';

@ApiTags('Recommendations')
@Controller('recommendations')
export class RecommendationsController {
  constructor(
    private readonly recommendationsService: RecommendationsService,
    private readonly userService: UserService,
  ) {}

  @ApiOperation({
    summary: 'Отримати загальну кількість пісень (за лайками)',
  })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { count: { type: 'number' } } },
    description: 'Кількість пісень',
  })
  @Get('count/likes')
  @HttpCode(HttpStatus.OK)
  async getSongsCountByLikes(
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<{ count: number }> {
    const count = await this.recommendationsService.getSongsCountByLikes(
      categoryId,
      authorId,
      search,
    );
    return { count };
  }

  @ApiOperation({
    summary: 'Отримати загальну кількість пісень (за переглядами)',
  })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { count: { type: 'number' } } },
    description: 'Кількість пісень',
  })
  @Get('count/views')
  @HttpCode(HttpStatus.OK)
  async getSongsCountByViews(
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<{ count: number }> {
    const count = await this.recommendationsService.getSongsCountByViews(
      categoryId,
      authorId,
      search,
    );
    return { count };
  }

  @ApiOperation({
    summary: 'Отримати список пісень, відсортованих за лайками',
  })
  @ApiQuery({ name: 'from', type: Number, description: 'Початковий індекс' })
  @ApiQuery({ name: 'to', type: Number, description: 'Кінцевий індекс' })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({ type: [SongRankingItemDto], description: 'Список пісень' })
  @Get('bylikes')
  @HttpCode(HttpStatus.OK)
  async getSongsSortedByLikes(
    @Query('from') from: number,
    @Query('to') to: number,
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<SongRankingItemDto[]> {
    const songs = await this.recommendationsService.getSongsSortedByLikes(
      from,
      to,
      categoryId,
      authorId,
      search,
    );
    await Promise.all(
      songs.map(async (song) => {
        const user = await this.userService.findById(song.authorId);
        song.authorLogin = user?.login || '';
      })
    );
    return songs;
  }

  @ApiOperation({
    summary: 'Отримати список пісень, відсортованих за переглядами',
  })
  @ApiQuery({ name: 'from', type: Number, description: 'Початковий індекс' })
  @ApiQuery({ name: 'to', type: Number, description: 'Кінцевий індекс' })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({ type: [SongRankingItemDto], description: 'Список пісень' })
  @Get('byviews')
  @HttpCode(HttpStatus.OK)
  async getSongsSortedByViews(
    @Query('from') from: number,
    @Query('to') to: number,
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<SongRankingItemDto[]> {
    const songs = await this.recommendationsService.getSongsSortedByViews(
      from,
      to,
      categoryId,
      authorId,
      search,
    );
    await Promise.all(
      songs.map(async (song) => {
        const user = await this.userService.findById(song.authorId);
        song.authorLogin = user?.login || '';
      })
    );
    return songs;
  }

  @ApiOperation({
    summary: 'Отримати загальну кількість пісень (за датою додавання)',
  })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { count: { type: 'number' } } },
    description: 'Кількість пісень',
  })
  @Get('count/newest')
  @HttpCode(HttpStatus.OK)
  async getSongsCountByNewest(
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<{ count: number }> {
    const count = await this.recommendationsService.getSongsCountByNewest(
      categoryId,
      authorId,
      search,
    );
    return { count };
  }

  @ApiOperation({
    summary: 'Отримати список пісень, відсортованих за датою додавання (від новіших)',
  })
  @ApiQuery({ name: 'from', type: Number, description: 'Початковий індекс' })
  @ApiQuery({ name: 'to', type: Number, description: 'Кінцевий індекс' })
  @ApiQuery({
    name: 'categoryId',
    type: String,
    required: false,
    description: 'ID категорії для фільтрації',
  })
  @ApiQuery({
    name: 'authorId',
    type: String,
    required: false,
    description: 'ID автора для фільтрації',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({ type: [SongRankingItemDto], description: 'Список пісень' })
  @Get('bynewest')
  @HttpCode(HttpStatus.OK)
  async getSongsSortedByNewest(
    @Query('from') from: number,
    @Query('to') to: number,
    @Query('categoryId') categoryId?: string,
    @Query('authorId') authorId?: string,
    @Query('search') search?: string,
  ): Promise<SongRankingItemDto[]> {
    const songs = await this.recommendationsService.getSongsSortedByNewest(
      from,
      to,
      categoryId,
      authorId,
      search,
    );
    await Promise.all(
      songs.map(async (song) => {
        const user = await this.userService.findById(song.authorId);
        song.authorLogin = user?.login || '';
      })
    );
    return songs;
  }
}

