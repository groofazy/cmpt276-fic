package com.group14.fic_attendance_tracker.models;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User implements Serializable {
    public enum RoleType {
        ADMIN,
        TEACHER,
        STUDENT
    }

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;
    private String name;
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;

    public User() {

    }
    public User(String name, String password, RoleType role) {
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
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public RoleType getRole() {
        return role;
    }
    public void setRole(RoleType role) {
        this.role = role;
    }
}
