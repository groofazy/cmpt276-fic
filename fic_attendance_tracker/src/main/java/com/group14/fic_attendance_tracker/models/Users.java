package com.group14.fic_attendance_tracker.models;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class Users {
    public enum RoleType {
        ADMIN,
        TEACHER,
        STUDENT
    }

    private String name;
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    public Users() {

    }
    public Users(String name, String password, RoleType role) {
        this.name = name;
        this.password = password;
        this.role = role;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
