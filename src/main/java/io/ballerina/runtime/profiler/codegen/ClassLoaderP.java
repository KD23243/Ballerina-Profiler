package io.ballerina.runtime.profiler.codegen;

import org.objectweb.asm.ClassReader;

public class ClassLoaderP extends ClassLoader {

    public ClassLoaderP(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(byte[] code) {
        Class<?> classOut = null;

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