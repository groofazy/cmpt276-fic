package com.group14.fic_attendance_tracker.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findByMapId(int mapId);

    List<Seat> findByStudentId(int studentId);

    Seat findByMapIdAndSeatRowAndSeatNumber(int mapId, int seatRow, String seatNumber);

    void deleteByMapId(int mapId);
}
