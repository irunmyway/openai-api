package com.example.tencentllm.controller;

import com.example.tencentllm.model.dto.ChatRequest;
import com.example.tencentllm.model.dto.ChatResponse;
import com.example.tencentllm.model.dto.ModelResponse;
import com.example.tencentllm.model.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        // 设置系统编码
        System.setProperty("file.encoding", "UTF-8");
        
        String requestInfo = String.format("收到请求 - model: %s, stream: %s, messages: %s", 
                request.getModel(), request.getStream(), request.getMessages());
        System.out.println("=== " + requestInfo + " ===");
        log.info(requestInfo);

        // Validate model
        if (!SUPPORTED_MODELS.contains(request.getModel())) {
            ErrorResponse errorResponse = ErrorResponse.createError(
                "The model `" + request.getModel() + "` does not exist or you do not have access to it.",
                "invalid_request_error",
                "model_not_found"
            );
            String errorMsg = "模型不支持: " + request.getModel();
            System.out.println("ERROR: " + errorMsg);
            log.error(errorMsg);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Check if streaming is requested
        if ("true".equals(request.getStream())) {
            String userContent = getUserContent(request.getMessages());
            String streamInfo = "流式响应 - 用户内容: " + userContent;
            System.out.println("STREAM: " + streamInfo);
            log.info(streamInfo);
            
            return Flux.range(0, userContent.length())
                    .delayElements(Duration.ofMillis(50))
                    .doOnNext(i -> {
                        String charInfo = String.format("发送字符 '%c' (位置: %d)", userContent.charAt(i), i);
                        System.out.println("CHAR: " + charInfo);
                        log.debug(charInfo);
                    })
                    .map(i -> {
                        char c = userContent.charAt(i);
                        String escapedChar = escapeJsonChar(c);
                        String json = String.format(
                            "{\"id\":\"chatcmpl-%d\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"%s\"}}]",
                            System.currentTimeMillis(),
                            System.currentTimeMillis() / 1000,
                            request.getModel(),
                            escapedChar
                        );
                        String chunk = "data: " + json + "\n\n";
                        System.out.println("CHUNK: " + json);
                        log.debug("生成SSE块: {}", json);
                        return chunk;
                    })
                    .concatWithValues("data: [DONE]\n\n")
                    .doOnComplete(() -> {
                        String completeMsg = "流式响应完成";
                        System.out.println("COMPLETE: " + completeMsg);
                        log.info(completeMsg);
                    });
        }

        String normalInfo = "非流式响应";
        System.out.println("NORMAL: " + normalInfo);
        log.info(normalInfo);
        
        ChatResponse response = ChatResponse.createDefault(request.getModel(), request.getMessages());
        String responseInfo = "返回响应: " + response;
        System.out.println("RESPONSE: " + responseInfo);
        log.info(responseInfo);
        
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
        
        // 获取最后一条用户消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.Message message = messages.get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent() != null ? message.getContent() : "";
            }
        }
        
        return "Hello! I'm a mock AI assistant.";
    }

    private String escapeJsonChar(char c) {
        switch (c) {
            case '"': return "\\\"";
            case '\\': return "\\\\";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            default: return String.valueOf(c);
        }
    }
}