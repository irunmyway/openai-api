package com.example.tencentllm.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class ChatRequest {
    
    @NotBlank
    private String model;
    
    @NotNull
    private List<Message> messages;
    
    private Double temperature = 0.7;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens = 1000;
    
    private String stream;
    
    private Double topP = 1.0;
    
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty = 0.0;
    
    @JsonProperty("presence_penalty")
    private Double presencePenalty = 0.0;
    
    private List<String> stop;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;

    }

    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    
    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }
    
    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }
    
    public Double getFrequencyPenalty() { return frequencyPenalty; }
    public void setFrequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; }
    
    public Double getPresencePenalty() { return presencePenalty; }
    public void setPresencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; }
    
    public List<String> getStop() { return stop; }
    public void setStop(List<String> stop) { this.stop = stop; }
}