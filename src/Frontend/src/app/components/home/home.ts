import { Component } from '@angular/core';
import { Book } from '../../model/IClient';
import { UserService } from '../../services/user-service';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgClass } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [FormsModule, CommonModule, NgClass],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  searchQuery: string = '';
  selectedCategory: string = '';
  categories: string[] = ['All','Fiction', 'Non-fiction', 'Science', 'History','Biography','Programming'];

  books: Book[] = [];
  constructor(private bookService: UserService) {}

  ngOnInit(): void {
    this.getBooks();
  }

  getBooks() {
    this.bookService.getListOfBooks().subscribe(
      (data: any) => {
        this.books = data;
      },
      (error) => {
        console.log(error);
      }
    );
  }
}
