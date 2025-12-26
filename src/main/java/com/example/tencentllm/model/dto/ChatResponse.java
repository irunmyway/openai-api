package com.example.tencentllm.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class ChatResponse {
    
    private String id;
    private String object = "chat.completion";
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    public static ChatResponse createDefault(String model, List<ChatRequest.Message> messages) {
        ChatResponse response = new ChatResponse();
        response.id = "chatcmpl-" + System.currentTimeMillis();
        response.created = Instant.now().getEpochSecond();
        response.model = model;
        
        Choice choice = new Choice();
        choice.index = 0;
        choice.message = new ChatRequest.Message("assistant", generateDefaultResponse(messages));
        choice.finishReason = "stop";
        
        response.choices = List.of(choice);
        
        Usage usage = new Usage();
        usage.promptTokens = calculateTokens(messages);
        usage.completionTokens = 50;
        usage.totalTokens = usage.promptTokens + usage.completionTokens;
        response.usage = usage;
        
        return response;
    }
    
    private static String generateDefaultResponse(List<ChatRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Hello! I'm a mock AI assistant. How can I help you today?";
        }
        
        ChatRequest.Message lastMessage = messages.get(messages.size() - 1);
        String content = lastMessage.getContent();
        
        if (content.toLowerCase().contains("hello") || content.toLowerCase().contains("hi")) {
            return "Hello! I'm doing great, thank you for asking. How can I assist you today?";
        } else if (content.toLowerCase().contains("how are you")) {
            return "I'm doing well! I'm a mock AI assistant ready to help you with various tasks.";
        } else if (content.toLowerCase().contains("weather")) {
            return "I'm a mock AI and don't have access to real-time weather data. However, I recommend checking your local weather service for accurate information.";
        } else if (content.toLowerCase().contains("code") || content.toLowerCase().contains("program")) {
            return "I'd be happy to help you with coding! Since I'm a mock AI, I can provide general programming advice and examples. What specific programming task are you working on?";
        } else {
            return "I understand you said: \"" + content + "\". As a mock AI assistant, I'm here to help! This is a simulated response for testing purposes.";
        }
    }
    
    private static int calculateTokens(List<ChatRequest.Message> messages) {
        // Simple mock token calculation
        return messages.stream()
            .mapToInt(msg -> msg.getContent().length() / 4 + 1)
            .sum();
    }
    
    public static class Choice {
        private int index;
        private ChatRequest.Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
        
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        
        public ChatRequest.Message getMessage() { return message; }
        public void setMessage(ChatRequest.Message message) { this.message = message; }
        
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }
    
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
        @JsonProperty("total_tokens")
        private int totalTokens;
        
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    
    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
    
    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }
}