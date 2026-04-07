package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;
import com.group14.fic_attendance_tracker.models.Seat;
import com.group14.fic_attendance_tracker.models.SeatRepository;
import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ClassMapsControllerTest {

    @Mock
    private ClassMapRepository mapRepo;

    @Mock
    private SeatRepository seatRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ClassMapsController controller;

    private User teacherUser() {
        User teacher = new User("teacher", "pw", User.RoleType.TEACHER);
        teacher.setUid(10);
        return teacher;
    }

    private User studentUser() {
        User student = new User("student", "pw", User.RoleType.STUDENT);
        student.setUid(20);
        return student;
    }

    private String emptySeats() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 48; i++) {
            sb.append("0");
            if (i < 47) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Test
    void testDeleteMap() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        ClassMap map = new ClassMap(teacher.getUid(), "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(3);
        when(mapRepo.findById(3)).thenReturn(Optional.of(map));

        String view = controller.deleteMap(3, session);

        assertThat(view).isEqualTo("redirect:/users/teacher");
        verify(seatRepo).deleteByMapId(3);
        verify(mapRepo).delete(map);
    }

    @Test
    void testExportAttendance() throws Exception {
        ClassMap map = new ClassMap(10, "CMPT 276", "Mon 10:30", LocalDate.of(2026, 4, 1), 6);
        map.setMapId(8);
        when(mapRepo.findById(8)).thenReturn(Optional.of(map));

        Seat seat = new Seat(8, 20, 2, "B2");
        when(seatRepo.findByMapId(8)).thenReturn(List.of(seat));

        User student = studentUser();
        when(userRepo.findById(20)).thenReturn(Optional.of(student));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportAttendance(8, response);

        assertThat(response.getContentType()).isEqualTo("text/csv");
        assertThat(response.getContentAsString()).contains("student,Present");
    }

    @Test
    void testMarkAttendance() {
        User student = studentUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", student);

        ClassMap map = new ClassMap(10, "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(11);
        map.setPasscode("1234");
        map.setAttendanceOpen(true);
        map.setSeats(emptySeats());

        when(mapRepo.findById(11)).thenReturn(Optional.of(map));
        when(seatRepo.findByMapIdAndStudentId(11, student.getUid())).thenReturn(null);

        Model model = new ExtendedModelMap();
        String view = controller.markAttendance(11, "1234", session, model);

        assertThat(view).isEqualTo("maps/mapView");
        assertThat(session.getAttribute("verified_11")).isEqualTo(true);
    }

    @Test
    void testSaveMap() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String view = controller.saveMap(
            "CMPT",
            "276",
            "Mon 10:30",
            LocalDate.of(2026, 4, 6),
            6,
            "1234",
            session,
            response
        );

        assertThat(view).isEqualTo("redirect:/users/teacher");
        assertThat(response.getStatus()).isEqualTo(201);
        verify(mapRepo).save(any(ClassMap.class));
    }

    @Test
    void testSelectSeat() {
        User student = studentUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", student);

        ClassMap map = new ClassMap(10, "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(12);
        map.setSeats(emptySeats());
        when(mapRepo.findById(12)).thenReturn(Optional.of(map));

        String view = controller.selectSeat(12, 2, session, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/maps/view/12");
        verify(mapRepo).save(map);
    }

    @Test
    void testShowAttendanceScreen() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        ClassMap map = new ClassMap(teacher.getUid(), "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(13);
        when(mapRepo.findById(13)).thenReturn(Optional.of(map));

        Model model = new ExtendedModelMap();
        String view = controller.showAttendanceScreen(13, model, session);

        assertThat(view).isEqualTo("users/passcodeDisplay");
        assertThat(model.getAttribute("classMap")).isEqualTo(map);
    }

    @Test
    void testShowCreateMapForm() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        String view = controller.showCreateMapForm(new ExtendedModelMap(), session);

        assertThat(view).isEqualTo("maps/create");
    }

    @Test
    void testShowStudentPasscode() {
        User student = studentUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", student);

        ClassMap map = new ClassMap(10, "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(14);
        when(mapRepo.findById(14)).thenReturn(Optional.of(map));

        Model model = new ExtendedModelMap();
        String view = controller.showStudentPasscode(14, model, session);

        assertThat(view).isEqualTo("users/studentPasscode");
        assertThat(model.getAttribute("classMap")).isEqualTo(map);
    }

    @Test
    void testStartAttendance() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        ClassMap map = new ClassMap(teacher.getUid(), "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(15);
        map.setAttendanceOpen(false);
        when(mapRepo.findById(15)).thenReturn(Optional.of(map));

        String view = controller.startAttendance(15, new ExtendedModelMap(), session);

        assertThat(view).isEqualTo("redirect:/attendance/display/15");
        assertThat(map.getAttendanceOpen()).isTrue();
        verify(mapRepo).save(map);
    }

    @Test
    void testVerifyStudentPasscode() {
        User student = studentUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", student);

        ClassMap map = new ClassMap(10, "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(16);
        map.setPasscode("7777");
        when(mapRepo.findById(16)).thenReturn(Optional.of(map));

        String view = controller.verifyStudentPasscode(16, "7777", new ExtendedModelMap(), session);

        assertThat(view).isEqualTo("redirect:/maps/view/16");
        assertThat(session.getAttribute("verified_16")).isEqualTo(true);
    }

    @Test
    void testViewMap() {
        User teacher = teacherUser();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        ClassMap map = new ClassMap(teacher.getUid(), "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(17);
        map.setSeats(emptySeats());

        when(mapRepo.findById(17)).thenReturn(Optional.of(map));
        when(seatRepo.findByMapIdAndStudentId(17, teacher.getUid())).thenReturn(null);
        when(userRepo.findAll()).thenReturn(List.of(studentUser()));
        when(seatRepo.findByMapId(17)).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.viewMap(17, model, session);

        assertThat(view).isEqualTo("maps/mapView");
        assertThat(model.getAttribute("classMap")).isEqualTo(map);
        assertThat(model.getAttribute("sessionUser")).isEqualTo(teacher);
    }
}
