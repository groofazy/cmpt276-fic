package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.group14.fic_attendance_tracker.models.Seat;
import com.group14.fic_attendance_tracker.models.SeatRepository;
import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;

@ExtendWith(MockitoExtension.class)
public class SeatControllerTest {

    @Mock
    private SeatRepository seatRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private SeatController controller;

    private User studentUser() {
        User student = new User("student", "pw", User.RoleType.STUDENT);
        student.setUid(7);
        return student;
    }

    @Test
    void testCancelSeat() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", studentUser());
        MockHttpServletResponse response = new MockHttpServletResponse();

        Seat existing = new Seat(1, 7, 1, "A1");
        when(seatRepo.findByMapIdAndStudentId(1, 7)).thenReturn(existing);

        String view = controller.cancelSeat(1, session, response);

        assertThat(view).isEqualTo("redirect:/maps/view/1");
        assertThat(response.getStatus()).isEqualTo(200);
        verify(seatRepo).delete(existing);
    }

    @Test
    void testGetSeatsByMap() {
        Seat seat = new Seat(2, 7, 3, "B4");
        when(seatRepo.findByMapId(2)).thenReturn(List.of(seat));

        User student = studentUser();
        when(userRepo.findByUid(7)).thenReturn(student);

        List<Map<String, Object>> result = controller.getSeatsByMap(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("seatRow")).isEqualTo(3);
        assertThat(result.get(0).get("seatNumber")).isEqualTo("B4");
        assertThat(result.get(0).get("studentName")).isEqualTo("student");
    }

    @Test
    void testSaveSeat() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", studentUser());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(seatRepo.findByMapIdAndSeatRowAndSeatNumber(2, 3, "B4")).thenReturn(null);

        String view = controller.saveSeat(2, 3, "B4", session, response);

        assertThat(view).isEqualTo("redirect:/maps/view/2");
        assertThat(response.getStatus()).isEqualTo(201);
        verify(seatRepo).save(any(Seat.class));
    }
}
