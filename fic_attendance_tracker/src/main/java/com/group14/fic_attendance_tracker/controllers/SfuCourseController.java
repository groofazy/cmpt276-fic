package com.group14.fic_attendance_tracker.controllers;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.services.SfuCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/sfu")
public class SfuCourseController {
    @Autowired
    private SfuCourseService sfuCourseService;

    @GetMapping("/departments")
    @ResponseBody
    public List<String> getDepartments(HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return sfuCourseService.getDepartments();
    }

    @GetMapping("/course-numbers")
    @ResponseBody
    public List<String> getCourseNumbers(@RequestParam("department") String department, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return sfuCourseService.getCourseNumbers(department);
    }
}