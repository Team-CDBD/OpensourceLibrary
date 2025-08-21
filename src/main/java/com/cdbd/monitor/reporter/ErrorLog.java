package com.cdbd.monitor.reporter;

import java.util.List;

public class ErrorLog {

  private final String className;
  private final String method;
  private final int line;
  private final String message;
  private final String severity;
  private final List<String> futureCalls;

  public ErrorLog(
      String className,
      String method,
      int line,
      String message,
      String severity,
      List<String> futureCalls) {
    this.className = className;
    this.method = method;
    this.line = line;
    this.message = message;
    this.severity = severity;
    this.futureCalls = futureCalls;
  }

  public String getClassName() {
    return className;
  }

  public String getMethod() {
    return method;
  }

  public int getLine() {
    return line;
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
