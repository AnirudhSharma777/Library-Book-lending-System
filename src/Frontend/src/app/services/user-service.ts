import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UserLogin, UserRegister } from '../model/Client';
import { Observable } from 'rxjs';
import { ApiResponse, LoginResponse } from '../model/IClient';

@Injectable({
  providedIn: 'root',
})
export class UserService {

  constructor(private http: HttpClient) {}

  newUserRegister(object:UserRegister):Observable<ApiResponse> {
    return this.http.post<ApiResponse>('http://localhost:8080/api/v1/auth/signup',object);
  }


  loginUser(object:UserLogin): Observable<LoginResponse>{
    return this.http.post<LoginResponse>('http://localhost:8080/api/v1/auth/login',object);
  }


  getListOfBooks(){
    return this.http.get('http://localhost:8080/api/v1/books');
  }


  //admin
  getAllUsers(){
    return this.http.get('http://localhost:8080/api/v1/users');
  }


}
