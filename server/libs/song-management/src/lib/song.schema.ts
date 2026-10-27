import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type SongDocument = Song & Document & {
  createdAt: Date;
  updatedAt: Date;
};

@Schema({ timestamps: true, collection: 'songs' })
export class Song {
  @Prop({ type: String, required: true, index: true })
  title!: string;

  @Prop({ type: String, index: true })
  originalAuthor?: string;

  @Prop({ default: '' })
  description!: string;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true, index: true })
  authorId!: Types.ObjectId;

  @Prop({ type: [{ type: Types.ObjectId, ref: 'Category' }], default: [] })
  categoryIds!: Types.ObjectId[];

  @Prop({ type: String, default: null })
  song!: string | null;

  @Prop({ type: String, default: null })
  cover!: string | null;
}

export const SongSchema = SchemaFactory.createForClass(Song);
