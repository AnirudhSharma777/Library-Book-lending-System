import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user-service';
import { HotToastService } from '@ngneat/hot-toast';
import { UserLogin } from '../../model/Client';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LoginResponse } from '../../model/IClient';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  showPassword: boolean = false;
  showModal: boolean = false;
  showPasswordHandler() {
    this.showPassword = !this.showPassword;
  }

  showHandler() {
    this.showModal = !this.showModal;
  }

  loginObj: UserLogin = new UserLogin();

  constructor(
    private router: Router,
    private userService: UserService,
    private toast: HotToastService
  ) {}

  login() {
   this.userService.loginUser(this.loginObj).subscribe((res:LoginResponse) => {
    this.toast.success("Login Success");
    localStorage.setItem("loginData",JSON.stringify(res));
    this.showModal = false;
    this.router.navigate(['/home']);
   },error => {
    this.toast.error("error",error);
   });
  }
}
