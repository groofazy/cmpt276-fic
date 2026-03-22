package com.group14.fic_attendance_tracker.controllers;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import com.group14.fic_attendance_tracker.models.AttendanceRecord;
import com.group14.fic_attendance_tracker.models.AttendanceRecordRepository;
import com.group14.fic_attendance_tracker.models.AttendanceSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;
import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;


@Controller
public class UsersController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ClassMapRepository mapRepo;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepo;
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

    // student dashboard
    @GetMapping("/users/student")
    public String displayStudentDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "redirect:/login";
        }

        List<ClassMap> allMaps = mapRepo.findAll()
            .stream()
            .sorted(Comparator.comparing(ClassMap::getLectureDate))
            .toList();

        List<ClassMap> upcomingMaps = allMaps.stream()
            .filter(map -> !map.getLectureDate().isBefore(LocalDate.now()))
            .toList();

        List<AttendanceRecord> records = attendanceRepo.findByStudentIdOrderBySelectedAtDesc(user.getUid());
        List<AttendanceSummary> attendanceHistory = new ArrayList<>();

        for (AttendanceRecord record : records) {
            mapRepo.findById(record.getMapId()).ifPresent(map ->
                attendanceHistory.add(
                    new AttendanceSummary(
                        map.getClassName(),
                        map.getLectureDate(),
                        record.getSeatIndex() + 1,
                        record.isApproved()
                    )
                )
            );
        }

        model.addAttribute("user", user);
        model.addAttribute("maps", allMaps);
        model.addAttribute("upcomingMaps", upcomingMaps);
        model.addAttribute("attendanceHistory", attendanceHistory);

        return "users/studentView";
    }

    // teacher dashboard
    @GetMapping("/users/teacher")
    public String displayTeacherDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "redirect:/login";

        }
        
        // Fetch all students
        model.addAttribute("user", user);
        List<User> students = userRepo.findAll()
            .stream()
            .filter(u -> u.getRole() == User.RoleType.STUDENT)
            .toList();
        model.addAttribute("students", students);

        // Fetch all maps created by that teacher
        List<ClassMap> maps = mapRepo.findAll()
            .stream()
            .filter(map -> map.getCreatorId() == user.getUid())
            .toList();
        model.addAttribute("maps", maps);

        return "users/teacherView";
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
                return "redirect:/users/student";
            } else if (user.getRole() == User.RoleType.ADMIN) {
                return "users/adminView";
            } else if (user.getRole() == User.RoleType.TEACHER) {
                return "redirect:/users/teacher";
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
                return "redirect:/users/teacher";
            }
            
            else if (user.getRole() == User.RoleType.STUDENT) {
                return "redirect:/users/student";
            }
                  
            else {
                return "users/protected";
            }
         }
    }

    // Back button logic (Back button in Create page and mapView page)
    @GetMapping("/back")
    public String getBack(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "users/login"; 
        }
        else {
            model.addAttribute("user", user);

            if (user.getRole() == User.RoleType.STUDENT) {
                return "redirect:/users/student";
            } else if (user.getRole() == User.RoleType.ADMIN) {
                return "users/adminView";
            } else if (user.getRole() == User.RoleType.TEACHER) {
                return "redirect:/users/teacher";
            } else {
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
    @GetMapping("/users/adminView")
    public String displayAdmin() {
        return "users/adminView";
    }
}
