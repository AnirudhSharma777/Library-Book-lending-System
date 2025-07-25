export interface ApiResponse{
  id: string;
  name: string;
  email: string;
  role: string;
}

export interface LoginResponse{
  token: string;
  expiredIn:number;
  role:string;
}

export interface Book{
  id: string;
  title: string;
  author:string;
  imageUrl:string;
  category:string;
  totalCopies:number;
  availableCopies:number;
}
