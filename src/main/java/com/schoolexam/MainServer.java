package com.schoolexam;

import com.schoolexam.config.SchemaInitializer;
import com.schoolexam.filter.AuthFilter;
import com.schoolexam.servlet.*;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
        System.out.println("🏫 Starting School Exam Correction Platform (Embedded Jetty)");
        System.out.println("==========================================================");

        // Initialize SQLite Database
        SchemaInitializer.initialize();

        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        File webappDir = new File("src/main/webapp");
        if (webappDir.exists()) {
            context.setResourceBase(webappDir.getAbsolutePath());
        } else {
            context.setResourceBase(".");
        }

        // Register Security Filter
        FilterHolder filterHolder = new FilterHolder(new AuthFilter());
        context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        // Register Servlets
        context.addServlet(new ServletHolder(new DashboardServlet()), "/");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/dashboard");
        context.addServlet(new ServletHolder(new AuthServlet()), "/login");
        context.addServlet(new ServletHolder(new AuthServlet()), "/logout");
        context.addServlet(new ServletHolder(new AuthServlet()), "/register");
        context.addServlet(new ServletHolder(new AuthServlet()), "/api/auth/*");
        context.addServlet(new ServletHolder(new ExamServlet()), "/api/exams/*");
        context.addServlet(new ServletHolder(new SubmissionServlet()), "/api/submissions/*");
        context.addServlet(new ServletHolder(new EvaluationServlet()), "/api/evaluations/*");
        context.addServlet(new ServletHolder(new EvaluationServlet()), "/report-card/*");
        context.addServlet(new ServletHolder(new LlmConfigServlet()), "/api/llm-configs/*");

        // Default Servlet for static assets (CSS, JS, images)
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        staticHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(staticHolder, "/static/*");

        server.setHandler(context);

        server.start();
        System.out.println("✨ Server successfully started at: http://localhost:" + port);
        System.out.println("🔑 Default Teacher Account: teacher@school.edu / teacher123");
        System.out.println("==========================================================");

        server.join();
    }
}
