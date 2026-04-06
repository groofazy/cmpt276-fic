package com.group14.fic_attendance_tracker.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group14.fic_attendance_tracker.models.Course.CourseSubject;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    Course findByCourseId(int courseId);
    List<Course> findByAdminId(int adminId);
    List<Course> findBySubject(CourseSubject subject);
    List<Course> findByCourseNum(String courseNum);
    List<Course> findByCourseTimes(List<String> courseTimes);
    Course findBySubjectAndCourseNum(CourseSubject subject, String courseNum);
}
