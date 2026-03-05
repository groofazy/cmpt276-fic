package com.group14.fic_attendance_tracker.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByRole(String role);
    List<User> findByNameAndPassword(String name, String password);
}
