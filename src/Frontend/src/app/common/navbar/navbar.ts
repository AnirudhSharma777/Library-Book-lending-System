import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Signup } from '../signup/signup';
import { CommonModule, NgClass } from '@angular/common';
import { Login } from '../login/login';
import { Contact } from '../../components/contact/contact';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, Signup, NgClass, Login, Contact,CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar  {
  isMenuOpen = false;

  role: string = '';
  token: string = '';

  constructor() {
    const data = localStorage.getItem('loginData');
    if (data) {
      try {
        const parsed = JSON.parse(data);
        this.role = parsed.role;
        this.token = parsed.token;
      } catch (e) {
        console.error('Error parsing login data:', e);
      }
    }
  }


  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logOut() {
    this.role = '';
    this.token = '';
    localStorage.removeItem('loginData');
  }
}
