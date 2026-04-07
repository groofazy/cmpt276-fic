package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import com.group14.fic_attendance_tracker.models.ClassMap;
import com.group14.fic_attendance_tracker.models.ClassMapRepository;
import com.group14.fic_attendance_tracker.models.User;

@ExtendWith(MockitoExtension.class)
public class AttendanceControllerTest {

    @Mock
    private ClassMapRepository mapRepo;

    @InjectMocks
    private AttendanceController controller;

    @Test
    void testEndAttendance() {
        User teacher = new User("teacher", "pw", User.RoleType.TEACHER);
        teacher.setUid(9);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", teacher);

        ClassMap map = new ClassMap(9, "CMPT 276", "Mon 10:30", LocalDate.now(), 6);
        map.setMapId(4);
        map.setActive(true);
        when(mapRepo.findById(4)).thenReturn(Optional.of(map));

        String view = controller.endAttendance(4, session);

        assertThat(view).isEqualTo("redirect:/users/teacher");
        assertThat(map.getActive()).isFalse();
        verify(mapRepo).save(map);
    }
}
