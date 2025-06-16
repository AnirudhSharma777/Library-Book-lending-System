package com.librarysystem.ResponseDto;

public record BorrowReturnResponseDto(
        String message,
        String bookId,
        String bookTitle,
        String username
) {
}
