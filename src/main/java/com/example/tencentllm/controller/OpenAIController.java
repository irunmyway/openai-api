package com.example.tencentllm.controller;

import com.example.tencentllm.model.dto.ChatRequest;
import com.example.tencentllm.model.dto.ChatResponse;
import com.example.tencentllm.model.dto.ModelResponse;
import com.example.tencentllm.model.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class OpenAIController {

    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
        "gpt-3.5-turbo", "gpt-4", "gpt-4-turbo-preview", 
        "text-davinci-003", "text-curie-001"
    );

    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(@Valid @RequestBody ChatRequest request) {
        try {
            // Validate model
            if (!SUPPORTED_MODELS.contains(request.getModel())) {
                ErrorResponse errorResponse = ErrorResponse.createError(
                    "The model `" + request.getModel() + "` does not exist or you do not have access to it.",
                    "invalid_request_error",
                    "model_not_found"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if streaming is requested
            if ("true".equals(request.getStream())) {
                // For simplicity, return an error for streaming requests
                ErrorResponse errorResponse = ErrorResponse.createError(
                    "Streaming is not supported in this mock implementation.",
                    "invalid_request_error",
                    "streaming_not_supported"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ChatResponse response = ChatResponse.createDefault(request.getModel(), request.getMessages());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.createError(
                "An internal error occurred.",
                "internal_server_error",
                "internal_error"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/models")
    public ResponseEntity<ModelResponse> listModels() {
        ModelResponse response = ModelResponse.createDefault();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models/{modelId}")
    public ResponseEntity<?> getModel(@PathVariable String modelId) {
        ModelResponse modelResponse = ModelResponse.createDefault();
        
        ModelResponse.Model model = modelResponse.getData().stream()
            .filter(m -> m.getId().equals(modelId))
            .findFirst()
            .orElse(null);

        if (model == null) {
            ErrorResponse errorResponse = ErrorResponse.createError(
                "Model not found",
                "invalid_request_error",
                "model_not_found"
            );
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(model);
    }

    @PostMapping("/completions")
    public ResponseEntity<?> completions(@RequestBody String request) {
        ErrorResponse errorResponse = ErrorResponse.createError(
            "Legacy completions endpoint is not supported in this mock implementation. Please use chat/completions instead.",
            "invalid_request_error",
            "endpoint_not_supported"
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @PostMapping("/embeddings")
    public ResponseEntity<?> embeddings(@RequestBody String request) {
        ErrorResponse errorResponse = ErrorResponse.createError(
            "Embeddings endpoint is not supported in this mock implementation.",
            "invalid_request_error",
            "endpoint_not_supported"
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @GetMapping("/engines")
    public ResponseEntity<ModelResponse> listEngines() {
        return listModels(); // Alias for models endpoint
    }

    @GetMapping("/engines/{engineId}")
    public ResponseEntity<?> getEngine(@PathVariable String engineId) {
        return getModel(engineId); // Alias for models endpoint
    }
}