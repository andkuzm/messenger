package com.react_spring.messenger.repositories;

import com.react_spring.messenger.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(Long id);

    User findUsersByUsername(String username);
}
