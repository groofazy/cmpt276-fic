package com.group14.fic_attendance_tracker.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeatTest {

    @Test
    void testSeatConstructor() {
        int mapId = 1;
        int studentId = 2;
        int seatRow = 3;
        String seatNumber = "A";

        Seat seat = new Seat(mapId, studentId, seatRow, seatNumber);

        assertEquals(mapId, seat.getMapId());
        assertEquals(studentId, seat.getStudentId());
        assertEquals(seatRow, seat.getSeatRow());
        assertEquals(seatNumber, seat.getSeatNumber());
    }

    @Test
    void testSetSeatId() {
        int testSeatId = 1;
        Seat seat = new Seat();
        seat.setSeatId(testSeatId);
        assertEquals(testSeatId, seat.getSeatId());
    }

    @Test
    void testSetMapId() {
        int testMapId = 2;
        Seat seat = new Seat();
        seat.setMapId(testMapId);
        assertEquals(testMapId, seat.getMapId());
    }

    @Test 
    void testSetStudentId() {
        int testStudentId = 3;
        Seat seat = new Seat();
        seat.setStudentId(testStudentId);
        assertEquals(testStudentId, seat.getStudentId());
    }

    @Test
    void testSetSeatRow() {
        int testSeatRow = 4;
        Seat seat = new Seat();
        seat.setSeatRow(testSeatRow);
        assertEquals(testSeatRow, seat.getSeatRow());
    }

    @Test
    void testSetSeatNumber() {
        String testSeatNumber = "A";
        Seat seat = new Seat();
        seat.setSeatNumber(testSeatNumber);
        assertEquals(testSeatNumber, seat.getSeatNumber());
    }
}
