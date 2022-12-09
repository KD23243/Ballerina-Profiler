package wrapper;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MethodWrapperAdapter extends AdviceAdapter {

    String desc;

    MethodWrapperAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.desc = desc;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();

        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "start", "()V", false);

    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);

//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn("b7a");
////        mv.visitVarInsn(ALOAD,0);
////        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getState", "()Lio/ballerina/runtime/internal/scheduling/State;", false);
////        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "toString", "()Ljava/lang/String;", false);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);



        if (opcode != Opcodes.ATHROW){
            mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getState", "()Lio/ballerina/runtime/internal/scheduling/State;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/State", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "stop", "(Ljava/lang/String;)V", false);
        }
    }
}
