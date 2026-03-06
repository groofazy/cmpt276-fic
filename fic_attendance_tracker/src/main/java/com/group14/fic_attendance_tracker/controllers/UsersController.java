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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

    // teacher dashboard
    @GetMapping("/users/teacher")
    public String displayTeacherDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "redirect:/login";

        }

        model.addAttribute("user", user);
        List<User> students = userRepo.findAll()
            .stream()
            .filter(u -> u.getRole() == User.RoleType.STUDENT)
            .toList();
        model.addAttribute("students", students);

        return "users/teacher-dashboard";

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
        return "redirect:/";
    }

    @GetMapping("/users/login")
    public String showLoginForm(Model model) {
        return "users/login";
    }
 
    
    // login logic
    @GetMapping("/login")
    public String getLogin(Model model, HttpServletResponse request, HttpSession session) {
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "users/login"; 
        }
        else {
        model.addAttribute("user", user);

        if (user.getRole() == User.RoleType.STUDENT) {
            return "users/studentView";
        } else if (user.getRole() == User.RoleType.ADMIN) {
            return "users/adminView";
        } else {
            return "users/protected";
        }
}

    }

    @PostMapping("/login")
    public String loginUser(@RequestParam Map<String, String> formData, Model model, HttpServletRequest request, HttpSession session) {
        // processing login
        String name = formData.get("name");
        String pwd = formData.get("password");
        List<User> userList = userRepo.findByNameAndPassword(name, pwd);
        if (userList.isEmpty()) {
            return "users/login";
        }
        else {
            // Success Login
            User user = userList.get(0); // assume name and pwd is unique
            request.getSession().setAttribute("session_user", user);
            model.addAttribute("user", user);

            if (user.getRole() == User.RoleType.ADMIN) {
                return "users/adminView";
            }

            else if (user.getRole() == User.RoleType.TEACHER) {
                return "users/teacher-dashboard";
            }
            
            else if (user.getRole() == User.RoleType.STUDENT) {
                return "users/dashboard"
                  
            else {
                return "users/protected";
            }
         }
    }

    @GetMapping("/logout")
    public String destorySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "users/index";
    }

    // route for admin view (add to routing logic for login)
    @GetMapping("/users/adminview")
    public String displayAdmin() {
        return "users/adminView";
    }
}
