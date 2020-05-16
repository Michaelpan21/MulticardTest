package ru.multicard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.multicard.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
