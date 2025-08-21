package com.cdbd.monitor.agent;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AnnotationDetector extends ClassVisitor {

  public boolean hasErrorMonitor = false;
  private final String annotationDescriptor = "Lcom/cdbd/monitor/annotation/ErrorMonitor;";

  protected AnnotationDetector() {
    super(Opcodes.ASM9);
  }

  @Override
  public MethodVisitor visitMethod(
      int access, String name, String descriptor, String signature, String[] exceptions) {
    return new MethodVisitor(Opcodes.ASM9) {

      @Override
      public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals(annotationDescriptor)) {
          hasErrorMonitor = true;
        }
        return super.visitAnnotation(descriptor, visible);
      }
    };
  }
}
