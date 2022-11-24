package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodWrapperVisitor extends ClassVisitor {

    String mainClassPackage;
    String className;

    public MethodWrapperVisitor(ClassVisitor classVisitor, String mainClassPackage, String className) {
        super(Opcodes.ASM9, classVisitor);
        this.mainClassPackage = mainClassPackage;
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

//        if (className.endsWith("$_init.class")){
//            return new MainWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc, mainClassPackage);
//        }




        if (className.endsWith("$_init.class") && name.startsWith("main")){
            return new MainWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc, mainClassPackage);
        }else if (!className.endsWith("$_init.class") && desc.startsWith("(Lio/ballerina/runtime/internal/scheduling/Strand")){
            return new MethodWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc);
        }
//        if (name.startsWith("main") && desc.startsWith("([Ljava/lang/String;)V") && className.endsWith("$_init.class")) {
//            return new MainWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc, mainClassPackage);
//        }
//        else if (!className.endsWith("$_init.class") && !name.startsWith("$") && desc.startsWith("(Lio/ballerina/runtime/internal/scheduling/Strand")) {
//            return new MethodWrapperAdapter(Opcodes.ASM9, methodVisitor, access, name, desc);
//        }

        return methodVisitor;
    }
}