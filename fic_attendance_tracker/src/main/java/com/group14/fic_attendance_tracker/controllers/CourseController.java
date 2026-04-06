package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.Course;
import com.group14.fic_attendance_tracker.models.CourseRepository;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepo;

    @GetMapping("/courses/add")
    public String showAddCoursesForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "users/login";
        }

        model.addAttribute("user", user);
        return "users/addCourse";
    }
    
    @PostMapping("/courses/add")
    public String addCourses(@RequestParam("subject") String subject,
            @RequestParam("number") String number,
            @RequestParam(value = "courseTimes", required = false) List<String> courseTimes,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "users/login";
        }

        if (courseTimes == null) {
            courseTimes = new ArrayList<>();
        }

        if (courseTimes.isEmpty()) {
            redirectAttributes.addFlashAttribute("courseTimesError", "Add at least one course time before submitting.");
            return "redirect:/courses/add";
        }

        int adminId = user.getUid();
        Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
        String courseNum = String.format("%03d", Integer.parseInt(number));

        courseRepo.save(new Course(adminId, courseSubject, courseNum, courseTimes));
        response.setStatus(201);
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/courses/edit/{id}")
    public String showEditCoursesForm(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        Course course = courseRepo.findByCourseId(id);
        if (course == null) {
            return "redirect:/admin/dashboard";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("user", user);
        return "users/editCourse";
    }

    @PostMapping("/courses/edit/{id}")
    public String editCourse(@PathVariable int id, 
            @RequestParam("subject") String subject,
            @RequestParam("number") String number,
            @RequestParam("courseTimes") List<String> courseTimes, 
            HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        Course course = courseRepo.findByCourseId(id);
        if (course == null) {
            return "redirect:/admin/dashboard";
        }

        if (courseTimes == null) {
            courseTimes = new ArrayList<>();
        }
        
        Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
        String courseNum = String.format("%03d", Integer.parseInt(number));
        course.setCourseSubject(courseSubject);
        course.setCourseNum(courseNum);
        course.setCourseTimes(courseTimes);
        
        courseRepo.save(course);
        
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/courses/delete/{id}")
    public String deleteClassroom(@PathVariable int id, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "redirect:/login";
        }
        
        Course course = courseRepo.findByCourseId(id);
        if (course != null) {
            courseRepo.deleteById(id);
        }
        
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/courses/find")
    @ResponseBody
    public List<String> getCourseNumsBySubject(@RequestParam("subject") String subject) {
        try {
            Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
            List<Course> courses = courseRepo.findBySubject(courseSubject);
            
            return courses.stream()
                          .map(Course::getCourseNum)
                          .collect(Collectors.toList());
        
        } catch (IllegalArgumentException e) {
            return List.of(); 
        }
    }

    @GetMapping("/courses/times")
    @ResponseBody
    public List<String> getCourseTimes(@RequestParam("subject") String subject, @RequestParam("number") String number) {
        try {
            Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
            
            // Find the specific course using the repository method you already have
            Course course = courseRepo.findBySubjectAndCourseNum(courseSubject, number);
            
            if (course != null && course.getCourseTimes() != null) {
                return course.getCourseTimes();
            }
            return List.of(); 
            
        } catch (IllegalArgumentException e) {
            return List.of(); 
        }
    }
}
