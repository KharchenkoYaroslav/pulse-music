import {
  Controller,
  Get,
  Post,
  Param,
  Put,
  Delete,
  Body,
  UseGuards,
  Request,
  NotFoundException,
  UseInterceptors,
  ClassSerializerInterceptor,
  HttpCode,
  HttpStatus,
  Query,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiOkResponse,
  ApiQuery,
} from '@nestjs/swagger';
import { UserService } from '@simple-music/user';
import { UserPlaylistsService } from '@simple-music/playlists';
import { AuthGuard } from '@simple-music/auth';
import { UpdateUserDto } from './dto/user/update-user.dto';
import { UserResponseDto } from './dto/user/user-response.dto';
import { AuthRequestDto } from './dto/auth/auth-request.dto';
import { UserLoginDto } from './dto/user/user-login.dto';
import { SongHistoryItemDto } from './dto/song/song-history-item.dto';
import { SongRankingItemDto } from './dto/song/song-ranking-item.dto';
import { CreatePlaylistDto } from './dto/playlist/create-playlist.dto';
import { UpdatePlaylistDto } from './dto/playlist/update-playlist.dto';
import { PlaylistResponseDto } from './dto/playlist/playlist-response.dto';
import { UserPlaylistDto } from './dto/playlist/user-playlist.dto';
import { CheckSongInPlaylistsDto } from './dto/playlist/check-song-in-playlists.dto';
import { UserMetricsService } from '@simple-music/user';

@ApiTags('Users')
@Controller('users')
@UseInterceptors(ClassSerializerInterceptor)
export class UsersController {
  constructor(
    private readonly userService: UserService,
    private readonly userMetricsService: UserMetricsService,
    private readonly userPlaylistsService: UserPlaylistsService,
  ) {}

  @ApiOperation({ summary: 'Отримати профіль користувача за ID' })
  @ApiOkResponse({ type: UserResponseDto, description: 'Профіль користувача' })
  @Get('profile/:id')
  @HttpCode(HttpStatus.OK)
  async getProfileById(@Param('id') id: string): Promise<UserResponseDto> {
    const user = await this.userService.findById(id);
    if (!user) throw new NotFoundException('Користувача не знайдено');
    return new UserResponseDto(user);
  }

  @ApiOperation({ summary: 'Отримати логін користувача за ID' })
  @ApiOkResponse({ type: UserLoginDto, description: 'Логін користувача' })
  @Get('login/:id')
  @HttpCode(HttpStatus.OK)
  async getLoginById(@Param('id') id: string): Promise<UserLoginDto> {
    const user = await this.userService.findById(id);
    if (!user) throw new NotFoundException('Користувача не знайдено');
    const loginDto = new UserLoginDto();
    loginDto.login = user.login;
    return loginDto;
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Оновити профіль поточного користувача' })
  @ApiOkResponse({
    type: UserResponseDto,
    description: 'Оновлений профіль користувача',
  })
  @UseGuards(AuthGuard)
  @Put('profile')
  @HttpCode(HttpStatus.OK)
  async update(
    @Request() req: AuthRequestDto,
    @Body() updateUserDto: UpdateUserDto,
  ): Promise<UserResponseDto> {
    const userId = req.user.sub;
    const { currentPassword, ...updateData } = updateUserDto;
    const updatedUser = await this.userService.updateProfile(
      userId,
      currentPassword || '',
      updateData,
    );
    return new UserResponseDto(updatedUser);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Видалити профіль поточного користувача' })
  @ApiOkResponse({
    type: UserResponseDto,
    description: 'Видалений профіль користувача',
  })
  @UseGuards(AuthGuard)
  @Delete('profile')
  @HttpCode(HttpStatus.OK)
  async remove(@Request() req: AuthRequestDto): Promise<UserResponseDto> {
    const userId = req.user.sub;

    const deletedUser = await this.userService.delete(userId);
    return new UserResponseDto(deletedUser);
  }

  @ApiBearerAuth()
  @ApiOperation({
    summary: 'Отримати кількість пісень в історії переглядів користувача',
  })
  @ApiOkResponse({
    schema: { type: 'object', properties: { count: { type: 'number' } } },
    description: 'Кількість пісень',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @UseGuards(AuthGuard)
  @Get('history/count')
  @HttpCode(HttpStatus.OK)
  async getUserViewHistoryCount(
    @Request() req: AuthRequestDto,
    @Query('search') search?: string,
  ): Promise<{ count: number }> {
    const count = await this.userMetricsService.getUserViewHistoryCount(
      req.user.sub,
      search,
    );
    return { count };
  }

  @ApiBearerAuth()
  @ApiOperation({
    summary: 'Отримати кількість вподобаних користувачем пісень',
  })
  @ApiOkResponse({
    schema: { type: 'object', properties: { count: { type: 'number' } } },
    description: 'Кількість пісень',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @UseGuards(AuthGuard)
  @Get('likes/count')
  @HttpCode(HttpStatus.OK)
  async getUserLikedSongsCount(
    @Request() req: AuthRequestDto,
    @Query('search') search?: string,
  ): Promise<{ count: number }> {
    const count = await this.userMetricsService.getUserLikedSongsCount(
      req.user.sub,
      search,
    );
    return { count };
  }

  @ApiBearerAuth()
  @ApiOperation({
    summary: 'Отримати історію переглядів користувача',
  })
  @ApiQuery({ name: 'from', type: Number, description: 'Початковий індекс' })
  @ApiQuery({ name: 'to', type: Number, description: 'Кінцевий індекс' })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({ type: [SongHistoryItemDto], description: 'Історія переглядів' })
  @UseGuards(AuthGuard)
  @Get('history')
  @HttpCode(HttpStatus.OK)
  async getUserViewHistory(
    @Query('from') from: number,
    @Query('to') to: number,
    @Request() req: AuthRequestDto,
    @Query('search') search?: string,
  ): Promise<SongHistoryItemDto[]> {
    const history = await this.userMetricsService.getUserViewHistory(
      req.user.sub,
      from,
      to,
      search,
    );
    await Promise.all(
      history.map(async (song) => {
        const user = await this.userService.findById(song.authorId);
        song.authorLogin = user?.login || '';
      })
    );
    return history;
  }

  @ApiBearerAuth()
  @ApiOperation({
    summary: 'Отримати вподобані користувачем пісні',
  })
  @ApiQuery({ name: 'from', type: Number, description: 'Початковий індекс' })
  @ApiQuery({ name: 'to', type: Number, description: 'Кінцевий індекс' })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @ApiOkResponse({ type: [SongRankingItemDto], description: 'Вподобані пісні' })
  @UseGuards(AuthGuard)
  @Get('likes')
  @HttpCode(HttpStatus.OK)
  async getUserLikedSongs(
    @Query('from') from: number,
    @Query('to') to: number,
    @Request() req: AuthRequestDto,
    @Query('search') search?: string,
  ): Promise<SongRankingItemDto[]> {
    const songs = await this.userMetricsService.getUserLikedSongs(
      req.user.sub,
      from,
      to,
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

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Отримати плейлісти користувача' })
  @ApiOkResponse({
    type: UserPlaylistDto,
    isArray: true,
    description: 'Список плейлістів користувача',
  })
  @ApiQuery({ name: 'search', type: String, required: false, description: 'Рядок пошуку' })
  @UseGuards(AuthGuard)
  @Get('playlists')
  @HttpCode(HttpStatus.OK)
  async getUserPlaylists(
    @Request() req: AuthRequestDto,
    @Query('search') search?: string,
  ): Promise<UserPlaylistDto[]> {
    return this.userPlaylistsService.getUserPlaylists(req.user.sub, search);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Створити плейліст' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { id: { type: 'string' } } },
    description: 'Створений плейліст',
  })
  @UseGuards(AuthGuard)
  @Post('playlists')
  @HttpCode(HttpStatus.CREATED)
  async createPlaylist(
    @Request() req: AuthRequestDto,
    @Body() createPlaylistDto: CreatePlaylistDto,
  ): Promise<{ id: string }> {
    const id = await this.userPlaylistsService.createPlaylist(req.user.sub, createPlaylistDto.name);
    return { id };
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Оновити назву плейлісту' })
  @UseGuards(AuthGuard)
  @Put('playlists/:id')
  @HttpCode(HttpStatus.OK)
  async updatePlaylist(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
    @Body() updatePlaylistDto: UpdatePlaylistDto,
  ): Promise<void> {
    await this.userPlaylistsService.updatePlaylistName(id, updatePlaylistDto.name, req.user.sub);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Отримати плейліст' })
  @ApiOkResponse({ type: PlaylistResponseDto, description: 'Плейліст з піснями' })
  @UseGuards(AuthGuard)
  @Get('playlists/:id')
  @HttpCode(HttpStatus.OK)
  async getPlaylist(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<PlaylistResponseDto> {
    const playlist = await this.userPlaylistsService.getPlaylist(id, req.user.sub);
    await Promise.all(
      playlist.songs.map(async (song) => {
        if (song.authorId) {
          const author = await this.userService.findById(song.authorId);
          song.authorLogin = author?.login || 'Невідомий';
        }
      }),
    );
    return playlist;
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Видалити плейліст' })
  @UseGuards(AuthGuard)
  @Delete('playlists/:id')
  @HttpCode(HttpStatus.OK)
  async deletePlaylist(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.userPlaylistsService.deletePlaylist(id, req.user.sub);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Додати пісню до плейлісту' })
  @UseGuards(AuthGuard)
  @Post('playlists/:id/songs/:songId')
  @HttpCode(HttpStatus.OK)
  async addSongToPlaylist(
    @Param('id') id: string,
    @Param('songId') songId: string,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.userPlaylistsService.addSongToPlaylist(id, songId, req.user.sub);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Видалити пісню з плейлісту' })
  @UseGuards(AuthGuard)
  @Delete('playlists/:id/songs/:songId')
  @HttpCode(HttpStatus.OK)
  async removeSongFromPlaylist(
    @Param('id') id: string,
    @Param('songId') songId: string,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.userPlaylistsService.removeSongFromPlaylist(id, songId, req.user.sub);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Перевірити наявність пісні у вказаних плейлістах' })
  @ApiOkResponse({ description: 'Масив ключ-значення (id плейлісту: boolean)' })
  @UseGuards(AuthGuard)
  @Post('playlists/check-song/:songId')
  @HttpCode(HttpStatus.OK)
  async checkSongInPlaylists(
    @Param('songId') songId: string,
    @Body() dto: CheckSongInPlaylistsDto,
    @Request() req: AuthRequestDto,
  ): Promise<Record<string, boolean>> {
    return this.userPlaylistsService.checkSongInPlaylists(songId, dto.playlistIds, req.user.sub);
  }
}
