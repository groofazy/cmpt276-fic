package com.group14.fic_attendance_tracker.controllers;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.Set;
import com.group14.fic_attendance_tracker.models.AttendanceRecord;
import com.group14.fic_attendance_tracker.models.AttendanceRecordRepository;
import com.group14.fic_attendance_tracker.models.AttendanceSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;
import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;
import com.group14.fic_attendance_tracker.models.Seat;
import com.group14.fic_attendance_tracker.models.SeatRepository;


@Controller
public class UsersController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ClassMapRepository mapRepo;
    
    @Autowired
    private AttendanceRecordRepository attendanceRepo;

    @Autowired
    private SeatRepository seatRepo;
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
            .filter(map->map.getActive() != null && map.getActive())
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
            .filter(map -> map.getActive() != null && map.getActive() == true)
            .toList();
        model.addAttribute("maps", maps);

        // Get active map
        ClassMap activeMap = maps.stream()
            .filter(map -> map.getActive() != null && map.getActive())
            .findFirst()
            .orElse(null);
        model.addAttribute("activeMap", activeMap);

        if (activeMap != null) {
        List<Seat> seatRecords = seatRepo.findByMapId(activeMap.getMapId());

        Set<Integer> presentIds = seatRecords.stream()
          .map(Seat::getStudentId)
          .filter(id -> id != null)
          .collect(java.util.stream.Collectors.toSet());

        List<User> presentStudents = students.stream()
          .filter(student -> presentIds.contains(student.getUid()))
          .toList();

        model.addAttribute("presentStudents", presentStudents);
}

       

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

    // Back button logic (Back button in Create, mapView & addCourse page)
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

    // ===== ADMIN ENDPOINTS =====
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("students", List.of());
        model.addAttribute("professors", List.of());
        model.addAttribute("courses", List.of());
        model.addAttribute("classrooms", List.of());
        model.addAttribute("attendanceReports", List.of());
        return "users/adminView";
    }
    
    @GetMapping("/admin/students")
    public String listStudents(Model model) {
        model.addAttribute("students", List.of());
        return "users/adminView";
    }
    
    @GetMapping("/admin/students/add")
    public String showAddStudentForm() {
        return "users/adminView";
    }
    
    @PostMapping("/admin/students/add")
    public String addStudent(@RequestParam Map<String, String> formData) {
        return "redirect:/admin/students";
    }
    
    @GetMapping("/admin/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        model.addAttribute("students", List.of());
        return "users/adminView";
    }
    
    @PostMapping("/admin/students/edit/{id}")
    public String editStudent(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        return "redirect:/admin/students";
    }
    
    @PostMapping("/admin/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        return "redirect:/admin/students";
    }
    
    @GetMapping("/admin/professors")
    public String listProfessors(Model model) {
        model.addAttribute("professors", List.of());
        return "users/adminView";
    }
    
    @GetMapping("/admin/professors/add")
    public String showAddProfessorForm() {
        return "users/adminView";
    }
    
    @PostMapping("/admin/professors/add")
    public String addProfessor(@RequestParam Map<String, String> formData) {
        return "redirect:/admin/professors";
    }
    
    @GetMapping("/admin/professors/edit/{id}")
    public String showEditProfessorForm(@PathVariable Long id, Model model) {
        model.addAttribute("professors", List.of());
        return "users/adminView";
    }
    
    @PostMapping("/admin/professors/edit/{id}")
    public String editProfessor(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        return "redirect:/admin/professors";
    }
    
    @PostMapping("/admin/professors/delete/{id}")
    public String deleteProfessor(@PathVariable Long id) {
        return "redirect:/admin/professors";
    }

    @GetMapping("/admin/courses")
    public String listCourses(Model model) {
        return "redirect:/users/teacher";
    }
    
    @GetMapping("/admin/courses/add")
    public String showAddCoursesForm() {
        return "users/addCourse";
    }
    
    @PostMapping("/admin/courses/add")
    public String addCourses(@RequestParam Map<String, String> formData) {
        return "redirect:/users/teacher";
    }
    
    @GetMapping("/admin/courses/edit/{id}")
    public String showEditCoursesForm(@PathVariable Long id, Model model) {
        return "redirect:/users/teacher";
    }
    
    @PostMapping("/admin/courses/edit/{id}")
    public String editCourses(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        return "redirect:/users/teacher";
    }
    
    @PostMapping("/admin/courses/delete/{id}")
    public String deleteCourses(@PathVariable Long id) {
        return "redirect:/users/teacher";
    }

    @GetMapping("/admin/classrooms")
    public String listClassrooms(Model model) {
        return "redirect:/users/teacher";
    }
    
    @GetMapping("/admin/classrooms/add")
    public String showAddClassroomForm() {
        return "redirect:/users/teacher";
    }
    
    @PostMapping("/admin/classrooms/add")
    public String addClassroom(@RequestParam Map<String, String> formData) {
        return "redirect:/users/teacher";
    }
    
    @GetMapping("/admin/classrooms/edit/{id}")
    public String showEditClassroomForm(@PathVariable Long id, Model model) {
        return "redirect:/users/teacher";
    }
    
    @PostMapping("/admin/classrooms/edit/{id}")
    public String editClassroom(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        return "redirect:/users/teacher";
    }
    
    @PostMapping("/admin/classrooms/delete/{id}")
    public String deleteClassroom(@PathVariable Long id) {
        return "redirect:/users/teacher";
    }

     
    @GetMapping("/admin/reports")
    public String viewReports(Model model,
                             @RequestParam(required = false) String classroom,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String professor) {
        model.addAttribute("attendanceReports", List.of());
        model.addAttribute("courses", List.of());
        model.addAttribute("classrooms", List.of());
        model.addAttribute("professors", List.of());
        return "users/adminView";
    }
    
    @GetMapping("/admin/reports/export")
    public void exportReports(HttpServletResponse response,
                             @RequestParam(required = false) String classroom,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String professor) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");
    }
    
    @GetMapping("/admin/settings")
    public String adminSettings() {
        return "users/adminView";
    }
    
}
