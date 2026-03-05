package com.group14.fic_attendance_tracker.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group14.fic_attendance_tracker.models.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import com.group14.fic_attendance_tracker.models.User;


@Controller
public class UsersController {

    @Autowired
    private UserRepository userRepo;

    // 
    @GetMapping("/")
    public String index() {
        return "users/index";
    }

    @GetMapping("/users/view")
    public String getAllUsers(Model model) {
        System.out.println("Getting all users");

        List<User> users = userRepo.findAll();

        model.addAttribute("us", users);
        return "users/allUsers";
    }

    @GetMapping("/users/dashboard")
    public String displayDashboard() {
        System.out.println("Displaying Dashboard");
        return "users/dashboard";
    }

    @GetMapping("/users/add")
    public String showAddForm(Model model) {
        return "users/add";
    }

    // database logic
    @PostMapping("/users/add")
    public String addUser(@RequestParam Map<String, String> newUser, HttpServletResponse response) {
        System.out.println("ADD user");
        String newName = newUser.get("name");
        String newPass = newUser.get("password");
        String newRoleStr = newUser.get("role");
        User.RoleType newRole = User.RoleType.valueOf(newRoleStr);

        userRepo.save(new User(newName, newPass, newRole));
        response.setStatus(201);
        return "redirect:/users/index";
    }

    @GetMapping("/users/login")
    public String showLoginForm(Model model) {
        return "users/login";
    }
    
}
