package wrapper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class InitWrapperAdapter extends AdviceAdapter {

    String mainClassPackage;

    InitWrapperAdapter(int api, MethodVisitor mv, int access, String name, String desc, String mainClassPackage) {
        super(api, mv, access, name, desc);
        this.mainClassPackage = mainClassPackage;
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);

        if(opcode == Opcodes.INVOKEVIRTUAL && name.equals("addShutdownHook")){
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Scheduler", "getRuntimeRegistry", "()Lio/ballerina/runtime/internal/scheduling/RuntimeRegistry;", false);
            mv.visitMethodInsn(INVOKESTATIC, mainClassPackage + "/$_init", "shutDownHook", "(Lio/ballerina/runtime/internal/scheduling/RuntimeRegistry;)V", false);
        }
        else if (name.equals("stopListeners")){
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("stop");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
    }
//    @Override
//    protected void onMethodExit(int opcodes) {
//        mv.visitMethodInsn(INVOKESTATIC, mainClassPackage + "/$_init", "shutDownHook", "()V", false);
//    }
}
