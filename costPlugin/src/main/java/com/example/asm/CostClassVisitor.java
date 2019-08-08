package com.example.asm;


import com.example.annotation.Cost;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class CostClassVisitor extends ClassVisitor {

    public CostClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        mv = new AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {

            private boolean inject = false;

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                System.out.println("AnnotationVisitor:"+desc);
                System.out.println("AnnotationVisitor2:"+Type.getDescriptor(Cost.class));
                if (Type.getDescriptor(Cost.class).equals(desc)) {
                    inject = true;
                }

                System.out.println("AnnotationVisitor inject:"+inject);

                return super.visitAnnotation(desc, visible);
            }

            @Override
            protected void onMethodEnter() {
                if (inject) {
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/common/TimeCost", "setStartTime", "(Ljava/lang/String;J)V", false);

                }
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (inject) {
                    mv.visitLdcInsn("method");
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/common/TimeCost", "setEndTime", "(Ljava/lang/String;J)V", false);

                    mv.visitLdcInsn("method");
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/common/TimeCost", "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
                }
            }
        };
        return mv;
    }
}