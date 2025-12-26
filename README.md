# Mock OpenAI API Server

一个使用Spring Boot构建的模拟OpenAI API服务器，支持基础的Chat Completions等接口，可以用于Cherry Studio等AI客户端的对接测试。

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.2.0
- **构建工具**: Maven
- **容器化**: Docker
- **CI/CD**: GitHub Actions

## 支持的接口

### Chat Completions
- **Endpoint**: `POST /v1/chat/completions`
- **支持模型**: `gpt-3.5-turbo`, `gpt-4`, `gpt-4-turbo-preview`, `text-davinci-003`, `text-curie-001`
- **功能**: 模拟智能对话回复

### Models
- **Endpoint**: `GET /v1/models`
- **功能**: 返回支持的模型列表

### Single Model
- **Endpoint**: `GET /v1/models/{modelId}`
- **功能**: 获取特定模型信息

### Health Check
- **Endpoint**: `GET /health`
- **功能**: 服务健康检查

## 快速开始

### 本地运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd tencent-llm-api
   ```

2. **构建项目**
   ```bash
   mvn clean package -DskipTests
   ```

3. **运行应用**
   ```bash
   java -jar target/tencent-llm-api-1.0.0.jar
   ```

### Docker运行

Docker镜像会在GitHub Actions中自动构建，你可以使用以下方式运行：

```bash
# 拉取镜像（如果已推送到Docker Hub）
docker pull tencent-llm-api:latest

# 或者在GitHub Actions构建后手动构建
docker build -t tencent-llm-api:latest -<<'EOF'
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"
CMD ["java", "-jar", "app.jar"]
EOF

# 运行容器
docker run -p 8080:8080 tencent-llm-api:latest
```

### 使用Docker Compose

```yaml
version: '3.8'
services:
  tencent-llm-api:
    image: tencent-llm-api:latest
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
    restart: unless-stopped
```

## 使用示例

### 配置Cherry Studio

1. **API Base URL**: `http://localhost:8080/v1`
2. **API Key**: 可以输入任意值（如 `sk-test`）
3. **Model**: 选择 `gpt-3.5-turbo` 或其他支持的模型

### 测试API

#### 1. 健康检查
```bash
curl http://localhost:8080/health
```

#### 2. 获取模型列表
```bash
curl http://localhost:8080/v1/models
```

#### 3. Chat Completions
```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [
      {"role": "user", "content": "Hello, how are you?"}
    ],
    "max_tokens": 100,
    "temperature": 0.7
  }'
```

### 响应示例

#### Chat Completions Response
```json
{
  "id": "chatcmpl-1234567890",
  "object": "chat.completion",
  "created": 1703123456,
  "model": "gpt-3.5-turbo",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Hello! I'm doing great, thank you for asking. How can I assist you today?"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 50,
    "total_tokens": 60
  }
}
```

#### Models Response
```json
{
  "object": "list",
  "data": [
    {
      "id": "gpt-3.5-turbo",
      "object": "model",
      "created": 1677610602,
      "owned_by": "Owned by the mock user"
    },
    {
      "id": "gpt-4",
      "object": "model",
      "created": 1677610602,
      "owned_by": "Owned by the mock user"
    }
  ]
}
```

## 项目结构

```
src/main/java/com/example/tencentllm/
├── TencentLlmApiApplication.java  # 主启动类
├── config/
│   └── CorsConfig.java            # CORS配置
├── controller/
│   ├── OpenAIController.java      # OpenAI API接口
│   ├── HealthController.java      # 健康检查接口
│   └── GlobalExceptionHandler.java # 全局异常处理
└── model/dto/
    ├── ChatRequest.java           # 聊天请求DTO
    ├── ChatResponse.java          # 聊天响应DTO
    ├── ModelResponse.java         # 模型响应DTO
    └── ErrorResponse.java         # 错误响应DTO
```

## CI/CD

项目配置了GitHub Actions自动构建流水线：

1. **测试**: 在每次push和pull request时运行单元测试
2. **构建**: 构建Maven项目并上传构建产物
3. **Docker**: 构建Docker镜像并测试
4. **部署**: 在master/main分支更新时触发部署

## 开发说明

### 添加新的API接口

1. 在`OpenAIController.java`中添加新的端点
2. 在`model/dto`包中创建相应的DTO类
3. 更新Swagger文档（如果使用）
4. 添加相应的测试用例

### 自定义回复逻辑

修改`ChatResponse.java`中的`generateDefaultResponse`方法来自定义AI的回复逻辑。

## 注意事项

1. 这是一个Mock API服务，不会提供真正的AI功能
2. 不支持流式输出（stream=true）
3. 部分OpenAI接口（如embeddings、completions）返回错误信息
4. Token计算为模拟值，不是真实的分词结果
5. 无需数据库，所有数据都在内存中处理

## 许可证

MIT License