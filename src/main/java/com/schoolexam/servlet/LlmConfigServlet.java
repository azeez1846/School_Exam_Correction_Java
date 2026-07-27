package com.schoolexam.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.dao.LlmConfigDao;
import com.schoolexam.model.LlmConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/llm-configs", "/api/llm-configs/*"})
public class LlmConfigServlet extends HttpServlet {

    private final LlmConfigDao llmConfigDao = new LlmConfigDao();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        List<LlmConfig> configs = llmConfigDao.findAll();
        // Mask API keys for security in UI output except checking length
        for (LlmConfig cfg : configs) {
            if (cfg.getApiKey() != null && cfg.getApiKey().length() > 6 && !"NONE".equals(cfg.getApiKey())) {
                cfg.setApiKey(cfg.getApiKey().substring(0, 4) + "..." + cfg.getApiKey().substring(cfg.getApiKey().length() - 4));
            }
        }
        objectMapper.writeValue(resp.getOutputStream(), configs);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Map<String, String> body = objectMapper.readValue(req.getInputStream(), Map.class);
        String action = body.get("action");
        String providerKey = body.get("providerKey");

        Map<String, Object> result = new HashMap<>();

        if ("setDefault".equals(action)) {
            boolean success = llmConfigDao.setDefaultProvider(providerKey);
            result.put("success", success);
            result.put("message", "Default LLM model set to: " + providerKey);
        } else if ("updateKey".equals(action)) {
            String apiKey = body.get("apiKey");
            boolean success = llmConfigDao.updateApiKey(providerKey, apiKey);
            result.put("success", success);
            result.put("message", "API Key updated for model: " + providerKey);
        } else {
            resp.setStatus(400);
            result.put("error", "Invalid action");
        }
        objectMapper.writeValue(resp.getOutputStream(), result);
    }
}
