package com.unsia.netinv.service;


import com.unsia.netinv.entity.Users;

public interface UserService {
    Users authenticate(String username, String password);
    Users findByUsername(String username);
}
