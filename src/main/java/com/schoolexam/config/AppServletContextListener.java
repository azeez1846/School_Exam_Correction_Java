package com.schoolexam.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("🚀 Initializing School Exam Correction Database Schema...");
        SchemaInitializer.initialize();
        System.out.println("✅ SQLite Database Ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("👋 Shutting down School Exam Correction Server.");
    }
}
