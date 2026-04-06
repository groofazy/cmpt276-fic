package com.group14.fic_attendance_tracker.models;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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

    @Column(name="course_enrolled")
    private String courseEnrolled;

    public User() {

    }
    public User(String name, String password, RoleType role) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.courseEnrolled = "";
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty or null");
        }
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty or null");
        }
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

    public List<String> getCourseEnrolled() {
        if (this.courseEnrolled == null || this.courseEnrolled.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(this.courseEnrolled.split(",")));
    }
    public void setCourseEnrolled(List<String> enrolledCourses) {
        if (enrolledCourses == null || enrolledCourses.isEmpty()) {
            this.courseEnrolled = "";
        } else {
            this.courseEnrolled = String.join(",", enrolledCourses);
        }
    }
}
