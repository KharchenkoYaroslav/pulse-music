import { Module } from '@nestjs/common';
import { CategoriesService } from './categories.service';
import { Categories, CategoriesSchema } from './categories.schema';
import { MongooseModule } from '@nestjs/mongoose';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Categories.name, schema: CategoriesSchema },
    ]),
  ],
  controllers: [],
  providers: [
    CategoriesService,
  ],
  exports: [
    CategoriesService,
  ],
})
export class CategoriesModule {}
