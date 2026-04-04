package com.group14.fic_attendance_tracker.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AttendanceRecordTest {

    @Test
    void testAttendanceRecordConstructor() {
        int mapId = 1;
        int studentId = 2;
        int seatIndex = 3;

        AttendanceRecord attendanceRecord = new AttendanceRecord(mapId, studentId, seatIndex);

        assertEquals(mapId, attendanceRecord.getMapId());
        assertEquals(studentId, attendanceRecord.getStudentId());
        assertEquals(seatIndex, attendanceRecord.getSeatIndex());
        assertEquals(false, attendanceRecord.isApproved());
        assertNotNull(attendanceRecord.getSelectedAt());
    }

    @Test
    void testSetMapId() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        int testMapId = 4;

        attendanceRecord.setMapId(testMapId);

        assertEquals(testMapId, attendanceRecord.getMapId());
    }

    @Test
    void testSetStudentId() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        int testStudentId = 5;

        attendanceRecord.setStudentId(testStudentId);

        assertEquals(testStudentId, attendanceRecord.getStudentId());
    }

    @Test
    void testSetSeatIndex() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        int testSeatIndex = 6;

        attendanceRecord.setSeatIndex(testSeatIndex);

        assertEquals(testSeatIndex, attendanceRecord.getSeatIndex());
    }

    @Test
    void testSetApproved() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();

        attendanceRecord.setApproved(true);

        assertEquals(true, attendanceRecord.isApproved());
    }

    @Test
    void testSetSelectedAt() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        LocalDateTime testSelectedAt = LocalDateTime.of(2026, 4, 1, 12, 0);

        attendanceRecord.setSelectedAt(testSelectedAt);

        assertEquals(testSelectedAt, attendanceRecord.getSelectedAt());
    }

    @Test
    void testSetId() {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setMapId(1);
        attendanceRecord.setStudentId(2);
        attendanceRecord.setSeatIndex(3);

        assertEquals(1, attendanceRecord.getMapId());
        assertEquals(2, attendanceRecord.getStudentId());
        assertEquals(3, attendanceRecord.getSeatIndex());
    }
}