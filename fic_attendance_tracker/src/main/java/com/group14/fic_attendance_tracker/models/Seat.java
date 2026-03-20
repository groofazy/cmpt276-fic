package com.group14.fic_attendance_tracker.models;

import jakarta.persistence.*;

@Entity
@Table(name="seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private int seatId;

    @Column(name = "map_id")
    private int mapId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "seat_row")
    private int seatRow;

    @Column(name = "seat_number")
    private String seatNumber;

    // Constructor for Seat object
    public Seat() {
    
    }
    public Seat(int mapId, int studentId, int seatRow, String seatNumber) {
        this.mapId = mapId;
        this.studentId = studentId;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
    }

    // Getter and setter for each variable
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public int getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(int seatRow) {
        this.seatRow = seatRow;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
