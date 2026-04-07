package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.group14.fic_attendance_tracker.models.User;
import com.group14.fic_attendance_tracker.models.UserRepository;


@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {

    @Autowired
    private MockMvc MockMvc;

    @Autowired
    private UsersController controller;

    @MockitoBean
    private UserRepository userRepo;

    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    // REGISTRATION USER STORY INTEGRATION TEST
    @Test
    void registerStudentSuccess() throws Exception {
        String username = "student_reg_test_user";
        String password = "student_reg_test_pass";
        User savedUser = new User(username, password, User.RoleType.STUDENT);
        savedUser.setUid(100);

        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        MockMvc.perform(post("/users/add")
            .param("name", username)
            .param("password", password)
            .param("role", User.RoleType.STUDENT.name()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        verify(userRepo).save(argThat(user ->
            username.equals(user.getName())
            && password.equals(user.getPassword())
            && user.getRole() == User.RoleType.STUDENT
        ));
    }


    // STUDENT USER STORY LOGIN INTEGRATION TEST
    @Test
    void studentLoginSuccessRedirectsToStudentDashboard() throws Exception {
        User student = new User("student_login_test_user", "student_login_test_pass", User.RoleType.STUDENT);
        List<User> expectedUsers = List.of(student);
        when(userRepo.findByNameAndPassword(student.getName(), student.getPassword())).thenReturn(expectedUsers);

        MockMvc.perform(post("/login")
            .param("name", student.getName())
            .param("password", student.getPassword()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/student"));

        verify(userRepo).findByNameAndPassword(student.getName(), student.getPassword());
    }

    
    
    


}
