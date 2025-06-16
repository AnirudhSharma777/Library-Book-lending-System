package com.librarysystem.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.librarysystem.Entities.BorrowRecord;
import com.librarysystem.Services.BorrowRecordServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/borrow-records")
@RequiredArgsConstructor
@Tag(name = "Borrow Records", description = "API for viewing borrow history")
@SecurityRequirement(name = "Bearer Authentication")
public class BorrowRecordController {

    private final BorrowRecordServiceImpl borrowRecordService;


    @Operation(summary = "View all borrow history (for ADMIN only)",
            description = "Retrieves all borrow and return records across all users.")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<BorrowRecord>> getAllBorrowHistory() {
        return ResponseEntity.ok(borrowRecordService.getAllBorrowHistory());
    }


    @Operation(summary = "View user's own borrow history (for MEMBER and ADMIN)",
            description = "Retrieves borrow and return records for the authenticated user.")
    @GetMapping("/my-history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MEMBER')")
    public ResponseEntity<List<BorrowRecord>> getMyBorrowHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(borrowRecordService.getUserBorrowHistory(username));
    }

}
