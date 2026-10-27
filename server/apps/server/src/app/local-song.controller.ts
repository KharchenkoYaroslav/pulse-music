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
  UseInterceptors,
  UploadedFiles,
  BadRequestException,
} from '@nestjs/common';
import { FileFieldsInterceptor } from '@nestjs/platform-express';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiConsumes,
  ApiOkResponse,
} from '@nestjs/swagger';
import { AuthGuard } from '@simple-music/auth';
import { LocalSongService } from '@simple-music/song-management';
import { AuthRequestDto } from './dto/auth/auth-request.dto';
import { UpdateSongParamsDto } from './dto/song/update-song-params.dto';
import { CreateSongDto } from './dto/song/create-song.dto';

@ApiTags('Local Songs')
@Controller('local-songs')
export class LocalSongController {
  constructor(
    private readonly localSongService: LocalSongService,
  ) {}

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Створити нову пісню' })
  @ApiConsumes('multipart/form-data')
  @ApiOkResponse({ description: 'Пісню успішно створено', schema: { type: 'object', properties: { id: { type: 'string' } } } })
  @UseGuards(AuthGuard)
  @Post()
  @HttpCode(HttpStatus.CREATED)
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'song', maxCount: 1 },
      { name: 'cover', maxCount: 1 },
    ]),
  )
  async createSong(
    @Body() params: CreateSongDto,
    @UploadedFiles() files: { song?: Express.Multer.File[]; cover?: Express.Multer.File[] },
    @Request() req: AuthRequestDto,
  ): Promise<{ id: string }> {
    const songFile = files?.song?.[0];
    const coverFile = files?.cover?.[0];

    if (songFile && !this.isValidAudioFile(songFile)) {
      throw new BadRequestException('Файл пісні має бути аудіо формату');
    }
    if (coverFile && !this.isValidImageFile(coverFile)) {
      throw new BadRequestException('Файл обкладинки має бути зображенням');
    }

    return this.localSongService.createLocalSong(req.user.sub, params, songFile, coverFile);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Оновити пісню (дані або файли)' })
  @ApiConsumes('multipart/form-data')
  @ApiOkResponse({ description: 'Пісню успішно оновлено' })
  @UseGuards(AuthGuard)
  @Patch(':id')
  @HttpCode(HttpStatus.OK)
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'song', maxCount: 1 },
      { name: 'cover', maxCount: 1 },
    ]),
  )
  async updateSong(
    @Param('id') id: string,
    @Body() params: UpdateSongParamsDto,
    @UploadedFiles() files: { song?: Express.Multer.File[]; cover?: Express.Multer.File[] },
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    const songFile = files?.song?.[0];
    const coverFile = files?.cover?.[0];

    if (songFile && !this.isValidAudioFile(songFile)) {
      throw new BadRequestException('Файл пісні має бути аудіо формату');
    }
    if (coverFile && !this.isValidImageFile(coverFile)) {
      throw new BadRequestException('Файл обкладинки має бути зображенням');
    }

    await this.localSongService.updateLocalSong(id, req.user.sub, params, songFile, coverFile);
  }

  @ApiBearerAuth()
  @ApiOperation({ summary: 'Видалити пісню' })
  @ApiOkResponse({ description: 'Пісню успішно видалено' })
  @UseGuards(AuthGuard)
  @Delete(':id')
  @HttpCode(HttpStatus.OK)
  async deleteSong(
    @Param('id') id: string,
    @Request() req: AuthRequestDto,
  ): Promise<void> {
    await this.localSongService.deleteLocalSong(id, req.user.sub);
  }

  private isValidAudioFile(file: Express.Multer.File): boolean {
    const mime = file.mimetype?.toLowerCase() || '';
    const ext = file.originalname?.toLowerCase() || '';
    return (
      mime.startsWith('audio/') ||
      /\.(mp3|wav|ogg|oga|opus|m4a|aac|flac|webm|weba)$/.test(ext)
    );
  }

  private isValidImageFile(file: Express.Multer.File): boolean {
    const mime = file.mimetype?.toLowerCase() || '';
    const ext = file.originalname?.toLowerCase() || '';
    return (
      mime.startsWith('image/') ||
      ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'application/octet-stream'].includes(mime) ||
      /\.(jpg|jpeg|jfif|pjpeg|pjp|png|apng|gif|webp|avif|heic|heif|svg|bmp|tiff|tif|ico)$/.test(ext)
    );
  }
}

