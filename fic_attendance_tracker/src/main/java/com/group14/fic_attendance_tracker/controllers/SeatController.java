package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.Seat;
import com.group14.fic_attendance_tracker.models.SeatRepository;

@Controller
public class SeatController {

    @Autowired
    private SeatRepository seatRepo;

    // Database logic
    @PostMapping("/seat/save")
    public String saveSeat(
            @RequestParam("mapId") int mapId,
            @RequestParam("seatRow") int seatRow, 
            @RequestParam("seatNumber") String seatNumber,
            HttpSession session, 
            HttpServletResponse response
    ) {
        System.out.println("SAVE Seat");

        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.STUDENT) {
            return "users/login"; 
        }
        else {
            int studentId = user.getUid();
            seatRepo.save(new Seat(mapId, studentId, seatRow, seatNumber));
            response.setStatus(201);
            return "redirect:/maps/view/{mapId}";
        }
    }
}
