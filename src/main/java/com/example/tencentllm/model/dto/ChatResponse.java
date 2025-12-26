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
            return "Hello! I'm a mock AI assistant.";
        }
        
        // 获取最后一条用户消息并直接返回
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.Message message = messages.get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent() != null ? message.getContent() : "Hello! I'm a mock AI assistant.";
            }
        }
        
        return "Hello! I'm a mock AI assistant.";
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