import {
  Controller,
  Post,
  Patch,
  Delete,
  Param,
  Body,
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
import { ExternalSongService } from '@simple-music/song-management';
import { AuthRequestDto } from './dto/auth/auth-request.dto';
import { CreateExternalSongDto } from './dto/song/create-external-song.dto';
import { UpdateExternalSongParamsDto } from './dto/song/update-external-song-params.dto';

@ApiTags('External Songs')
@Controller('external-songs')
export class ExternalSongController {
  constructor(
    private readonly externalSongService: ExternalSongService,
  ) {}

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Створити нову зовнішню пісню' })
  @ApiOkResponse({ description: 'Пісню успішно створено', schema: { type: 'object', properties: { id: { type: 'string' } } } })
  @UseGuards(AuthGuard)
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async createSong(
    @Body() params: CreateExternalSongDto,
    @Request() req: AuthRequestDto,
  ): Promise<{ id: string }> {
    return this.externalSongService.createExternalSong(req.user.sub, params);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Оновити зовнішню пісню' })
  @ApiOkResponse({ description: 'Пісню успішно оновлено' })
  @UseGuards(AuthGuard)
  @Patch(':id')
  @HttpCode(HttpStatus.OK)
  async updateSong(
    @Param('id') id: string,
    @Body() params: UpdateExternalSongParamsDto,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.externalSongService.updateExternalSong(id, req.user.sub, params);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Видалити зовнішню пісню' })
  @ApiOkResponse({ description: 'Пісню успішно видалено' })
  @UseGuards(AuthGuard)
  @Delete(':id')
  @HttpCode(HttpStatus.OK)
  async deleteSong(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.externalSongService.deleteExternalSong(id, req.user.sub);
  }
}

