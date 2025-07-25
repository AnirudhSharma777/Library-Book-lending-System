import { CommonModule } from '@angular/common';
import { Book } from '../../model/IClient';
import { UserService } from './../../services/user-service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-books',
  imports: [CommonModule],
  templateUrl: './books.html',
  styleUrl: './books.css'
})
export class Books implements OnInit {

  constructor(private bookService:UserService){}
  books: Book[] = [];
  ngOnInit(): void {
    this.getBooks();
  }

  getBooks(){
    this.bookService.getListOfBooks().subscribe((data:any) => {
      this.books = data;
      // console.log(this.books);

    },error => {
      console.log(error);
    })
  }
}
