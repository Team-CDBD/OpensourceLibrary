package com.cdbd.monitor.reporter;

import java.util.List;

public class ErrorPayload {

  private final String className;
  private final String methodName;
  private final int lineNumber;
  private final String message;
  private final String severity;
  private final List<String> futureCalls;

  public ErrorPayload(
      String className,
      String methodName,
      int lineNumber,
      String message,
      String severity,
      List<String> futureCalls) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
    this.message = message;
    this.severity = severity;
    this.futureCalls = futureCalls;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getMessage() {
    return message;
  }

  public String getSeverity() {
    return severity;
  }

  public List<String> getFutureCalls() {
    return futureCalls;
  }

}
