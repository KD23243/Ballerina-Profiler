package wrapper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class BasicWrapperAdapter extends AdviceAdapter {

    public BasicWrapperAdapter(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "start", "()V", false);
    }

}
