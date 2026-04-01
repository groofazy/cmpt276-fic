package com.group14.fic_attendance_tracker.controllers;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {

    @Autowired
    private MockMvc MockMvc;

    @Autowired
    private UsersController controller;

    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }


    @Test
    void testAddClassroom() {

    }

    @Test
    void testAddProfessor() {

    }

    @Test
    void testAddStudent() {

    }

    @Test
    void testAddUser() {

    }

    @Test
    void testAdminDashboard() {

    }

    @Test
    void testAdminSettings() {

    }

    @Test
    void testDeleteClassroom() {

    }

    @Test
    void testDeleteProfessor() {

    }

    @Test
    void testDeleteStudent() {

    }

    @Test
    void testDestorySession() {

    }

    @Test
    void testDisplayAdmin() {

    }

    @Test
    void testDisplayDashboard() {

    }

    @Test
    void testDisplayStudentDashboard() {

    }

    @Test
    void testDisplayTeacherDashboard() {

    }

    @Test
    void testEditClassroom() {

    }

    @Test
    void testEditProfessor() {

    }

    @Test
    void testEditStudent() {

    }

    @Test
    void testExportReports() {

    }

    @Test
    void testGetAllUsers() {

    }

    @Test
    void testGetBack() {

    }

    @Test
    void testGetLogin() {

    }

    @Test
    void testIndex() {

    }

    @Test
    void testListClassrooms() {

    }

    @Test
    void testListProfessors() {

    }

    @Test
    void testListStudents() {

    }

    @Test
    void testLoginUser() {

    }

    @Test
    void testShowAddClassroomForm() {

    }

    @Test
    void testShowAddForm() {

    }

    @Test
    void testShowAddProfessorForm() {

    }

    @Test
    void testShowAddStudentForm() {

    }

    @Test
    void testShowEditClassroomForm() {

    }

    @Test
    void testShowEditProfessorForm() {

    }

    @Test
    void testShowEditStudentForm() {

    }

    @Test
    void testShowLoginForm() {

    }

    @Test
    void testViewReports() {

    }
}
