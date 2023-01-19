package wrapper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class ResourceWrapperAdapter extends AdviceAdapter {

    /**
     Constructor for MethodWrapperAdapter
     @param access - access flag of the method that is wrapped
     @param mv - MethodVisitor instance to generate the bytecode
     @param methodName - name of the method that is wrapped
     @param description - description of the method that is wrapped
     */
    public ResourceWrapperAdapter(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    /*  This method is called when the wrapped method is entered
    It retrieves the profiler instance and starts the profiling */
    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "start", "()V", false);
    }

    /*  This method stops the profiling for the wrapped method
    It retrieves the profiler instance and stops the profiling   */
    protected void onMethodExit(int opcode) {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "stop", "()V", false);
    }
}

