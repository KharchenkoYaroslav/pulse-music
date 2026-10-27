export interface AuthUser {
  sub: string;
}

export interface AuthRequest {
  user: AuthUser;
}

export interface RefreshUser {
  sub: string;
  refreshToken: string;
}

export interface RefreshRequest {
  user: RefreshUser;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface UserProfile {
  login: string;
  createdAt: Date;
}

export interface UserLogin {
  login: string;
}
