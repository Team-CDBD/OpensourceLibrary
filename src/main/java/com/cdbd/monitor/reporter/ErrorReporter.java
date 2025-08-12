package com.cdbd.monitor.reporter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ErrorReporter {

  private static final ExecutorService executor = Executors.newFixedThreadPool(2);

  public static void handle(Throwable throwable) {
    executor.submit(() -> handleInternal(throwable));
  }

  private static void handleInternal(Throwable throwable) {
    try {
      System.out.println("\n=========================================");
      System.out.println("    [ErrorMonitor] An exception was caught!");
      System.out.println("=========================================");

      StackTraceElement element = throwable.getStackTrace()[0];
      String className = element.getClassName();
      String methodName = element.getMethodName();
      int lineNumber = element.getLineNumber();

      System.out.println("Error Type: " + throwable.getClass().getName());
      System.out.println("Error Message: " + throwable.getMessage());
      System.out.println(String.format("Location: %s.%s() at line %d",
          element.getClassName(), element.getMethodName(), element.getLineNumber()));

      //TODO : JSON 생성, HTTP 전송 로직 추가
      List<String> futureCalls = BytecodeFutureCallAnalyzer.analyze(
          className, methodName, lineNumber);
      if (!futureCalls.isEmpty()) {
        System.out.println("Predicted Future Calls: " + futureCalls);
      }
      System.out.println("=========================================\n");

    } catch (Throwable t) {
      // ErrorReporter 자체에서 오류가 나더라도 절대 밖으로 전파되지 않도록 함
      System.err.println("[ErrorReporter] Failed to report an error.");
      t.printStackTrace();
    }
  }
}
