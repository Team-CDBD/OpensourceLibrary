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
      StackTraceElement element = throwable.getStackTrace()[0];
      String className = element.getClassName();
      String methodName = element.getMethodName();
      int lineNumber = element.getLineNumber();
      String errorMessage = throwable.getMessage();
      String severity = "ERROR";
      List<String> futureCalls = BytecodeFutureCallAnalyzer.analyze(
          className, methodName, lineNumber);

      ErrorPayload errorPayload = new ErrorPayload(
          className, methodName, lineNumber, errorMessage, severity, futureCalls
      );

    } catch (Throwable t) {
      // ErrorReporter 자체에서 오류가 나더라도 절대 밖으로 전파되지 않도록 함
      System.err.println("[ErrorReporter] Failed to report an error.");
      t.printStackTrace();
    }
  }
}
