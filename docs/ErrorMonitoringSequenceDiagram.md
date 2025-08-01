### 시퀀스 다이어그램
```mermaid
sequenceDiagram
    participant JVM as JVM
    participant Agent as ErrorMonitorAgent
    participant Transformer as ErrorMonitorTransformer
    participant ByteBuddy as ByteBuddy
    participant UserApp as UserService
    participant Advice as ErrorMonitorAdvice
    participant Reporter as ErrorReporter
    participant Analyzer as BytecodeFutureCallAnalyzer
    participant Config as ErrorMonitorConfig
    participant Server as External Server

    %% Phase 1: Application Startup & Agent Initialization
    Note over JVM, Agent: 🚀 Application Startup Phase
    JVM->>Agent: premain(agentArgs, instrumentation)
    Agent->>Agent: Initialize ErrorMonitorAgent
    Agent->>Transformer: new ErrorMonitorTransformer()
    Agent->>JVM: instrumentation.addTransformer(transformer)

    %% Phase 2: Class Loading & Transformation
    Note over JVM, ByteBuddy: 🔧 Class Loading & Bytecode Transformation
    JVM->>Transformer: transform(loader, className, ..., classfileBuffer)
    
    %% Performance Optimization Check
    Transformer->>Transformer: containsErrorMonitorAnnotation(classfileBuffer)
    alt @ErrorMonitor annotation found
        Transformer->>ByteBuddy: new ByteBuddy().redefine(...)
        ByteBuddy->>ByteBuddy: visit(Advice.to(ErrorMonitorAdvice.class))
        ByteBuddy->>Transformer: transformed bytecode
        Transformer->>JVM: return transformedBytes
    else No annotation found
        Transformer->>JVM: return null (skip transformation)
    end

    %% Phase 3: Runtime Execution & Error Occurrence
    Note over UserApp, Server: 🏃‍♂️ Runtime Execution Phase
    UserApp->>UserApp: processUser() // @ErrorMonitor method called
    
    Note over UserApp: Original user code wrapped in try-catch by Agent
    UserApp->>UserApp: validateUser() // executes normally
    UserApp->>UserApp: saveUser() // 💥 RuntimeException thrown!
    
    %% Phase 4: Exception Handling & Reporting
    Note over Advice, Server: 🚨 Exception Handling Phase
    UserApp->>Advice: onExit(thrown = RuntimeException)
    Advice->>Advice: if (thrown != null)
    Advice->>Reporter: ErrorReporter.handle(thrown)
    
    %% Asynchronous Processing Starts Here
    Reporter->>Reporter: executor.submit(() -> processError())
    Note over Reporter: 🔄 Async processing to avoid blocking user flow
    
    par Async Error Processing
        Reporter->>Config: loadConfiguration()
        Config->>Reporter: configuration properties
        
        Reporter->>Reporter: extract stack trace info
        Note over Reporter: className, methodName, lineNumber
        
        Reporter->>Analyzer: analyze(className, methodName, lineNumber)
        Analyzer->>Analyzer: parse bytecode for future INVOKE instructions
        Analyzer->>Reporter: List<String> futureCalls
        
        Reporter->>Reporter: createJsonPayload(throwable, futureCalls)
        Note over Reporter: Create ErrorPayload object
        
        Reporter->>Server: HTTP POST /report (JSON payload)
        Server->>Reporter: 200 OK
        
        Reporter->>Reporter: log success
    and Original Exception Flow Continues
        Advice->>UserApp: re-throw exception (automatic)
        UserApp->>UserApp: original exception handling continues
    end

    %% Phase 5: Application Continues Normally
    Note over UserApp: 🔄 Application continues with original exception handling
    UserApp->>UserApp: catch(RuntimeException e) // user's original catch block
    UserApp->>UserApp: handle exception as intended

    %% Error Case Handling
    Note over Reporter, Server: ⚠️ Error Handling Scenarios
    alt Network failure or server error
        Reporter->>Server: HTTP POST /report
        Server-->>Reporter: 500 Error or timeout
        Reporter->>Reporter: log failure, continue gracefully
    end
    
    alt ErrorReporter itself throws exception
        Reporter->>Reporter: try { processError() } catch(Throwable t)
        Reporter->>Reporter: System.err.println("ErrorReporter failed: " + t.getMessage())
        Note over Reporter: Never interfere with original application flow
    end
```
- 시퀀스 다이어그램 주요 흐름
    1. **🚀 Startup Phase**: Agent 초기화 및 Transformer 등록
    2. **🔧 Transformation Phase**: 클래스 로딩 시 바이트코드 변환 (성능 최적화 포함)
    3. **🏃‍♂️ Runtime Phase**: 실제 사용자 코드 실행
    4. **🚨 Exception Handling**: 예외 발생 시 자동 감지 및 비동기 처리
    5. **🔄 Graceful Continue**: 원본 애플리케이션 흐름은 방해받지 않음