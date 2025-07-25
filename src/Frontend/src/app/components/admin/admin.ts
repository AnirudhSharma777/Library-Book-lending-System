import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user-service';

@Component({
  selector: 'app-admin',
  imports: [],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin implements OnInit {

  constructor(private userService:UserService){}

  ngOnInit(): void {
    this.getUser();
  }

  users:any [] = [];

  getUser(){
    this.userService.getAllUsers().subscribe((res:any) => {
      this.users = res;
      console.log(this.users);

    },error => {
      console.log(error);

    })
  }
}
