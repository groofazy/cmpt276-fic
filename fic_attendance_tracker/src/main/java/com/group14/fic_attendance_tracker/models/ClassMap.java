package com.group14.fic_attendance_tracker.models;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="maps")
public class ClassMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="map_id")
    private int mapId;

    @Column(name="creator_id")
    private int creatorId;

    @Column(name="class_name")
    private String className;

    @Column(name="class_time")
    private String classTime;

    @Column(name="lecture_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate lectureDate;

    @Column(name="num_row")
    private int numRow;

    @Column(name = "seats")
    private String seats;

    @Column(name="active")
    private Boolean active = true;

    @Column(name="passcode")
    private String passcode;

    @Column(name="attendance_open")
    private Boolean attendanceOpen = false;


    
    // Constructor for Map object
    public ClassMap() {
    }

    public ClassMap(int creatorId, String className, String classTime, LocalDate lectureDate, int numRow){
        this.creatorId = creatorId;
        this.className = className;
        this.classTime = classTime;
        this.lectureDate = lectureDate;
        this.numRow = numRow;
        this.active = true;
    }

    public int getMapId(){
        return mapId;
    }

    public void setMapId(int mapId){
        this.mapId = mapId;
    }

    public int getCreatorId(){
        return creatorId;
    }

    public void setCreatorId(int creatorId){
        this.creatorId = creatorId;
    }

    public String getClassName(){
        return className;
    }

    public void setClassName(String className){
        this.className = className;
    }

    public String getClassTime() {
        return classTime;
    }
    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    public LocalDate getLectureDate(){
        return lectureDate;
    }

    public void setLectureDate(LocalDate lectureDate){
        this.lectureDate = lectureDate;
    }

    public int getNumRow(){
        return numRow;
    }

    public void setNumRow(int numRow){
        this.numRow = numRow;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public Boolean getAttendanceOpen() {
        return attendanceOpen;
    }

    public void setAttendanceOpen(Boolean attendanceOpen) {
        this.attendanceOpen = attendanceOpen;
    }
}
