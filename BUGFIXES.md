# Bug Fixes

Bugs found while adding the automated test suite (JUnit 5, Mockito, MockMvc, H2 integration tests).
Most were found by writing a test for the expected behavior, watching it fail, then fixing the code.

## Fixed

| # | Bug | Before | After | Fix | Proven by |
|---|---|---|---|---|---|
| 1 | Signup hid the "email already used" error | Signing up with an existing email returned the generic "Registration failed. Please try again later." | `400` "Account already exists using this Email" | `AuthServiceImpl.signup`: moved the email check out of the `try/catch` that wrapped every exception in `SignupFailedException` | `AuthServiceImplTest.signup_throwExceptionAccountAlreadyExist`, `AuthenticationControllerTest.signup_returns400WhenEmailExists` |
| 2 | Login revealed which emails are registered (user enumeration) | Unknown email returned "User not Found"; wrong password returned "Bad credentials" | Both return the identical `401` "Authentication Failed: Bad credentials" | `AuthServiceImpl.login`: removed its own `findByEmail` lookup and `try/catch`; `AuthenticationManager` reports both cases as `BadCredentialsException` | `AuthServiceImplTest.login_throwsSameErrorWhenUserNotFound`, `AuthIT.login_unknownEmailAndWrongPassword_returnSame401` |
| 3 | Anyone could sign up as ADMIN | `"role": "admin"` in the signup request created an admin account | `400` with a `role` validation error; no account is created | `RegisterDto.role`: `@Pattern(regexp = "(?i)member")` and `@NotBlank`. This also turns unknown roles (e.g. `"boss"`) and a missing role into a clear `400` | `AuthIT.signup_cannotCreateAdminAccount` |
| 4 | Logged-in user with the wrong role got a server error | Member calling an admin endpoint got `500` | `403` "You do not have permission to perform this action" | `GlobalExceptionHandler`: handler for `AccessDeniedException` (thrown by `@PreAuthorize`) | `BookControllerTest.deleteBook_returns403ForMember`, `UserControllerTest`, `BorrowRecordControllerTest` |
| 5 | Request with no login got the wrong status | Anonymous request to a protected endpoint got `403` | `401` | `SecurityConfig`: `authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))` | `BookControllerTest.deleteBook_returns401WithoutLogin`, `TokenSecurityIT.protectedEndpoint_returns401WithoutToken` |
| 6 | Broken or expired JWT caused a server error | Garbage, tampered or expired token returned `500` (e.g. "Malformed JWT JSON") | `401` "Invalid or expired token" | `GlobalExceptionHandler`: handler for jjwt's `JwtException`, with a fixed message so jjwt's internal details aren't sent to the client | `TokenSecurityIT.protectedEndpoint_returns401WithGarbageToken`, `...WithExpiredToken` |
| 7 | Password hashes were sent to clients | Borrow history and user endpoints returned the `User` entity, including the BCrypt hash (`"password": "$2a$..."`) | `password` is never in a response | `User.password`: `@JsonIgnore` | `BorrowIT.borrowHistory_doesNotExposePassword`, `UserControllerTest.getUserByEmail_returnUserWithOk`, `BorrowRecordControllerTest` |
| 8 | Borrow and return were not atomic | If saving the borrow record failed, the book's available copies had already changed | The copy count and the borrow record are saved together or not at all | `BorrowRecordServiceImpl.borrowBook` / `returnBook`: `@Transactional` | No test yet. A rollback test (make the record save fail, check copies are unchanged) is planned |

Also added as a regression guard, not a bug: a valid token for a deleted user returns `401`
(`TokenSecurityIT.protectedEndpoint_returns401WithValidTokenForDeletedUser`).

