package com.schoolexam.service;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.config.SchemaInitializer;
import com.schoolexam.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @BeforeAll
    public static void setup() {
        DatabaseConfig.setDbPath("target/test_school_exam.db");
        SchemaInitializer.initialize();
    }

    @Test
    public void testSuccessfulAuthentication() {
        AuthService authService = new AuthService();
        User user = authService.authenticate("teacher@school.edu", "teacher123");
        assertNotNull(user);
        assertEquals("teacher@school.edu", user.getEmail());
    }

    @Test
    public void testFailedAuthentication() {
        AuthService authService = new AuthService();
        User user = authService.authenticate("teacher@school.edu", "wrongpassword");
        assertNull(user);
    }
}
