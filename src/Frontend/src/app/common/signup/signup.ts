import { UserService } from './../../services/user-service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserRegister } from '../../model/Client';
import { Router } from '@angular/router';
import { ApiResponse } from '../../model/IClient';
import { HotToastService } from '@ngneat/hot-toast';
// import { Login } from "../login/login";

@Component({
  selector: 'app-signup',
  imports: [CommonModule, FormsModule],
  templateUrl: './signup.html',
  styleUrl: './signup.css',
})
export class Signup {


  showPassword: boolean = false;

  showPasswordHandler() {
    this.showPassword = !this.showPassword;
  }
  showModal: boolean = false;
  showHandler() {
    this.showModal = !this.showModal;
  }



  registerObj: UserRegister = new UserRegister();

  constructor(private router: Router, private userService: UserService,private toast:HotToastService) {}

  registerUser() {
    this.userService.newUserRegister(this.registerObj).subscribe((res:ApiResponse) => {
      this.toast.success("User created successfully");
      this.showModal = !this.showModal;
    },error => {
      this.toast.error("User not created");
      console.log(error);
    });
  }
}
