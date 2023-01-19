package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodWrapperVisitor extends ClassVisitor {

    String mainClassPackage;
    String className;
    String strand = "(Lio/ballerina/runtime/internal/scheduling/Strand";
    String valueAnon = "/$value$$anonType$_";

    public MethodWrapperVisitor(ClassVisitor classVisitor, String mainClassPackage, String className) {
        super(Opcodes.ASM9, classVisitor);
        this.mainClassPackage = mainClassPackage;
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        if (!className.startsWith(mainClassPackage + valueAnon) && !className.endsWith("$_init.class") && desc.startsWith(strand) && !name.startsWith("$")){
            return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);
        }
        else if (className.startsWith(mainClassPackage + valueAnon)){
            if (desc.startsWith(strand) && name.startsWith("$") && !name.startsWith("$anonType") && !name.startsWith("$init")){
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);
            }else if (name.endsWith("$init")){
                return new ResourceWrapperAdapter(access, methodVisitor, name, desc);
            }
        }
        return methodVisitor;
    }
}