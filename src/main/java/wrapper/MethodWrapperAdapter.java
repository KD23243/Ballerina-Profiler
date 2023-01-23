package wrapper;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

    /*  This class is an adapter that wraps a method in order to profile its execution time.
    It is using the ASM library to generate bytecode for the wrapped method.
    The profiler instance is retrieved from the class "profiler/Profiler" and its methods are called
    to start and stop the profiling for the given method.   */

public class MethodWrapperAdapter extends AdviceAdapter {

    Label tryStart = new Label();
    int load;

    /**
     Constructor for MethodWrapperAdapter
     @param access - access flag of the method that is wrapped
     @param mv - MethodVisitor instance to generate the bytecode
     @param methodName - name of the method that is wrapped
     @param description - description of the method that is wrapped
     @param load - the index of the local variable that holds the strand instance
     */
    public MethodWrapperAdapter(int access, MethodVisitor mv, String methodName, String description, int load) {
        super(Opcodes.ASM9, mv, access, methodName, description);
        this.load = load;
    }

    /*  This method is called when the visitCode() method is called on the MethodVisitor
    It adds a label to the try block of the wrapped method  */
    public void visitCode() {
        super.visitCode();
        mv.visitLabel(tryStart);
    }

    /*  This method is called when the wrapped method is entered
    It retrieves the profiler instance, gets the strand id and starts the profiling */
    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getId", "()I", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "start", "(I)V", false);
    }

    /**
     This method is called when the wrapped method is exited
     If the exit is not due to an exception, it calls the onFinally method
     @param opcode - the opcode of the instruction that caused the method exit
     */
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            onFinally();
        }
    }

    /**
     This method is called to generate the max stack and max locals for the wrapped method
     It adds a try-catch block to the wrapped method and calls the onFinally method in the catch block
     @param maxStack - the maximum stack size
     @param maxLocals - the maximum number of local variables
     */
    public void visitMaxs(int maxStack, int maxLocals) {
        Label tryEnd = new Label();
        mv.visitTryCatchBlock(tryStart, tryEnd, tryEnd, null);
        mv.visitLabel(tryEnd);
        onFinally();
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
    }

    /*  This method stops the profiling for the wrapped method
    It retrieves the profiler instance, gets the strand state and id, and stops the profiling   */
    private void onFinally() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getState", "()Lio/ballerina/runtime/internal/scheduling/State;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/State", "toString", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getId", "()I", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "stop", "(Ljava/lang/String;I)V", false);
    }
}

//java -jar app.jar -f /home/wso2/Documents/bigDemo/package_api_pack/package.api.jar -t 5000
