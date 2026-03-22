package com.group14.fic_attendance_tracker.models;

import java.time.LocalDate;

public class AttendanceSummary {
    private String className;
    private LocalDate lectureDate;
    private int seatNumber;
    private boolean approved;

    public AttendanceSummary(String className, LocalDate lectureDate, int seatNumber, boolean approved) {
        this.className = className;
        this.lectureDate = lectureDate;
        this.seatNumber = seatNumber;
        this.approved = approved;
    }

    public String getClassName() {
        return className;
    }

    public LocalDate getLectureDate() {
        return lectureDate;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isApproved() {
        return approved;
    }
}