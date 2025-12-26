package com.example.tencentllm.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ModelResponse {
    private String object = "list";
    private List<Model> data;
    
    public static ModelResponse createDefault() {
        ModelResponse response = new ModelResponse();
        
        response.data = List.of(
            new Model("gpt-3.5-turbo", "gpt-3.5-turbo", "Owned by the mock user"),
            new Model("gpt-4", "gpt-4", "Owned by the mock user"),
            new Model("gpt-4-turbo-preview", "gpt-4-turbo-preview", "Owned by the mock user"),
            new Model("text-davinci-003", "text-davinci-003", "Owned by the mock user"),
            new Model("text-curie-001", "text-curie-001", "Owned by the mock user")
        );
        
        return response;
    }
    
    public static class Model {
        private String id;
        private String object = "model";
        @JsonProperty("created")
        private long created = 1677610602L;
        @JsonProperty("owned_by")
        private String ownedBy;
        
        public Model() {}
        
        public Model(String id, String object, String ownedBy) {
            this.id = id;
            this.object = object;
            this.ownedBy = ownedBy;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        
        public long getCreated() { return created; }
        public void setCreated(long created) { this.created = created; }
        
        public String getOwnedBy() { return ownedBy; }
        public void setOwnedBy(String ownedBy) { this.ownedBy = ownedBy; }
    }
    
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    
    public List<Model> getData() { return data; }
    public void setData(List<Model> data) { this.data = data; }
}