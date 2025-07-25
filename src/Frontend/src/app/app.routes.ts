import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { AboutUs } from './components/about-us/about-us';
import { Admin } from './components/admin/admin';

export const routes: Routes = [
  {
    path:'',
    component:Home,
  },
  {
    path:'about',
    component:AboutUs
  },
  {
    path: 'admin',
    component:Admin
  }
];
