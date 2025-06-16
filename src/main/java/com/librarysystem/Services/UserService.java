package com.librarysystem.Services;

import com.librarysystem.Entities.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(String id);
    User getUserByUsername(String email);
}
