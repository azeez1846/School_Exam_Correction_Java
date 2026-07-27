package com.schoolexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(urlPatterns = {"/", "/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        File htmlFile = new File("src/main/webapp/WEB-INF/html/dashboard.html");
        if (htmlFile.exists()) {
            try (InputStream is = new FileInputStream(htmlFile)) {
                is.transferTo(resp.getOutputStream());
            }
        } else {
            resp.getWriter().write("<h2>Dashboard</h2>");
        }
    }
}
