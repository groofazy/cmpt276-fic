package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;

@Controller
public class ClassMapsController {

    @Autowired
    private ClassMapRepository mapRepo;

    @GetMapping("/maps/create")
    public String showCreateMapForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.TEACHER) {
            return "users/login";
        }
        return "maps/create";
    }

    @PostMapping("/maps/create")
    public String saveMap(
            @RequestParam("className") String className,
            @RequestParam("lectureDate") LocalDate lectureDate,
            @RequestParam("numRow") int numRow,
            HttpSession session,
            HttpServletResponse response
    ) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.TEACHER) {
            return "users/login";
        }

        int creatorId = user.getUid();
        mapRepo.save(new ClassMap(creatorId, className, lectureDate, numRow));
        response.setStatus(201);
        return "redirect:/users/teacher";
    }

    @GetMapping("/maps/view/{id}")
    public String viewMap(@PathVariable int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null) {
            return "users/login";
        }

        ClassMap classMap = mapRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid map Id:" + id));

        model.addAttribute("classMap", classMap);
        model.addAttribute("sessionUser", user);
        return "maps/mapView";
    }
}