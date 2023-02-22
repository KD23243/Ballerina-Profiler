package wrapper;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static app.App.utilPaths;

public class MethodWrapperVisitor extends ClassVisitor {

    private final String mainClassPackage;
    private final String className;
    private final String strand = "(Lio/ballerina/runtime/internal/scheduling/Strand";
    private final String valueAnon = "/$value$$anonType$_";

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
        // get a MethodVisitor for the visited method
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        if (desc.startsWith(strand) && !className.contains("$_init") && !name.startsWith("$init")) {
            if (!className.startsWith(mainClassPackage + valueAnon) && !className.contains("init")) {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);
            } else if (className.startsWith(mainClassPackage + valueAnon) && !name.startsWith("$anonType")) {
                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);
            }
        }

        return methodVisitor;

        //        if (!className.startsWith(mainClassPackage + valueAnon) && !className.contains("init") && desc.startsWith(strand) && !name.startsWith(" $")){
        //            return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);
        //        }
        //        else if (className.startsWith(mainClassPackage + valueAnon)){
        //            if (desc.startsWith(strand) && !name.startsWith(" $") && !name.startsWith("$anonType") && !name.startsWith("$init")){
        //                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);
        //            }
        //        }

        //        if (!className.startsWith(mainClassPackage + valueAnon) && !className.endsWith("$_init.class") && desc.startsWith(strand)){
        //            return new MethodWrapperAdapter(access, methodVisitor, name, desc, 0);  // If the above condition is true, return a new MethodWrapperAdapter
        //        }
        //        // check if the className starts with the mainClassPackage + valueAnon
        //        else if (className.startsWith(mainClassPackage + valueAnon)){
        //            // check if the desc starts with the strand and the name starts with "$" and doesn't start with "$anonType" and "$init"
        //            if (desc.startsWith(strand) && name.startsWith("$") && !name.startsWith("$anonType") && !name.startsWith("$init")){
        //                return new MethodWrapperAdapter(access, methodVisitor, name, desc, 1);  // If the above condition is true, return a new MethodWrapperAdapter
        //            }
        //        }

        // If none of the above conditions are true, return the original methodVisitor
    }
}