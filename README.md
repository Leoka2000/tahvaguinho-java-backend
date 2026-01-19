# Tahvago API Documentation

A  Spring Boot 17 backend implementation focusing on secure user lifecycles, JWT-based authentication, and email verification.

---

## Tech Stack

* **Java Version:** 17
* **Framework:** Spring Boot 3
* **Security:** Spring Security (JWT)
* **Database:** JPA / Hibernate (Repository Pattern)
* **Lombok:** Used for boilerplate reduction (@Getter, @Setter)

---

## Authentication Endpoints
**Base Path:** `/api/auth`

### 1. User Signup
Registers a new account. The account remains disabled (`enabled: false`) until email verification is completed.

* **URL:** `POST /signup`
* **Payload:**
    ```json
    {
      "username": "john_doe",
      "email": "john@example.com",
      "password": "securePassword123"
    }
    ```
* **Success Response:** `200 OK` with the created User object.

### 2. Account Verification
Validates the temporary verification code sent to the user's email.

* **URL:** `POST /verify`
* **Payload:**
    ```json
    {
      "email": "john@example.com",
      "verificationCode": "123456"
    }
    ```

### 3. Login & JWT Generation
Authenticates credentials and returns a Bearer token. This endpoint includes protection against brute-force attacks by tracking client IP addresses.

* **URL:** `POST /login`
* **Payload:**
    ```json
    {
      "email": "john@example.com",
      "password": "securePassword123"
    }
    ```
* **Success Response:**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1Ni...",
      "expiresIn": 3600000
    }
    ```
* **Error Responses:**
    * `429 TOO MANY REQUESTS`: Brute-force protection triggered.
    * `401 UNAUTHORIZED`: Invalid credentials.

### 4. Password Recovery

#### Forgot Password
* **URL:** `POST /forgot-password`
* **Payload:** `{"email": "user@example.com"}`

#### Reset Password
* **URL:** `POST /reset-password`
* **Payload:**
    ```json
    {
      "code": "reset-code-from-email",
      "newPassword": "newSecurePassword123"
    }
    ```

---

## 👤 User Management
**Base Path:** `/users`
**Header Required:** `Authorization: Bearer <JWT_TOKEN>`

### Get Current Profile
* **URL:** `GET /me`
* **Description:** Retrieves the profile of the currently logged-in user.

### Update Account Details
* **URL:** `PATCH /me`
* **Payload:** `{"username": "updated_name", "email": "new_email@test.com"}`
* **Behavior:** Returns a refreshed JWT token in the response header and body to reflect updated user claims.

### Change Password
* **URL:** `PATCH /me/password`
* **Payload:**
    ```json
    {
      "currentPassword": "oldPassword",
      "newPassword": "newPassword123",
      "confirmPassword": "newPassword123"
    }
    ```

---

## Core Entity: AppUser

This entity implements `UserDetails`, allowing seamless integration with Spring Security's authentication provider.

```java
package pt.tahvago.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;

    @Column(name = "verification_code")
    private String verificationCode;
    
    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;
    
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
