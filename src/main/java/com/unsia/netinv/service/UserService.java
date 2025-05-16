package com.unsia.netinv.service;


import com.unsia.netinv.entity.Users;

public interface UserService {
    Users authenticate(String username, String password);
    Users findByUsername(String username);
    Users registeruser(Users user);
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
}
