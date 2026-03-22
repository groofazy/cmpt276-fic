package com.group14.fic_attendance_tracker.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "attendance_records",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"map_id", "student_id"}),
        @UniqueConstraint(columnNames = {"map_id", "seat_index"})
    }
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "map_id", nullable = false)
    private int mapId;

    @Column(name = "student_id", nullable = false)
    private int studentId;

    @Column(name = "seat_index", nullable = false)
    private int seatIndex;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt = LocalDateTime.now();

    public AttendanceRecord() {
    }

    public AttendanceRecord(int mapId, int studentId, int seatIndex) {
        this.mapId = mapId;
        this.studentId = studentId;
        this.seatIndex = seatIndex;
        this.approved = false;
        this.selectedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }

    public void setSelectedAt(LocalDateTime selectedAt) {
        this.selectedAt = selectedAt;
    }
}