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

        // ===== ADMIN ENDPOINTS =====
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("students", List.of());
        model.addAttribute("professors", List.of());
        model.addAttribute("classrooms", List.of());
        model.addAttribute("attendanceReports", List.of());
        return "users/adminView";
    }
    
    
    
    @GetMapping("/admin/professors")
    public String listProfessors(Model model) {
        model.addAttribute("professors", List.of());
        return "users/adminView";
    }

    @GetMapping("/admin/students")
public String listStudents(Model model, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    // Fetch all students (users with STUDENT role)
    List<User> students = userRepo.findAll()
        .stream()
        .filter(u -> u.getRole() == User.RoleType.STUDENT)
        .toList();
    
    model.addAttribute("students", students);
    model.addAttribute("user", user);
    return "users/adminView";
}

@GetMapping("/admin/students/add")
public String showAddStudentForm(HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    model.addAttribute("user", user);
    return "users/addStudent";
}

@PostMapping("/admin/students/add")
public String addStudent(@RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    String name = formData.get("name");
    String password = formData.get("password");
    
    User newStudent = new User(name, password, User.RoleType.STUDENT);
    userRepo.save(newStudent);
    
    return "redirect:/admin/students";
}

@GetMapping("/admin/students/edit/{id}")
public String showEditStudentForm(@PathVariable int id, HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User student = userRepo.findById(id).orElse(null);
    if (student == null || student.getRole() != User.RoleType.STUDENT) {
        return "redirect:/admin/students";
    }
    
    model.addAttribute("student", student);
    model.addAttribute("user", user);
    return "users/editStudent";
}

@PostMapping("/admin/students/edit/{id}")
public String editStudent(@PathVariable int id, @RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User student = userRepo.findById(id).orElse(null);
    if (student == null || student.getRole() != User.RoleType.STUDENT) {
        return "redirect:/admin/students";
    }
    
    student.setName(formData.get("name"));
    student.setPassword(formData.get("password"));
    
    userRepo.save(student);
    
    return "redirect:/admin/students";
}

@PostMapping("/admin/students/delete/{id}")
public String deleteStudent(@PathVariable int id, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User student = userRepo.findById(id).orElse(null);
    if (student != null && student.getRole() == User.RoleType.STUDENT) {
        userRepo.deleteById(id);
    }
    
    return "redirect:/admin/students";
}
    
    @GetMapping("/admin/professors")
public String listProfessors(Model model, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    // Fetch all professors (users with TEACHER role)
    List<User> professors = userRepo.findAll()
        .stream()
        .filter(u -> u.getRole() == User.RoleType.TEACHER)
        .toList();
    
    model.addAttribute("professors", professors);
    model.addAttribute("user", user);
    return "users/adminView";
}

@GetMapping("/admin/professors/add")
public String showAddProfessorForm(HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    model.addAttribute("user", user);
    return "users/addProfessor";
}

@PostMapping("/admin/professors/add")
public String addProfessor(@RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    String name = formData.get("name");
    String password = formData.get("password");
    
    User newProfessor = new User(name, password, User.RoleType.TEACHER);
    userRepo.save(newProfessor);
    
    return "redirect:/admin/professors";
}

@GetMapping("/admin/professors/edit/{id}")
public String showEditProfessorForm(@PathVariable int id, HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User professor = userRepo.findById(id).orElse(null);
    if (professor == null || professor.getRole() != User.RoleType.TEACHER) {
        return "redirect:/admin/professors";
    }
    
    model.addAttribute("professor", professor);
    model.addAttribute("user", user);
    return "users/editProfessor";
}

@PostMapping("/admin/professors/edit/{id}")
public String editProfessor(@PathVariable int id, @RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User professor = userRepo.findById(id).orElse(null);
    if (professor == null || professor.getRole() != User.RoleType.TEACHER) {
        return "redirect:/admin/professors";
    }
    
    professor.setName(formData.get("name"));
    professor.setPassword(formData.get("password"));
    
    userRepo.save(professor);
    
    return "redirect:/admin/professors";
}

@PostMapping("/admin/professors/delete/{id}")
public String deleteProfessor(@PathVariable int id, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    User professor = userRepo.findById(id).orElse(null);
    if (professor != null && professor.getRole() == User.RoleType.TEACHER) {
        userRepo.deleteById(id);
    }
    
    return "redirect:/admin/professors";
}

   @GetMapping("/admin/classrooms")
public String listClassrooms(Model model, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    // Fetch all classrooms (not filtered by teacher)
    List<ClassMap> classrooms = mapRepo.findAll();
    
    model.addAttribute("classrooms", classrooms);
    model.addAttribute("user", user);
    return "users/adminView";
}

@GetMapping("/admin/classrooms/add")
public String showAddClassroomForm(HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    model.addAttribute("user", user);
    return "users/addClassroom";
}

@PostMapping("/admin/classrooms/add")
public String addClassroom(@RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    String className = formData.get("className");
    String lectureDate = formData.get("lectureDate");
    int numRow = Integer.parseInt(formData.get("numRow"));
    
    ClassMap newClassroom = new ClassMap();
    newClassroom.setClassName(className);
    newClassroom.setLectureDate(LocalDate.parse(lectureDate));
    newClassroom.setNumRow(numRow);
    newClassroom.setCreatorId(user.getUid());
    newClassroom.setActive(true);
    
    mapRepo.save(newClassroom);
    
    return "redirect:/admin/classrooms";
}

@GetMapping("/admin/classrooms/edit/{id}")
public String showEditClassroomForm(@PathVariable int id, HttpSession session, Model model) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    ClassMap classroom = mapRepo.findById(id).orElse(null);
    if (classroom == null) {
        return "redirect:/admin/classrooms";
    }
    
    model.addAttribute("classroom", classroom);
    model.addAttribute("user", user);
    return "users/editClassroom";
}

@PostMapping("/admin/classrooms/edit/{id}")
public String editClassroom(@PathVariable int id, @RequestParam Map<String, String> formData, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    ClassMap classroom = mapRepo.findById(id).orElse(null);
    if (classroom == null) {
        return "redirect:/admin/classrooms";
    }
    
    classroom.setClassName(formData.get("className"));
    classroom.setLectureDate(LocalDate.parse(formData.get("lectureDate")));
    classroom.setNumRow(Integer.parseInt(formData.get("numRow")));
    
    mapRepo.save(classroom);
    
    return "redirect:/admin/classrooms";
}

@PostMapping("/admin/classrooms/delete/{id}")
public String deleteClassroom(@PathVariable int id, HttpSession session) {
    User user = (User) session.getAttribute("session_user");
    if (user == null || user.getRole() != User.RoleType.ADMIN) {
        return "redirect:/login";
    }
    
    ClassMap classroom = mapRepo.findById(id).orElse(null);
    if (classroom != null) {
        mapRepo.deleteById(id);
    }
    
    return "redirect:/admin/classrooms";
}
     
    @GetMapping("/admin/reports")
    public String viewReports(Model model,
                             @RequestParam(required = false) String classroom,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String professor) {
        model.addAttribute("attendanceReports", List.of());
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
