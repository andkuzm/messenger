package com.react_spring.messenger.system.user.repository;

import com.react_spring.messenger.system.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserById(Long id);

    User findUsersByUsername(String username);
}
