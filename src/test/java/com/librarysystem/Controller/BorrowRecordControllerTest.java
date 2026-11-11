package com.librarysystem.Controller;

import com.librarysystem.Config.SecurityConfig;
import com.librarysystem.Entities.Book;
import com.librarysystem.Entities.BorrowRecord;
import com.librarysystem.Entities.Role;
import com.librarysystem.Entities.User;
import com.librarysystem.Services.BorrowRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowRecordController.class)
@Import(SecurityConfig.class) // load the app's real security rules instead of Spring's defaults
public class BorrowRecordControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowRecordServiceImpl borrowRecordService;
    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private List<BorrowRecord> makeListOfRecord() {
        List<BorrowRecord> list = new ArrayList<>();
        for (int i =0; i < 2; i++) {
            User u = new User("u1", "Alice", "alice@gmail.com", "pass", Role.MEMBER);
            Book b = new Book("b1", "Book1", "Author", "", "", 5, 5);
            BorrowRecord br = new BorrowRecord("br1",
                    u,
                    b,
                    LocalDate.now(),
                    null);
            list.add(br);
        }

        return list;
    }
    @Test
    @WithMockUser(username = "harryl423@gmail", authorities = "ROLE_ADMIN")
    public void getAllBorrowHistory() throws Exception {
        List<BorrowRecord> brList = makeListOfRecord();
        when(borrowRecordService.getAllBorrowHistory()).thenReturn(brList);

        mockMvc.perform(get("/api/v1/borrow-records/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value("br1"))
                .andExpect(jsonPath("$.[0].user.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "harryl423@gmail", authorities = "ROLE_MEMBER")
    public void getAllBorrowHistory_return403ForMemberRequest() throws Exception {
        mockMvc.perform(get("/api/v1/borrow-records/all"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(borrowRecordService);
    }

    @Test
    @WithMockUser(value = "harryl423@gmail.com", authorities = "ROLE_MEMBER")
    public void getMyBorrowHistoryForMember() throws Exception {
        List<BorrowRecord> brList = makeListOfRecord();
        when(borrowRecordService.getUserBorrowHistory("harryl423@gmail.com"))
                .thenReturn(brList);

        mockMvc.perform(get("/api/v1/borrow-records/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value("br1"))
                .andExpect(jsonPath("$.[0].user.password").doesNotExist());
    }

    @Test
    @WithMockUser(value = "harryl423@gmail.com", authorities = "ROLE_ADMIN")
    public void getMyBorrowHistoryForAdmin() throws Exception {
        List<BorrowRecord> brList = makeListOfRecord();
        when(borrowRecordService.getUserBorrowHistory("harryl423@gmail.com"))
                .thenReturn(brList);

        mockMvc.perform(get("/api/v1/borrow-records/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value("br1"))
                .andExpect(jsonPath("$.[0].user.password").doesNotExist());
    }

    @Test
    public void getAllBorrowHistory_return401ForAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/v1/borrow-records/all"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(borrowRecordService);
    }
}
