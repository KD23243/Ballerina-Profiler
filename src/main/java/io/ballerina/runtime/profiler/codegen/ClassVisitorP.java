package io.ballerina.runtime.profiler.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorP extends ClassVisitor {
    public ClassVisitorP(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (desc.startsWith("(Lio/ballerina/runtime/internal/scheduling/Strand")){
            return new AdapterStrandCheckY(access, methodVisitor, name, desc, (access & Opcodes.ACC_STATIC));
        }
        return new AdapterStrandCheckN(access, methodVisitor, name, desc);
    }
}