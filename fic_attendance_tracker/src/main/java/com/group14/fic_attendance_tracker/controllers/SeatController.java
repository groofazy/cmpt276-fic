package com.group14.fic_attendance_tracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;
import com.group14.fic_attendance_tracker.models.Seat;
import com.group14.fic_attendance_tracker.models.SeatRepository;

@Controller
public class SeatController {

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/seat/load/{mapId}")
    @ResponseBody
    public List<Map<String, Object>> getSeatsByMap(@PathVariable int mapId) {
        List<Seat> seats = seatRepo.findByMapId(mapId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        // Extract seat row, seat number and student Id for each seat
        for (Seat seat : seats) {
            Map<String, Object> seatData = new HashMap<>();
            seatData.put("seatRow", seat.getSeatRow());
            seatData.put("seatNumber", seat.getSeatNumber());
            seatData.put("studentId", seat.getStudentId());

            // Look for the student taken that seat
            if (seat.getStudentId() != null) {
                User student = userRepo.findByUid(seat.getStudentId());
                if (student != null) {
                    seatData.put("studentName", student.getName());
                } else {
                    seatData.put("studentName", "Unknown Student");
                }
            } else {
                seatData.put("studentName", null);
            }

            response.add(seatData);
        }
        
        return response; 
    }

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

        int studentId = user.getUid();
        
        // Check if that seat already in Database
        Seat existing = seatRepo.findByMapIdAndSeatRowAndSeatNumber(mapId, seatRow, seatNumber);
        if (existing != null) {
            if (existing.getStudentId() != null && 
                !existing.getStudentId().equals(studentId)) {
                    response.setStatus(409);
                    return "redirect:/maps/view/" + mapId;
            }
        } 
        
        else {
            seatRepo.save(new Seat(mapId, studentId, seatRow, seatNumber));
        }
        
        response.setStatus(201);
        return "redirect:/maps/view/" + mapId;
    }
}
