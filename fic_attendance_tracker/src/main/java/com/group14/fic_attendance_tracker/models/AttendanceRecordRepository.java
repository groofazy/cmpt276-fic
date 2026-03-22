package com.group14.fic_attendance_tracker.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    Optional<AttendanceRecord> findByMapIdAndStudentId(int mapId, int studentId);
    Optional<AttendanceRecord> findByMapIdAndSeatIndex(int mapId, int seatIndex);
    List<AttendanceRecord> findByMapId(int mapId);
    List<AttendanceRecord> findByStudentIdOrderBySelectedAtDesc(int studentId);
}