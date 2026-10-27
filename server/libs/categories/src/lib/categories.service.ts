import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { Categories, CategoriesDocument } from './categories.schema';

@Injectable()
export class CategoriesService {
  constructor(
    @InjectModel(Categories.name) private categoriesModel: Model<CategoriesDocument>,
  ) {}

  async getAllCategoriesList(): Promise<CategoriesDocument[]> {
    return this.categoriesModel.find().lean().exec();
  }

  async areCategoriesValid(categoryIds: string[]): Promise<boolean> {
    if (!categoryIds || categoryIds.length === 0) {
      return true;
    }

    const uniqueIds = [...new Set(categoryIds)];
    if (!uniqueIds.every((id) => Types.ObjectId.isValid(id))) {
      return false;
    }

    const count = await this.categoriesModel
      .countDocuments({ _id: { $in: uniqueIds } })
      .exec();

    return count === uniqueIds.length;
  }
}

