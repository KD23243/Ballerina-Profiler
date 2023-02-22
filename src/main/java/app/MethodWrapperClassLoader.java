package app;

import org.objectweb.asm.ClassReader;

public class MethodWrapperClassLoader extends ClassLoader {

    public MethodWrapperClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected Class < ? > loadClass(byte[] code) {
        Class < ? > classOut = null;

        String name = readClassName(code);

        try {
            classOut = defineClass(name, code, 0, code.length);
        } catch (Error e) {
            System.out.println(name);
        }
        return classOut;
    }

    public String readClassName(final byte[] byteCode) {
        String className;
        className = new ClassReader(byteCode).getClassName().replace("/", ".");
        return className;
    }
}