package com.group14.fic_attendance_tracker.models;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassMapRepository extends JpaRepository<ClassMap, Integer> {
    List<ClassMap> findByCreatorId(int creatorId);

    List<ClassMap> findByClassName(String className);

    List<ClassMap> findByLectureDate(LocalDate lectureDate);

    ClassMap findByClassNameAndLectureDate(String className, LocalDate lectureDate);
}
