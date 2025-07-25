package com.librarysystem.ResponseDto;

public record BorrowReturnResponseDto(
        String message,
        String bookId,
        String imageUrl,
        String bookTitle,
        String username
) {
}
