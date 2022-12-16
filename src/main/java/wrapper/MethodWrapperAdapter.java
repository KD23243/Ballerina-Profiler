package wrapper;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Hashtable;

public class MethodWrapperAdapter extends AdviceAdapter {

    String desc;

    MethodWrapperAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.desc = desc;
    }

    private Label tryStart = new Label();
    private Label tryEnd = new Label();
    private Label catchStart = new Label();
    private Label catchEnd = new Label();

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();


        mv.visitCode();
//        mv.visitFrame(F_NEW, initLocals.length, initLocals, 0, new Object[]{});
        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, "java/lang/Throwable");
        mv.visitLabel(tryStart);

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("start");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        mv.visitLabel(tryEnd);
        mv.visitJumpInsn(GOTO, catchEnd);
        mv.visitLabel(catchStart);
        // exception caught
        mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});

        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("stop");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);


        mv.visitInsn(ATHROW);
        mv.visitLabel(catchEnd);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        super.visitEnd();


//        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "start", "()V", false);

    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);




//        if (opcode != Opcodes.ATHROW) {
//            mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getState", "()Lio/ballerina/runtime/internal/scheduling/State;", false);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/State", "toString", "()Ljava/lang/String;", false);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "stop", "(Ljava/lang/String;)V", false);
//        }
    }
}
