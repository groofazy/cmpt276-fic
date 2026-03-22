package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;
import com.group14.fic_attendance_tracker.models.User;

@Controller
public class AttendanceController {

    @Autowired
    private ClassMapRepository mapRepo;

    @PostMapping("/attendance/end/{id}")
    public String endAttendance(@PathVariable int id, HttpSession session) {

        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "redirect:/login";
        }

        ClassMap map = mapRepo.findById(id).orElse(null);

        if (map != null && map.getCreatorId() == user.getUid()) {
            map.setActive(false);
            mapRepo.save(map);
        }

        return "redirect:/users/teacher";
    }
}