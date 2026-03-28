package com.group14.fic_attendance_tracker.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;



public class UserTest {

    @Test
    void testUserConstructor() {
        String name = "test";
        String password = "password";
        User.RoleType role = User.RoleType.ADMIN;

        User user = new User(name, password , role);

        assertEquals(name, user.getName());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getPassword());
    }

    @Test
    void testSetName() {
        User user = new User();
        String name = "test";
        
        user.setName(name);

        assertEquals(name, user.getName());
    }

    @Test
    void testSetPassword() {
        User user = new User();
        String password = "test";
        
        user.setPassword(password);

        assertEquals(password, user.getPassword());
    }

    @Test
    void testSetRole() {
        User user = new User();
        User.RoleType role = User.RoleType.ADMIN;
        
        user.setRole(role);

        assertEquals(role, user.getRole());
    }

    @Test
    void testSetUid() {
        User user = new User();
        String name = "test";
        
        user.setName(name);

        assertEquals(name, user.getName());
    }
}
