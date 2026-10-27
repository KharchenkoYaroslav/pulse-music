import { Controller, Get, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiOkResponse } from '@nestjs/swagger';
import { CategoriesService } from '@simple-music/categories';
import { CategoryListResponseDto } from './dto/category/category-list-response.dto';

@ApiTags('Categories')
@Controller('categories')
export class CategoriesController {
  constructor(
    private readonly categoriesService: CategoriesService,
  ) {}

  @ApiOperation({ summary: 'Отримати список всіх категорій' })
  @ApiOkResponse({
    type: [CategoryListResponseDto],
    description: 'Список категорій',
  })
  @Get()
  @HttpCode(HttpStatus.OK)
  async getAllCategories(): Promise<CategoryListResponseDto[]> {
    const categories = await this.categoriesService.getAllCategoriesList();
    return categories.map((cat) => ({
      id: cat._id.toString(),
      title: cat.title,
    }));
  }
}
