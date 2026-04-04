package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.Course;
import com.group14.fic_attendance_tracker.models.CourseRepository;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepo;

    @GetMapping("/courses/add")
    public String showAddCoursesForm(HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "users/login";
        }

        return "users/addCourse";
    }
    
    @PostMapping("/courses/add")
    public String addCourses(@RequestParam("subject") String subject,
            @RequestParam("number") String number,
            HttpSession session,
            HttpServletResponse response
    ) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            return "users/login";
        }

        int adminId = user.getUid();
        Course.CourseSubject courseSubject = Course.CourseSubject.valueOf(subject);
        String courseNum = String.format("%03d", Integer.parseInt(number));
        courseRepo.save(new Course(adminId, courseSubject, courseNum));
        response.setStatus(201);
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/courses/edit/{id}")
    public String showEditCoursesForm(@PathVariable Long id, Model model) {
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/courses/edit/{id}")
    public String editCourses(@PathVariable Long id, @RequestParam Map<String, String> formData) {
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/courses/delete/{id}")
    public String deleteCourses(@PathVariable Long id) {
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
}
