package com.group14.fic_attendance_tracker.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;
import com.group14.fic_attendance_tracker.models.User;

@Controller
public class AttendanceController {

    @Autowired
    private ClassMapRepository mapRepo;

    @PostMapping("/attendance/end")
    public String endAttendance(HttpSession session) {

        User user = (User) session.getAttribute("session_user");

        if (user == null) {
            return "redirect:/login";
        }

        List<ClassMap> maps = mapRepo.findAll()
            .stream()
            .filter(map -> map.getCreatorId() == user.getUid())
            .filter(map -> map.getActive() != null && map.getActive())
            .toList();

        if (!maps.isEmpty()) {
            ClassMap activeMap = maps.get(0);
            activeMap.setActive(false);
            mapRepo.save(activeMap);
        }

        return "redirect:/users/teacher";
    }
}