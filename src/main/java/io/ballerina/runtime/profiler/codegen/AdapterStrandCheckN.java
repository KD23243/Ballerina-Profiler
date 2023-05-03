package io.ballerina.runtime.profiler.codegen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class AdapterStrandCheckN extends AdviceAdapter {

    /**
     Constructor for MethodWrapperAdapter
     @param access - access flag of the method that is wrapped
     @param mv - MethodVisitor instance to generate the bytecode
     @param methodName - name of the method that is wrapped
     @param description - description of the method that is wrapped
     */
    public AdapterStrandCheckN(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, "io/ballerina/runtime/profiler/runtime/Profiler", "getInstance", "()Lio/ballerina/runtime/profiler/runtime/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/profiler/runtime/Profiler", "start", "()V", false);
    }

    protected void onMethodExit(int opcode) {
        mv.visitMethodInsn(INVOKESTATIC, "io/ballerina/runtime/profiler/runtime/Profiler", "getInstance", "()Lio/ballerina/runtime/profiler/runtime/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/profiler/runtime/Profiler", "stop", "()V", false);
    }
}