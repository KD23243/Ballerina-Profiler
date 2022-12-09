package wrapper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class MainWrapperAdapter extends AdviceAdapter {

    String mainClassPackage;

    MainWrapperAdapter(int api, MethodVisitor mv, int access, String name, String desc, String mainClassPackage) {
        super(api, mv, access, name, desc);
        this.mainClassPackage = mainClassPackage;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitMethodInsn(INVOKESTATIC, mainClassPackage + "/$_init", "shutDownHook", "()V", false);
    }
}
