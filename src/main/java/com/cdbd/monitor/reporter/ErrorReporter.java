package com.cdbd.monitor.reporter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ErrorReporter {

  private static final ExecutorService executor = Executors.newFixedThreadPool(2);
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

      ErrorLog errorLog = new ErrorLog(
          className, methodName, lineNumber, errorMessage, severity, futureCalls
      );
      String jsonPayload = gson.toJson(errorLog);
      System.out.println(jsonPayload);

      System.out.println("AI에 분석 요청을 보냅니다...");

    } catch (Throwable t) {
      // ErrorReporter 자체에서 오류가 나더라도 절대 밖으로 전파되지 않도록 함
      System.err.println("[ErrorReporter] Failed to report an error.");
      t.printStackTrace();
    }
  }
}
