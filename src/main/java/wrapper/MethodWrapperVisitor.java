package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodWrapperVisitor extends ClassVisitor {

    String mainClassPackage;

    public MethodWrapperVisitor(ClassVisitor classVisitor, String mainClassPackage) {
        super(Opcodes.ASM9, classVisitor);
        this.mainClassPackage = mainClassPackage;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        if (name.startsWith("main") && desc.startsWith("([Ljava/lang/String;)V")) {
            return new MainWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc, mainClassPackage);
        } else if (!name.startsWith("$") && desc.startsWith("(Lio/ballerina/runtime/internal/scheduling/Strand")) {
            return new MethodWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc);
        }

        return methodVisitor;
    }
}