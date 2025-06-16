package com.librarysystem.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookDto(
        @NotBlank(message = "Title is required")
         String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Total copies must be specified")
        @Min(value = 1, message = "Total copies must be at least 1")
        Integer totalCopies
) {
}
