package com.group14.fic_attendance_tracker.controllers;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
import com.group14.fic_attendance_tracker.models.Course;
import com.group14.fic_attendance_tracker.models.CourseRepository;
import com.group14.fic_attendance_tracker.models.AttendanceRecord;
import com.group14.fic_attendance_tracker.models.AttendanceRecordRepository;
import com.group14.fic_attendance_tracker.models.AttendanceSummary;

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

    @Autowired
    private CourseRepository courseRepo;

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

        // Check if the student has enrolled in any courses
        List<String> enrolledCourseIds = user.getCourseEnrolled();
        boolean hasEnrolledCourses = enrolledCourseIds != null && !enrolledCourseIds.isEmpty();

        // Fetch all enrolled courses
        List<Course> enrolledCourses = new ArrayList<>();
        List<String> enrolledCourseNames = new ArrayList<>();
        if (hasEnrolledCourses) {
            for (String id : enrolledCourseIds) {
                try {
                    int courseId = Integer.parseInt(id);
                    Course course = courseRepo.findByCourseId(courseId);
                    if (course != null) {
                        enrolledCourses.add(course);
                        enrolledCourseNames.add(course.getCourseSubject().name() + " " + course.getCourseNum());
                    }
                } catch (NumberFormatException e) {}
            }
        }

        // Display maps of only enrolled courses
        List<ClassMap> allMaps = mapRepo.findAll()
            .stream()
            .filter(map->map.getActive() != null && map.getActive())
            .filter(map -> enrolledCourseNames.contains(map.getClassName()))
            .toList();

        List<ClassMap> upcomingMaps = allMaps.stream()
            .filter(map -> !map.getLectureDate().isBefore(LocalDate.now()))
            .filter(map -> enrolledCourseNames.contains(map.getClassName()))
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
        model.addAttribute("hasEnrolledCourses", hasEnrolledCourses);
        model.addAttribute("enrolledCourses", enrolledCourses);

        return "users/studentView";
    }

    @PostMapping("/users/student/enroll")
    public String enrollCourse(@RequestParam("subject") String subject, 
                               @RequestParam("number") String number, 
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        User sessionUser = (User) session.getAttribute("session_user");
        if (sessionUser == null || sessionUser.getRole() != User.RoleType.STUDENT) {
            return "redirect:/login";
        }

        User user = userRepo.findById(sessionUser.getUid()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Fetch the course using Subject and Number
            Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
            Course course = courseRepo.findBySubjectAndCourseNum(courseSubject, number);
            
            if (course != null) {
                String courseIdStr = String.valueOf(course.getCourseId());
                List<String> enrolledCourses = user.getCourseEnrolled();

                // Check duplicate and maximum
                // Can enroll maximum 6 courses
                if (!enrolledCourses.contains(courseIdStr) && enrolledCourses.size() < 6) {
                    enrolledCourses.add(courseIdStr);
                    user.setCourseEnrolled(enrolledCourses);
                    userRepo.save(user);
                    session.setAttribute("session_user", user);

                    redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled in " + subject + " " + number + "!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Enrollment Failed: You cannot register for more than 6 courses.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while trying to enroll.");
        }

        return "redirect:/users/student";
    }

    @PostMapping("/users/student/drop")
    public String dropCourse(@RequestParam("courseId") String courseId, 
                             HttpSession session) {
        
        User sessionUser = (User) session.getAttribute("session_user");
        if (sessionUser == null || sessionUser.getRole() != User.RoleType.STUDENT) {
            return "redirect:/login";
        }

        User user = userRepo.findById(sessionUser.getUid()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Get the current list of enrolled courses
            List<String> enrolledCourses = user.getCourseEnrolled();

            // If the course is in the list, remove it
            if (enrolledCourses.contains(courseId)) {
                enrolledCourses.remove(courseId);
                
                // Save the updated list back to the user
                user.setCourseEnrolled(enrolledCourses);
                userRepo.save(user); 
                
                // Update the active session
                session.setAttribute("session_user", user);
            }
        } catch (Exception e) {
            System.out.println("Error dropping course: " + e.getMessage());
        }

        return "redirect:/users/student";
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

        List<User> presentStudents = new ArrayList<>();

        // Find the teacher's active classroom
        ClassMap activeMap = maps.stream()
            .filter(map -> map.getActive() != null && map.getActive())
            .findFirst()
            .orElse(null);

        if (activeMap != null) {
            List<Seat> seatRecords = seatRepo.findByMapId(activeMap.getMapId());
            Set<Integer> presentIds = seatRecords.stream()
                .map(Seat::getStudentId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
            presentStudents = students.stream()
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
                return "redirect:/admin/dashboard";
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
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
            return "users/login";
        }
        else {
            // Success Login
            User user = userList.get(0); // assume name and pwd is unique
            request.getSession().setAttribute("session_user", user);
            model.addAttribute("user", user);

            if (user.getRole() == User.RoleType.ADMIN) {
                return "redirect:/admin/dashboard";
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
                return "redirect:/admin/dashboard";
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

    // ===== ADMIN ENDPOINTS =====
   @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session, 
                            @RequestParam(required = false) String classroomStatus) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        // Fetch all students
        List<User> students = userRepo.findAll()
            .stream()
            .filter(u -> u.getRole() == User.RoleType.STUDENT)
            .toList();
        
        // Fetch all professors
        List<User> professors = userRepo.findAll()
            .stream()
            .filter(u -> u.getRole() == User.RoleType.TEACHER)
            .toList();

        // Fetch all courses
        List<Course> courses = courseRepo.findAll()
            .stream()
            .toList();
        
        // Fetch all classrooms
        List<ClassMap> classrooms = mapRepo.findAll();
        // Filter classrooms based on status parameter
        if (classroomStatus != null && !classroomStatus.isEmpty() && !classroomStatus.equals("all")) {
            if (classroomStatus.equals("active")) {
                classrooms = classrooms.stream()
                    .filter(c -> c.getActive() != null && c.getActive())
                    .toList();
            } else if (classroomStatus.equals("inactive")) {
                classrooms = classrooms.stream()
                    .filter(c -> c.getActive() == null || !c.getActive())
                    .toList();
            }
        }
        
        // Add all data to model
        model.addAttribute("students", students);
        model.addAttribute("professors", professors);
        model.addAttribute("courses", courses);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("classroomStatus", classroomStatus != null ? classroomStatus : "all");
        model.addAttribute("user", user);
        
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
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/students/edit/{id}")
    public String showEditStudentForm(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        User student = userRepo.findById(id).orElse(null);
        if (student == null || student.getRole() != User.RoleType.STUDENT) {
            return "redirect:/admin/dashboard";
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
            return "redirect:/admin/dashboard";
        }
        
        student.setName(formData.get("name"));
        student.setPassword(formData.get("password"));
        
        userRepo.save(student);
        
        return "redirect:/admin/dashboard";
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
        
        return "redirect:/admin/dashboard";
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
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/professors/edit/{id}")
    public String showEditProfessorForm(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        User professor = userRepo.findById(id).orElse(null);
        if (professor == null || professor.getRole() != User.RoleType.TEACHER) {
            return "redirect:/admin/dashboard";
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
            return "redirect:/admin/dashboard";
        }
        
        professor.setName(formData.get("name"));
        professor.setPassword(formData.get("password"));
        
        userRepo.save(professor);
        
        return "redirect:/admin/dashboard";
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
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/courses")
    public String listCourses(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        // Fetch all courses
        List<Course> courses = courseRepo.findAll();
        
        model.addAttribute("courses", courses);
        model.addAttribute("user", user);
        return "users/adminView";
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
        
        String subject = formData.get("subject");
        String number = formData.get("number");
        String className = subject + " " + number;
        String classTime = formData.get("time");
        String lectureDate = formData.get("lectureDate");
        int numRow = Integer.parseInt(formData.get("numRow"));
        
        ClassMap newClassroom = new ClassMap();
        newClassroom.setClassName(className);
        newClassroom.setClassTime(classTime);
        newClassroom.setLectureDate(LocalDate.parse(lectureDate));
        newClassroom.setNumRow(numRow);
        newClassroom.setCreatorId(user.getUid());
        newClassroom.setActive(true);
        
        mapRepo.save(newClassroom);
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/classrooms/edit/{id}")
    public String showEditClassroomForm(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        ClassMap classroom = mapRepo.findById(id).orElse(null);
        if (classroom == null) {
            return "redirect:/admin/dashboard";
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
            return "redirect:/admin/dashboard";
        }
        
        String subject = formData.get("subject");
        String number = formData.get("number");
        String className = subject + " " + number;
        String classTime = formData.get("time");

        classroom.setClassName(className);
        classroom.setClassTime(classTime);
        classroom.setLectureDate(LocalDate.parse(formData.get("lectureDate")));
        classroom.setNumRow(Integer.parseInt(formData.get("numRow")));
        
        mapRepo.save(classroom);
        
        return "redirect:/admin/dashboard";
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
        
        return "redirect:/admin/dashboard";
    }
        
    @GetMapping("/admin/reports")
    public String viewReports(Model model, HttpSession session,
                            @RequestParam(required = false) String classroom,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) String professor) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        // Fetch all attendance records
        List<AttendanceRecord> records = attendanceRepo.findAll();
        
        // Create aggregated report data
        List<Map<String, Object>> reportData = new ArrayList<>();
        Map<String, Map<String, Object>> aggregated = new HashMap<>();
        
        for (AttendanceRecord record : records) {
        ClassMap classMap = mapRepo.findById(record.getMapId()).orElse(null);
        
        if (classMap != null) {  // CHECK FIRST
            User professor_user = userRepo.findById(classMap.getCreatorId()).orElse(null);
            
            if (professor_user != null) {
                String key = classMap.getClassName() + "_" + classMap.getLectureDate() + "_" + professor_user.getName();
                
                if (!aggregated.containsKey(key)) {
                    Map<String, Object> report = new HashMap<>();
                    report.put("className", classMap.getClassName());
                    report.put("lectureDate", classMap.getLectureDate());
                    report.put("professor", professor_user.getName());
                    report.put("totalStudents", 0);
                    report.put("presentStudents", 0);
                    report.put("attendanceRate", 0.0);
                    aggregated.put(key, report);
                }
                
                Map<String, Object> report = aggregated.get(key);
                int total = (int) report.get("totalStudents") + 1;
                int present = (int) report.get("presentStudents") + (record.isApproved() ? 1 : 0);
                double rate = total > 0 ? (double) present / total * 100 : 0;
                
                report.put("totalStudents", total);
                report.put("presentStudents", present);
                report.put("attendanceRate", Math.round(rate * 100.0) / 100.0);
            }
        }
    }
        
        reportData.addAll(aggregated.values());
        
        // Apply filters if provided
        if (classroom != null && !classroom.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("className").toString().equalsIgnoreCase(classroom))
                .toList();
        }
        if (professor != null && !professor.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("professor").toString().equalsIgnoreCase(professor))
                .toList();
        }
        if (date != null && !date.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("lectureDate").toString().equals(date))
                .toList();
        }
        
        // Get unique values for filter dropdowns
        List<String> classrooms = mapRepo.findAll().stream()
            .map(ClassMap::getClassName)
            .distinct()
            .toList();
        
        List<String> professors = userRepo.findAll().stream()
            .filter(u -> u.getRole() == User.RoleType.TEACHER)
            .map(User::getName)
            .distinct()
            .toList();
        
        List<String> dates = mapRepo.findAll().stream()
            .map(m -> m.getLectureDate().toString())
            .distinct()
            .toList();
        
        model.addAttribute("attendanceReports", reportData);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("professors", professors);
        model.addAttribute("dates", dates);
        model.addAttribute("user", user);
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/reports/export")
    public void exportReports(HttpServletResponse response, HttpSession session,
                            @RequestParam(required = false) String classroom,
                            @RequestParam(required = false) String date,
                            @RequestParam(required = false) String professor) throws IOException {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            response.sendRedirect("/login");
            return;
        }
        
        // Fetch and aggregate same as viewReports
        List<AttendanceRecord> records = attendanceRepo.findAll();
        List<Map<String, Object>> reportData = new ArrayList<>();
        Map<String, Map<String, Object>> aggregated = new HashMap<>();
        
        for (AttendanceRecord record : records) {
        ClassMap classMap = mapRepo.findById(record.getMapId()).orElse(null);
        
        if (classMap != null) {  // CHECK FIRST
            User professor_user = userRepo.findById(classMap.getCreatorId()).orElse(null);
            
            if (professor_user != null) {
                String key = classMap.getClassName() + "_" + classMap.getLectureDate() + "_" + professor_user.getName();
                
                if (!aggregated.containsKey(key)) {
                    Map<String, Object> report = new HashMap<>();
                    report.put("className", classMap.getClassName());
                    report.put("lectureDate", classMap.getLectureDate());
                    report.put("professor", professor_user.getName());
                    report.put("totalStudents", 0);
                    report.put("presentStudents", 0);
                    report.put("attendanceRate", 0.0);
                    aggregated.put(key, report);
                }
                
                Map<String, Object> report = aggregated.get(key);
                int total = (int) report.get("totalStudents") + 1;
                int present = (int) report.get("presentStudents") + (record.isApproved() ? 1 : 0);
                double rate = total > 0 ? (double) present / total * 100 : 0;
                
                report.put("totalStudents", total);
                report.put("presentStudents", present);
                report.put("attendanceRate", Math.round(rate * 100.0) / 100.0);
            }
        }
    }
        
        reportData.addAll(aggregated.values());
        
        // Apply filters
        if (classroom != null && !classroom.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("className").toString().equalsIgnoreCase(classroom))
                .toList();
        }
        if (professor != null && !professor.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("professor").toString().equalsIgnoreCase(professor))
                .toList();
        }
        if (date != null && !date.isEmpty()) {
            reportData = reportData.stream()
                .filter(r -> r.get("lectureDate").toString().equals(date))
                .toList();
        }
        
        // Generate CSV
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_report.csv\"");
        
        try (PrintWriter writer = response.getWriter()) {
            // Write header
            writer.println("Class Name,Lecture Date,Professor,Total Students,Present Students,Attendance Rate (%)");
            
            // Write data rows
            for (Map<String, Object> report : reportData) {
                writer.printf("%s,%s,%s,%d,%d,%.2f%n",
                    report.get("className"),
                    report.get("lectureDate"),
                    report.get("professor"),
                    report.get("totalStudents"),
                    report.get("presentStudents"),
                    report.get("attendanceRate")
                );
            }
        }
    }
    
    @GetMapping("/admin/settings")
    public String adminSettings() {
        return "redirect:/admin/dashboard";
    }
}
