package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodWrapperVisitor extends ClassVisitor {
    private final String strand = "(Lio/ballerina/runtime/internal/scheduling/Strand";

    /**
     * Constructor for MethodWrapperVisitor
     *
     * @param classVisitor     - ClassVisitor instance to visit the methods of the class being visited
     */
    public MethodWrapperVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // get a MethodVisitor for the visited method
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (desc.startsWith(strand)){
            if ((access & Opcodes.ACC_STATIC) == 0) {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);
            } else {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);
            }
        }
        return new ResourceWrapperAdapter(access, methodVisitor, name, desc);
    }
}
