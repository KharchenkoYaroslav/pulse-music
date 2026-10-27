import {
  Controller,
  Get,
  Post,
  Param,
  UseGuards,
  Request,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiOkResponse,
} from '@nestjs/swagger';
import { AuthGuard } from '@simple-music/auth';
import { SongService, SongMetricsService } from '@simple-music/song-management';
import { UserService } from '@simple-music/user';
import { AuthRequestDto } from './dto/auth/auth-request.dto';
import { Song } from '@simple-music/interfaces';
import { SongResponseDto } from './dto/song/song-response.dto';

@ApiTags('Song Management')
@Controller('song')
export class SongController {
  constructor(
    private readonly songService: SongService,
    private readonly songMetricsService: SongMetricsService,
    private readonly userService: UserService,
  ) {}

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Отримати детальну інформацію про пісню' })
  @ApiOkResponse({ type: SongResponseDto, description: 'Детальна інформація про пісню, якщо гість(без токену) то у `hasLiked` повертається false' })
  @Get(':id')
  @HttpCode(HttpStatus.OK)
  async getSong(@Param('id') id: string, @Request() req: { user?: { sub: string } }): Promise<Song | null> {
    const song = await this.songService.getSongById(id, req.user?.sub);
    if (song) {
      const user = await this.userService.findById(song.authorId);
      song.authorLogin = user?.login || '';
    }
    return song;
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Поставити або зняти лайк' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { liked: { type: 'boolean' } } },
    description: 'Статус лайку',
  })
  @UseGuards(AuthGuard)
  @Post(':id/toggle-like')
  @HttpCode(HttpStatus.OK)
  async toggleLike(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<{ liked: boolean }> {
    const liked = await this.songMetricsService.toggleLike(
      id,
      req.user.sub,
    );
    return { liked };
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Додати перегляд пісні' })
  @ApiOkResponse({
    schema: { type: 'object', properties: { success: { type: 'boolean' } } },
    description: 'Перегляд успішно додано',
  })
  @UseGuards(AuthGuard)
  @Post(':id/view')
  @HttpCode(HttpStatus.OK)
  async addView(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<{ success: boolean }> {
    await this.songMetricsService.addView(id, req.user.sub);
    return { success: true };
  }
}
