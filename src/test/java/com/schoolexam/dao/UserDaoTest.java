package com.schoolexam.dao;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.config.SchemaInitializer;
import com.schoolexam.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {

    @BeforeAll
    public static void setup() {
        DatabaseConfig.setDbPath("target/test_school_exam.db");
        SchemaInitializer.initialize();
    }

    @Test
    public void testFindDefaultTeacher() {
        UserDao dao = new UserDao();
        User teacher = dao.findByEmail("teacher@school.edu");
        assertNotNull(teacher, "Default teacher user should exist");
        assertEquals("Prof. Sarah Jenkins", teacher.getFullName());
    }

    @Test
    public void testCreateNewUser() {
        UserDao dao = new UserDao();
        String testEmail = "testuser_" + System.currentTimeMillis() + "@school.edu";
        User user = new User(null, testEmail, "hash123", "Test Teacher", "TEACHER", null);
        User created = dao.create(user);
        assertNotNull(created);
        assertNotNull(created.getId());

        User fetched = dao.findByEmail(testEmail);
        assertNotNull(fetched);
        assertEquals("Test Teacher", fetched.getFullName());
    }
}
