import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-about-us',
  imports: [CommonModule],
  templateUrl: './about-us.html',
  styleUrl: './about-us.css'
})
export class AboutUs {


  features = [
    {
      // icon: Book,
      title: "Extensive Collection",
      description: "Access thousands of books across various genres and categories, from classic literature to modern bestsellers."
    },
    {
      // icon: Clock,
      title: "24/7 Access",
      description: "Browse and manage your library account anytime, anywhere with our digital platform."
    },
    {
      // icon: Users,
      title: "Community Driven",
      description: "Connect with fellow readers, share recommendations, and participate in book clubs and discussions."
    },
    {
      // icon: Shield,
      title: "Secure & Reliable",
      description: "Your data is protected with enterprise-grade security, ensuring your privacy and account safety."
    }
  ];

  stats = [
    { number: "10,000+", label: "Books Available" },
    { number: "2,500+", label: "Active Members" },
    { number: "50+", label: "Categories" },
    { number: "98%", label: "User Satisfaction" }
  ];



}
