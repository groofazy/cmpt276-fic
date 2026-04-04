package com.group14.fic_attendance_tracker.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

public class ClassMapTest {

    @Test
    void testClassMapConstructor() {
        int creatorId = 1;
        String className = "CMPT 276";
        LocalDate lectureDate = LocalDate.of(2026, 3, 31);
        int numRow = 5;

        ClassMap classMap = new ClassMap(creatorId, className, lectureDate, numRow);

        assertEquals(creatorId, classMap.getCreatorId());
        assertEquals(className, classMap.getClassName());
        assertEquals(lectureDate, classMap.getLectureDate());
        assertEquals(numRow, classMap.getNumRow());
        assertEquals(true, classMap.getActive());
    }

    @Test
    void testSetMapId() {
        ClassMap classMap = new ClassMap();
        int testMapId = 1;
        classMap.setMapId(testMapId);
        assertEquals(testMapId, classMap.getMapId());
    }

    @Test
    void testSetCreatorId() {
        ClassMap classMap = new ClassMap();
        int testCreatorId = 2;
        classMap.setCreatorId(testCreatorId);
        assertEquals(testCreatorId, classMap.getCreatorId());
    }

    @Test
    void testSetClassName() {
        ClassMap classMap = new ClassMap();
        String testClassName = "CMPT 276";
        classMap.setClassName(testClassName);
        assertEquals(testClassName, classMap.getClassName());
    }

    @Test
    void testSetLectureDate() {
        ClassMap classMap = new ClassMap();
        LocalDate testDate = LocalDate.of(2026, 3, 31);
        classMap.setLectureDate(testDate);
        assertEquals(testDate, classMap.getLectureDate());
    }

    @Test
    void testSetNumRow() {
        ClassMap classMap = new ClassMap();
        int testNumRow = 5;
        classMap.setNumRow(testNumRow);
        assertEquals(testNumRow, classMap.getNumRow());
    }

    @Test
    void testSetSeats() {
        ClassMap classMap = new ClassMap();
        String testSeats = "A1,A2,B1,B2";
        classMap.setSeats(testSeats);
        assertEquals(testSeats, classMap.getSeats());
    }

    @Test
    void testSetActive() {
        ClassMap classMap = new ClassMap();
        classMap.setActive(false);
        assertEquals(false, classMap.getActive());
    }
}