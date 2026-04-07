package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.group14.fic_attendance_tracker.models.Course;
import com.group14.fic_attendance_tracker.models.CourseRepository;
import com.group14.fic_attendance_tracker.models.User;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @Mock
    private CourseRepository courseRepo;

    @InjectMocks
    private CourseController controller;

    private User adminUser() {
        User admin = new User("admin", "pw", User.RoleType.ADMIN);
        admin.setUid(1);
        return admin;
    }

    @Test
    void testAddCourses() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", adminUser());
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.addCourses(
            "CMPT",
            "276",
            List.of("Mon 10:30-12:20"),
            session,
            response,
            redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        assertThat(response.getStatus()).isEqualTo(201);
        verify(courseRepo).save(any(Course.class));
    }

    @Test
    void testDeleteClassroom() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", adminUser());
        Course course = new Course();
        course.setCourseId(3);
        when(courseRepo.findByCourseId(3)).thenReturn(course);

        String view = controller.deleteClassroom(3, session);

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        verify(courseRepo).deleteById(3);
    }

    @Test
    void testEditCourse() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", adminUser());
        Course course = new Course(1, Course.CourseSubject.CMPT, "120", List.of("Mon 08:30"));
        when(courseRepo.findByCourseId(10)).thenReturn(course);

        String view = controller.editCourse(
            10,
            "CMPT",
            "276",
            List.of("Tue 12:30-14:20"),
            session
        );

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        assertThat(course.getCourseSubject()).isEqualTo(Course.CourseSubject.CMPT);
        assertThat(course.getCourseNum()).isEqualTo("276");
        verify(courseRepo).save(course);
    }

    @Test
    void testGetAvailableSubjects() {
        Course c1 = new Course(1, Course.CourseSubject.CMPT, "276", List.of("Mon 10:30"));
        Course c2 = new Course(1, Course.CourseSubject.BISC, "101", List.of("Tue 09:30"));
        Course c3 = new Course(1, Course.CourseSubject.CMPT, "225", List.of("Wed 13:30"));
        when(courseRepo.findAll()).thenReturn(List.of(c1, c2, c3));

        List<String> subjects = controller.getAvailableSubjects();

        assertThat(subjects).containsExactly("BISC", "CMPT");
    }

    @Test
    void testGetCourseNumsBySubject() {
        Course c1 = new Course(1, Course.CourseSubject.CMPT, "120", List.of("Mon 08:30"));
        Course c2 = new Course(1, Course.CourseSubject.CMPT, "276", List.of("Mon 10:30"));
        when(courseRepo.findBySubject(Course.CourseSubject.CMPT)).thenReturn(List.of(c1, c2));

        List<String> nums = controller.getCourseNumsBySubject("CMPT");

        assertThat(nums).containsExactly("120", "276");
    }

    @Test
    void testGetCourseTimes() {
        Course c = new Course(1, Course.CourseSubject.CMPT, "276", List.of("Mon 10:30", "Wed 10:30"));
        when(courseRepo.findBySubjectAndCourseNum(Course.CourseSubject.CMPT, "276")).thenReturn(c);

        List<String> times = controller.getCourseTimes("CMPT", "276");

        assertThat(times).containsExactly("Mon 10:30", "Wed 10:30");
    }

    @Test
    void testShowAddCoursesForm() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", adminUser());
        Model model = new ExtendedModelMap();

        String view = controller.showAddCoursesForm(session, model);

        assertThat(view).isEqualTo("users/addCourse");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    void testShowEditCoursesForm() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", adminUser());
        Model model = new ExtendedModelMap();

        Course course = new Course(1, Course.CourseSubject.CMPT, "276", List.of("Mon 10:30"));
        when(courseRepo.findByCourseId(5)).thenReturn(course);

        String view = controller.showEditCoursesForm(5, session, model);

        assertThat(view).isEqualTo("users/editCourse");
        assertThat(model.getAttribute("course")).isEqualTo(course);
        assertThat(model.getAttribute("user")).isNotNull();
    }
}
