package com.example.tencentllm.controller;

import com.example.tencentllm.model.dto.ChatRequest;
import com.example.tencentllm.model.dto.ChatResponse;
import com.example.tencentllm.model.dto.ModelResponse;
import com.example.tencentllm.model.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class OpenAIController {

    private static final Logger log = LoggerFactory.getLogger(OpenAIController.class);
    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
        "gpt-3.5-turbo", "gpt-4", "gpt-4-turbo-preview",
        "text-davinci-003", "text-curie-001"
    );

    @PostMapping(value = "/chat/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@Valid @RequestBody ChatRequest request) {
        String requestInfo = String.format("收到请求 - model: %s, stream: %s, messages: %s",
                request.getModel(), request.getStream(), request.getMessages());
        log.info(requestInfo);

        if (!SUPPORTED_MODELS.contains(request.getModel())) {
            ErrorResponse errorResponse = ErrorResponse.createError(
                "The model `" + request.getModel() + "` does not exist or you do not have access to it.",
                "invalid_request_error",
                "model_not_found"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if ("true".equals(request.getStream())) {
            String userContent = getUserContent(request.getMessages());
            long timestamp = System.currentTimeMillis() / 1000;
            String model = request.getModel();

            Flux<String> fullFlux = Flux.concat(
                Flux.just(String.format(
                    "{\"id\":\"chatcmpl-%d\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}",
                    System.currentTimeMillis(), timestamp, model
                )),
                Flux.interval(Duration.ofSeconds(1))
                    .take(5)
                    .map(i -> String.format(
                        "{\"id\":\"chatcmpl-%d\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"%s\"},\"finish_reason\":null}]}",
                        System.currentTimeMillis(), timestamp, model, escapeJsonString(userContent)
                    )),
                Flux.just(String.format(
                    "{\"id\":\"chatcmpl-%d\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}",
                    System.currentTimeMillis(), timestamp, model
                ))
            );

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(fullFlux);
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

    private String getUserContent(List<ChatRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Hello! I'm a mock AI assistant.";
        }
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.Message message = messages.get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent() != null ? message.getContent() : "";
            }
        }
        
        return "Hello! I'm a mock AI assistant.";
    }

    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f");
    }
}
