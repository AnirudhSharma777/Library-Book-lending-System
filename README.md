# 📚 Library Book Lending System

## 🎯 Objective

This project aims to develop a secure microservice for managing a digital library. It enables users to efficiently borrow and return books, incorporating robust security features and **role-based access control**.

---

## 🔑 Key Features

### ✅ JWT-based Authentication
Secure login using JSON Web Tokens (JWTs), supporting two distinct roles:
- `ADMIN`
- `MEMBER`

### 👑 ADMIN Capabilities
- ➕ Add new books to the catalog  
- ✏️ Update existing book details  
- ❌ Delete books from the catalog  
- 📄 View a comprehensive history of all borrow/return activities  

### 🙋 MEMBER Capabilities
- 📚 Browse and view available books  
- 📥 Borrow books  
- 📤 Return previously borrowed books  

---

## 🧱 Entities

### 👤 User
- `username` (String)  
- `password` (String)  
- `role` (Enum: `ADMIN`, `MEMBER`)  

### 📘 Book
- `title` (String)  
- `author` (String)  
- `category` (String)  
- `availableCopies` (Integer)  

### 📑 BorrowRecord
- `user_id` (Foreign Key to User)  
- `book_id` (Foreign Key to Book)  
- `borrowDate` (Date)  
- `returnDate` (Date, nullable)  

---

## 📡 API Endpoints (Examples)

### 🔐 Authentication
- `POST /api/v1/auth/signup` - Register as a New Member
- `POST /api/v1/auth/login` – Authenticate user and get JWT token

### 🙋 Member Endpoints (Require `MEMBER` or `ADMIN`)
- `GET /api/v1/books` – View available books  
- `POST /api/v1/books/borrow/{bookId}` – Borrow a book  
- `POST /api/v1/books/return/{bookId}` – Return a book  

### 👑 Admin Endpoints (Require `ADMIN`)
- `POST /api/v1/books` – Add a new book  
- `PUT /api/v1/books/{bookId}` – Update book details  
- `DELETE /api/v1/books/{bookId}` – Delete a book  
- `GET /api/v1/borrow-history` – View all borrow records  

---

## 🛠️ Technical Stack

- **Java 17+**
- **Spring Boot 3+**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **H2** (for development) or **PostgreSQL** (for production)
- **JWT** (e.g., `jjwt`, `auth0`)
- **Lombok** (optional)
- **Validation annotations** for request validation

---

## 🌟 Good-to-Have Features

- ✅ **Swagger with JWT Support**  
- ✅ **BCrypt Password Encryption**  
- ✅ **Centralized Exception Handling (`@ControllerAdvice`)**  
- ✅ **Dockerization** for easy deployment  
- ✅ **Unit/Integration Tests** for core logic  

---

## 🚀 Getting Started

### 🔧 Prerequisites
- Java 17+  
- Maven  
- (Optional) Docker  

### 🏁 Setup

Clone the repo:
```bash
git clone https://github.com/AnirudhSharma777/Library-Book-lending-System.git
cd library-book-lending-system
````

Build the project:

```bash
./gradlew clean build
```

Run the application:

```bash
./gradlew bootRun
```

It will start at `http://localhost:8080`.

---

## 🗄️ Database Configuration

* **To use PostgreSQL**: Update  `application.yml`) with:

  ```
  spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
  spring.datasource.username=your_user
  spring.datasource.password=your_password
  ```

---

## 📄 API Documentation (Swagger)

Once running, access the Swagger UI here:
**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

You can authorize with JWT directly inside the Swagger interface.

---

## 🤝 Contribution

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a new branch (`feature/your-feature`)
3. Make your changes with proper testing
4. Commit with a descriptive message
5. Push and open a pull request

---

## 📜 License

This project is licensed under the **MIT License**.

