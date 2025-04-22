export interface UserDTO {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

export interface UserCreateDTO {
  username: string;
  email: string;
  fullName: string;
  roles: string[];
}

export interface UserUpdateDTO {
  fullName?: string;
  avatar?: string;
}

export interface UserSearchDTO {
  username?: string;
  email?: string;
  role?: string;
  active?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface PageResponseUserDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: UserDTO[];
} 