package com.example.tencentllm.controller;

import com.example.tencentllm.model.dto.ChatRequest;
import com.example.tencentllm.model.dto.ChatResponse;
import com.example.tencentllm.model.dto.ModelResponse;
import com.example.tencentllm.model.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.time.Duration;
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
    public Object chatCompletions(@Valid @RequestBody ChatRequest request) {
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
            return Flux.range(0, 5)
                    .delayElements(Duration.ofMillis(200))
                    .map(i -> "data: {\"id\":\"chatcmpl-" + System.currentTimeMillis() + "\",\"object\":\"chat.completion.chunk\",\"created\":" + (System.currentTimeMillis() / 1000) + ",\"model\":\"" + request.getModel() + "\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"chunk " + (i + 1) + "\"}}]}\n\n")
                    .concatWithValues("data: [DONE]\n\n");
        }

        ChatResponse response = ChatResponse.createDefault(request.getModel(), request.getMessages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models")
    public Mono<ModelResponse> listModels() {
        return Mono.just(ModelResponse.createDefault());
    }

    @GetMapping("/models/{modelId}")
    public Mono<ResponseEntity<?>> getModel(@PathVariable String modelId) {
        ModelResponse modelResponse = ModelResponse.createDefault();
        
        return Mono.just(modelResponse.getData().stream()
            .filter(m -> m.getId().equals(modelId))
            .findFirst()
            .map(model -> ResponseEntity.ok((Object) model))
            .orElse(ResponseEntity.notFound().build()));
    }

    @PostMapping("/completions")
    public Mono<ResponseEntity<ErrorResponse>> completions(@RequestBody String request) {
        ErrorResponse errorResponse = ErrorResponse.createError(
            "Legacy completions endpoint is not supported. Please use chat/completions instead.",
            "invalid_request_error",
            "endpoint_not_supported"
        );
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @PostMapping("/embeddings")
    public Mono<ResponseEntity<ErrorResponse>> embeddings(@RequestBody String request) {
        ErrorResponse errorResponse = ErrorResponse.createError(
            "Embeddings endpoint is not supported.",
            "invalid_request_error",
            "endpoint_not_supported"
        );
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @GetMapping("/engines")
    public Mono<ModelResponse> listEngines() {
        return listModels();
    }

    @GetMapping("/engines/{engineId}")
    public Mono<ResponseEntity<?>> getEngine(@PathVariable String engineId) {
        return getModel(engineId);
    }
}