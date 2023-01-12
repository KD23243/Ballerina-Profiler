package wrapper;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class InitWrapperAdapter extends AdviceAdapter {

    String mainClassPackage;

    InitWrapperAdapter(int api, MethodVisitor mv, int access, String name, String desc, String mainClassPackage) {
        super(api, mv, access, name, desc);
        this.mainClassPackage = mainClassPackage;
    }
}
