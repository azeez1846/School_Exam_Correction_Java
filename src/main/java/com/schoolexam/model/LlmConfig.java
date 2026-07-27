package com.schoolexam.model;

import java.time.LocalDateTime;

public class LlmConfig {
    private Long id;
    private String providerKey; // gemini-1.5-flash, gemini-2.0-flash, groq-llama3, huggingface-mistral, local-rule-engine
    private String providerName;
    private String apiKey;
    private String apiEndpoint;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime updatedAt;

    public LlmConfig() {}

    public LlmConfig(Long id, String providerKey, String providerName, String apiKey, String apiEndpoint, Boolean isDefault, Boolean isActive, LocalDateTime updatedAt) {
        this.id = id;
        this.providerKey = providerKey;
        this.providerName = providerName;
        this.apiKey = apiKey;
        this.apiEndpoint = apiEndpoint;
        this.isDefault = isDefault;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProviderKey() { return providerKey; }
    public void setProviderKey(String providerKey) { this.providerKey = providerKey; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean aDefault) { isDefault = aDefault; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
