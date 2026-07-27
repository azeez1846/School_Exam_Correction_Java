package com.schoolexam;

import com.schoolexam.config.SchemaInitializer;
import com.schoolexam.filter.AuthFilter;
import com.schoolexam.servlet.*;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.EnumSet;

public class MainServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portProp = System.getProperty("server.port");
        if (portProp != null && !portProp.isEmpty()) {
            port = Integer.parseInt(portProp);
        }

        System.out.println("==========================================================");
        System.out.println("🏫 Starting School Exam Correction Platform (Jetty Container)");
        System.out.println("==========================================================");

        // Initialize SQLite Database
        SchemaInitializer.initialize();

        Server server = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");

        // Web resource root
        File webappDir = new File("src/main/webapp");
        if (webappDir.exists()) {
            webapp.setResourceBase(webappDir.getAbsolutePath());
        } else {
            webapp.setResourceBase(".");
        }

        // Enable Standard Web Configurations
        webapp.setConfigurations(new org.eclipse.jetty.webapp.Configuration[] {
                new org.eclipse.jetty.webapp.WebInfConfiguration(),
                new org.eclipse.jetty.webapp.WebXmlConfiguration(),
                new org.eclipse.jetty.webapp.MetaInfConfiguration(),
                new org.eclipse.jetty.webapp.FragmentConfiguration()
        });

        // Register Servlets
        webapp.addServlet(new ServletHolder(new DashboardServlet()), "/");
        webapp.addServlet(new ServletHolder(new DashboardServlet()), "/dashboard");
        webapp.addServlet(new ServletHolder(new AuthServlet()), "/login");
        webapp.addServlet(new ServletHolder(new AuthServlet()), "/logout");
        webapp.addServlet(new ServletHolder(new AuthServlet()), "/register");
        webapp.addServlet(new ServletHolder(new AuthServlet()), "/api/auth/*");
        webapp.addServlet(new ServletHolder(new ExamServlet()), "/api/exams/*");
        webapp.addServlet(new ServletHolder(new SubmissionServlet()), "/api/submissions/*");
        webapp.addServlet(new ServletHolder(new EvaluationServlet()), "/api/evaluations/*");
        webapp.addServlet(new ServletHolder(new EvaluationServlet()), "/report-card/*");
        webapp.addServlet(new ServletHolder(new LlmConfigServlet()), "/api/llm-configs/*");

        // Register Security Filter
        FilterHolder filterHolder = new FilterHolder(new AuthFilter());
        webapp.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(webapp);

        server.start();
        System.out.println("✨ Server successfully started at: http://localhost:" + port);
        System.out.println("🔑 Default Teacher Account: teacher@school.edu / teacher123");
        System.out.println("==========================================================");

        server.join();
    }
}
