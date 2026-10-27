import { ApiProperty } from '@nestjs/swagger';
import { Exclude, Expose } from 'class-transformer';
import { UserProfile } from '@simple-music/interfaces';
import { User } from '@simple-music/user';

@Exclude()
export class UserResponseDto implements UserProfile {
  @ApiProperty({ description: 'Логін користувача' })
  @Expose()
  login!: string;

  @ApiProperty({ description: 'Дата створення облікового запису' })
  @Expose()
  createdAt!: Date;

  constructor(partial: Partial<User>) {
    Object.assign(this, partial);
  }
}
