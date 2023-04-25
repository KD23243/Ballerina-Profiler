package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static app.App.balFunctionCount;


public class MethodWrapperVisitor extends ClassVisitor {

    private final String mainClassPackage;
    private final String className;
    private final String strand = "(Lio/ballerina/runtime/internal/scheduling/Strand";

    /**
     * Constructor for MethodWrapperVisitor
     *
     * @param classVisitor     - ClassVisitor instance to visit the methods of the class being visited
     * @param mainClassPackage - name of the package of the main class being visited
     * @param className        - name of the class being visited
     */
    public MethodWrapperVisitor(ClassVisitor classVisitor, String mainClassPackage, String className) {
        super(Opcodes.ASM9, classVisitor);
        this.mainClassPackage = mainClassPackage;
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        balFunctionCount++;
        // get a MethodVisitor for the visited method
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        String classNameWithoutPackage = className.substring(className.lastIndexOf("/") + 1);

        if (desc.startsWith(strand) && !classNameWithoutPackage.endsWith("init.class") && !classNameWithoutPackage.endsWith("$_init.class")){
            if ((access & Opcodes.ACC_STATIC) == 0) {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);
            } else {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);
            }
        }
        else {
            return new ResourceWrapperAdapter(access, methodVisitor, name, desc);
        }


    }
}



//configinit remove