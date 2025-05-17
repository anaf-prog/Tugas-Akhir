package com.unsia.netinv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

    Users findByUsernameAndPassword(String username, String password);

    Users findByUsername(String username);

    Users findByEmail(String email);

    List<Users> findByRole(String role);
    
}
