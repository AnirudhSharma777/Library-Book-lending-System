package com.librarysystem.Config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Library Book Lending System API",
                version = "1.0",
                description = "API documentation for the Library Book Lending Microservice. " +
                        "Supports user authentication with JWT, role-based access control (ADMIN, MEMBER), " +
                        "and operations for managing books and borrow records.",
                contact = @Contact(
                        name = "Your Name",
                        email = "your.email@example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Provide a JWT token in the 'Bearer ' format. " +
                "Example: `Bearer eyJhbGciOiJIUzI1NiJ9...`"
)
public class OpenApiConfig {
}
