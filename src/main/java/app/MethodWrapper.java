package app;

import org.objectweb.asm.*;
import wrapper.MethodWrapperVisitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.objectweb.asm.Opcodes.*;

public class MethodWrapper extends ClassLoader {

    public static void invokeMethods(ArrayList<Class<?>> classFiles) {
        Object[] arguments = new Object[1];
        arguments[0] = new String[]{};

        for (int i = 0; i < classFiles.size(); i++) {
            if (classFiles.get(i).getName().endsWith("$_init")) {
                try {
                    classFiles.get(i).getDeclaredMethod("main", String[].class).invoke(null, arguments);
                } catch (Error | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println(e);
                }
            }
        }
    }

    public static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(jarPath));
        for (ZipEntry entry = zipFile.getNextEntry(); entry != null; entry = zipFile.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry));
            }
        }
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestURL.openStream());
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");
        } catch (Exception | Error ignore) {
            System.out.println(ignore);
            return null;
        }
    }

    public static byte[] streamToByte(InputStream stream) throws IOException {
        byte[] byteArray = stream.readAllBytes();
        return byteArray;
    }

    public static byte[] modifyMethods(InputStream inputStream, String mainClassPackage) {

        byte[] code;
        try {
            ClassReader classReader = new ClassReader(inputStream);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);

            classWriter.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);


            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, "shutDownHook", "()V", null, null);
            methodVisitor.visitCode();
            Label labelShutDownHookZero = new Label();
            methodVisitor.visitLabel(labelShutDownHookZero);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), Type.getType("()V"), new Handle(Opcodes.H_INVOKESTATIC, mainClassPackage + "/$_init", "lambda$shutDownHook$0", "()V", false), Type.getType("()V"));
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "addShutdownHook", "(Ljava/lang/Thread;)V", false);
            Label labelShutDownHookOne = new Label();
            methodVisitor.visitLabel(labelShutDownHookOne);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(4, 0);
            methodVisitor.visitEnd();


            methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$shutDownHook$0", "()V", null, null);
            methodVisitor.visitCode();
            Label labelLambda$shutDownHook$0Zero = new Label();
            methodVisitor.visitLabel(labelLambda$shutDownHook$0Zero);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("* Profiling Stopped *\n");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "profiler/Profiler", "getInstance", "()Lprofiler/Profiler;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "profiler/Profiler", "printProfilerOutput", "(Ljava/lang/String;)V", false);
            Label labelLambda$shutDownHook$0One = new Label();
            methodVisitor.visitLabel(labelLambda$shutDownHook$0One);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 0);
            methodVisitor.visitEnd();


            ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage);
            classReader.accept(change, ClassReader.EXPAND_FRAMES);
            code = classWriter.toByteArray();
            return code;

        } catch (Exception | Error ignore) {
            System.out.println(ignore);
        }
        return null;
    }

    public static void printCode(String className, byte[] code) {
        try {
            FileOutputStream fos = new FileOutputStream("output/" + className.substring(className.lastIndexOf("/") + 1));
            fos.write(code);
            fos.close();
        } catch (IOException ignore) {
        }
    }
}