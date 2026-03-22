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

    @Column(name="lecture_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate lectureDate;

    @Column(name="num_row")
    private int numRow;

    @Column(name="seats", length = 3000)
    private String seats;

    public ClassMap() {
    }

    public ClassMap(int creatorId, String className, LocalDate lectureDate, int numRow){
        this.creatorId = creatorId;
        this.className = className;
        this.lectureDate = lectureDate;
        this.numRow = numRow;
        this.seats = generateEmptySeats();
    }

    private String generateEmptySeats() {
        int totalSeats = 48; // 4 rows × 4 desks × 3 seats
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < totalSeats; i++) {
            sb.append("0");
            if (i < totalSeats - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
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
}