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
import com.group14.fic_attendance_tracker.models.SeatRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class ClassMapsController {

    private static final int TOTAL_SEATS = 48;
    @Autowired
    private ClassMapRepository mapRepo;

    @Autowired
    private SeatRepository seatRepo;

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
        ensureSeatsInitialized(classMap);
        String[] seatOwners = classMap.getSeats().split(",");
        String[] seatClasses = buildSeatClasses(seatOwners, user.getUid());

        model.addAttribute("classMap", classMap);

        model.addAttribute("seatClasses", seatClasses);
        model.addAttribute("currentUserId", user.getUid());

        model.addAttribute("sessionUser", user);
        Boolean verified = (Boolean) session.getAttribute("verified_" + id);

        if (verified == null) {
           verified = false;
        }

        model.addAttribute("verified", verified);
        return "maps/mapView";
    }

    //implementing generate passcode feature
    @GetMapping("/attendance/display/{id}")
    public String showAttendanceScreen(@PathVariable int id, Model model, HttpSession session) {

    User user = (User) session.getAttribute("session_user");

    if (user == null || user.getRole() != User.RoleType.TEACHER) {
        return "users/login";
    }

    ClassMap classMap = mapRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid map Id:" + id));

    model.addAttribute("classMap", classMap);
    model.addAttribute("sessionUser", user);

    return "users/passcodeDisplay"; 
}

    @PostMapping("/maps/selectSeat")
    public String selectSeat(
            @RequestParam("mapId") int mapId,
            @RequestParam("seatIndex") int seatIndex,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.STUDENT) {
            return "users/login";
        }

        ClassMap classMap = mapRepo.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid map Id:" + mapId));

        ensureSeatsInitialized(classMap);

        String[] seatOwners = classMap.getSeats().split(",");
        String currentUserId = String.valueOf(user.getUid());

        if (seatIndex < 0 || seatIndex >= TOTAL_SEATS) {
            return buildMapView(model, classMap, user.getUid(), "Invalid seat selection.", null);
        }

        String targetSeatOwner = seatOwners[seatIndex];

        // Someone else already owns this seat
        if (!"0".equals(targetSeatOwner) && !currentUserId.equals(targetSeatOwner)) {
            return buildMapView(model, classMap, user.getUid(), "That seat is already taken.", null);
        }

        // Remove this user's old seat first so they only ever have one seat per class
        for (int i = 0; i < seatOwners.length; i++) {
            if (currentUserId.equals(seatOwners[i])) {
                seatOwners[i] = "0";
            }
        }

        // Assign the new seat to this user
        seatOwners[seatIndex] = currentUserId;

        classMap.setSeats(String.join(",", seatOwners));
        mapRepo.save(classMap);

        return "redirect:/maps/view/" + mapId;
    }

    private String buildMapView(Model model, ClassMap classMap, int currentUserId, String error, String success) {
        String[] seatOwners = classMap.getSeats().split(",");
        String[] seatClasses = buildSeatClasses(seatOwners, currentUserId);

        model.addAttribute("classMap", classMap);
        model.addAttribute("seatClasses", seatClasses);
        model.addAttribute("currentUserId", currentUserId);

        if (error != null) {
            model.addAttribute("error", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }

        return "maps/mapView";
    }

    private String[] buildSeatClasses(String[] seatOwners, int currentUserId) {
        String[] seatClasses = new String[TOTAL_SEATS];
        String currentUserIdStr = String.valueOf(currentUserId);

        for (int i = 0; i < seatOwners.length && i < TOTAL_SEATS; i++) {
            if ("0".equals(seatOwners[i])) {
                seatClasses[i] = "available";
            } else if (currentUserIdStr.equals(seatOwners[i])) {
                seatClasses[i] = "mine";
            } else {
                seatClasses[i] = "occupied";
            }
        }

        // Fill any missing entries safely
        for (int i = seatOwners.length; i < TOTAL_SEATS; i++) {
            seatClasses[i] = "available";
        }

        return seatClasses;
    }

    private void ensureSeatsInitialized(ClassMap classMap) {
        if (classMap.getSeats() == null || classMap.getSeats().isBlank()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < TOTAL_SEATS; i++) {
                sb.append("0");
                if (i < TOTAL_SEATS - 1) {
                    sb.append(",");
                }
            }
            classMap.setSeats(sb.toString());
            mapRepo.save(classMap);
        }
    }

    @Transactional
    @PostMapping("/maps/delete/{id}")
    public String deleteMap(@PathVariable int id, HttpSession session) {

      User user = (User) session.getAttribute("session_user");

      if (user == null) {
          return "redirect:/login";
      }

      ClassMap map = mapRepo.findById(id).orElse(null);

      if (map != null && map.getCreatorId() == user.getUid()) {
        seatRepo.deleteByMapId(id);
        mapRepo.delete(map);
      }

      return "redirect:/users/teacher";
    }

    @PostMapping("/attendance/start/{id}")
    public String startAttendance(@PathVariable int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.TEACHER) {
            return "users/login";

        }

        ClassMap classMap = mapRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid map Id:" + id));

        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // Generate a random 6-digit code
        classMap.setPasscode(code);
        classMap.setAttendanceOpen(true);
        mapRepo.save(classMap);
        return "redirect:/attendance/display/" + id;
    }

    @PostMapping("/attendance/mark")
    public String markAttendance(
            @RequestParam("mapId") int mapId,
            @RequestParam("inputCode") String inputCode,
            HttpSession session,
            Model model
    ) {
        User user = (User) session.getAttribute("session_user");
        if (user == null || user.getRole() != User.RoleType.STUDENT) {
            return "users/login";
        }
        ClassMap classMap = mapRepo.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid map Id:" + mapId));
        
        if (classMap.getAttendanceOpen() != null && 
            classMap.getAttendanceOpen() && classMap.getPasscode() != null &&
            classMap.getPasscode().equals(inputCode)) {
            
            session.setAttribute("verified_" + mapId, true); // Mark this student as verified for this class map
            model.addAttribute("verified", true); // Mark the student as verified to show the seating chart
            model.addAttribute("success", "Please select your seat to mark attendance.");

            } else {
                session.setAttribute("verified_" + mapId, false); // Ensure student is marked as not verified
                model.addAttribute("verified", false);
                model.addAttribute("error", "Invalid passcode. Please try again.");
            }
            return viewMap(mapId, model, session);
    }
}
