package com.cdbd.monitor.agent;

import com.cdbd.monitor.reporter.ErrorReporter;
import net.bytebuddy.asm.Advice;

public class ErrorMonitorAdvice {
  @Advice.OnMethodExit(onThrowable = Throwable.class)
  public static void onExit(@Advice.Thrown Throwable thrown) {
    if (thrown != null) {
      ErrorReporter.handle(thrown);
    }
  }
}
