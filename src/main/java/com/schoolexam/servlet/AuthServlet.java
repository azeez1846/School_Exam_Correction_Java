package com.schoolexam.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.model.User;
import com.schoolexam.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/login", "/logout", "/register", "/api/auth/me"})
public class AuthServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/login".equals(path)) {
            resp.setContentType("text/html;charset=UTF-8");
            File htmlFile = new File("src/main/webapp/WEB-INF/html/login.html");
            if (htmlFile.exists()) {
                try (InputStream is = new FileInputStream(htmlFile)) {
                    is.transferTo(resp.getOutputStream());
                }
            } else {
                resp.getWriter().write("<h2>Login Page</h2>");
            }
        } else if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            resp.sendRedirect("/login");
        } else if ("/api/auth/me".equals(path)) {
            resp.setContentType("application/json");
            HttpSession session = req.getSession(false);
            User user = session != null ? (User) session.getAttribute("user") : null;
            if (user != null) {
                user.setPasswordHash(null);
                objectMapper.writeValue(resp.getOutputStream(), user);
            } else {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"Not logged in\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/login".equals(path)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");

            if (email == null || password == null) {
                try {
                    Map<String, String> body = objectMapper.readValue(req.getInputStream(), Map.class);
                    if (body != null) {
                        email = body.get("email");
                        password = body.get("password");
                    }
                } catch (Exception e) {}
            }

            User user = authService.authenticate(email, password);
            if (user != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);

                user.setPasswordHash(null);
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("user", user);
                result.put("redirect", "/dashboard");
                objectMapper.writeValue(resp.getOutputStream(), result);
            } else {
                resp.setStatus(401);
                Map<String, Object> err = new HashMap<>();
                err.put("status", "error");
                err.put("message", "Invalid email credentials or password.");
                objectMapper.writeValue(resp.getOutputStream(), err);
            }
        } else if ("/register".equals(path)) {
            Map<String, String> body = objectMapper.readValue(req.getInputStream(), Map.class);
            String email = body != null ? body.get("email") : null;
            String password = body != null ? body.get("password") : null;
            String name = body != null ? body.get("fullName") : null;

            User user = authService.registerUser(email, password, name);
            if (user != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                user.setPasswordHash(null);
                objectMapper.writeValue(resp.getOutputStream(), user);
            } else {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Failed to register user. Email may already exist.\"}");
            }
        }
    }
}
