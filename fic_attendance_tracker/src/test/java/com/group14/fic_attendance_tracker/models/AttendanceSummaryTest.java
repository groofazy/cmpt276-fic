package com.group14.fic_attendance_tracker.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

public class AttendanceSummaryTest {
    
    @Test
    void testAttendanceSummaryConstructor() {
        String className = "CMPT 276";
        LocalDate lectureDate = LocalDate.of(2026, 4, 1);
        int seatNumber = 12;
        boolean approved = true;

        AttendanceSummary attendanceSummary = new AttendanceSummary(className, lectureDate, seatNumber, approved);

        assertEquals(className, attendanceSummary.getClassName());
        assertEquals(lectureDate, attendanceSummary.getLectureDate());
        assertEquals(seatNumber, attendanceSummary.getSeatNumber());
        assertEquals(approved, attendanceSummary.isApproved());
    }
}