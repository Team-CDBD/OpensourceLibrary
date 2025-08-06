package com.cdbd.monitor.agent;

import com.cdbd.monitor.annotation.ErrorMonitor;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;


public class ErrorMonitorTransformer implements ClassFileTransformer {

  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classFileBuffer) {
    if (className == null || className.startsWith("com/cdbd/monitor") || className.startsWith("net/bytebuddy")) {
      return null;
    }

    try {
      TypePool typePool = TypePool.Default.of(loader);
      TypeDescription typeDescription = typePool.describe(className.replace('/', '.')).resolve();

      return new ByteBuddy()
          .redefine(
              typeDescription,
              ClassFileLocator.Simple.of(
                  className.replace('/', '.'), classFileBuffer))
          .visit(
              Advice.to(ErrorMonitorAdvice.class).on(ElementMatchers.isAnnotatedWith(ErrorMonitor.class)))
          .make()
          .getBytes();
    } catch (Exception e) {
      System.err.println("[ErrorMonitorTransformer] Failed to transform " + className);
      e.printStackTrace();
      return null;
    }
  }

}
