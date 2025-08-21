package com.cdbd.monitor.reporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.*;

public class BytecodeFutureCallAnalyzer {

  private static final int MAX_DEPTH = 5;

  public static List<String> analyze(String className, String methodName, int lineNumber) {
    List<String> futureCalls = new ArrayList<>();
    try {
      String classResourcePath = className.replace('.', '/') + ".class";
      InputStream classStream = Thread.currentThread()
          .getContextClassLoader()
          .getResourceAsStream(classResourcePath);

      if (classStream == null) {
        System.err.println("[Analyzer] Class file not found: " + classResourcePath);
        return futureCalls;
      }

      ClassReader classReader = new ClassReader(classStream);
      classReader.accept(
          new FutureCallClassVisitor(methodName, lineNumber, futureCalls), 0);
    } catch (Exception e) {
      System.err.println("[Analyzer] Failed to analyze bytecode for " + className);
      e.printStackTrace();
    }
    return futureCalls;
  }

  private static class FutureCallClassVisitor extends ClassVisitor {
    private final String targetMethodName;
    private final int targetLineNumber;
    private final List<String> futureCalls;

    public FutureCallClassVisitor(String methodName, int lineNumber, List<String> futureCalls) {
      super(Opcodes.ASM9);
      this.targetMethodName = methodName;
      this.targetLineNumber = lineNumber;
      this.futureCalls = futureCalls;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

      if (name.equals(targetMethodName)) {
        return new FutureCallMethodVisitor(targetLineNumber, futureCalls);
      }
      return null;
    }
  }

  private static class FutureCallMethodVisitor extends MethodVisitor {
    private final int targetLineNumber;
    private final List<String> futureCalls;
    private boolean foundTargetLine = false;
    private int currentDepth = 0;

    public FutureCallMethodVisitor(int lineNumber, List<String> futureCalls) {
      super(Opcodes.ASM9);
      this.targetLineNumber = lineNumber;
      this.futureCalls = futureCalls;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      if (line >= targetLineNumber) {
        this.foundTargetLine = true;
      }
    }

    @Override
    public void visitMethodInsn(
        int opcode, String owner, String name, String descriptor, boolean isInterface) {
      if (foundTargetLine && currentDepth < MAX_DEPTH && !name.equals("<init>")) {
        futureCalls.add(name + "()");
        currentDepth++;
      }
    }
  }
}
