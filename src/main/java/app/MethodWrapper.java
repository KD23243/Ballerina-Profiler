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

        for (Class<?> classFile : classFiles) {
            if (classFile.getName().endsWith("$_init")) {
                try {
                    classFile.getDeclaredMethod("main", String[].class).invoke(null, arguments);
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

    public static byte[] modifyMethods(InputStream inputStream, String mainClassPackage, String className) throws IOException {

        byte[] code;
        if (className.endsWith("init.class")) {
            try {
                ClassReader classReader = new ClassReader(inputStream);
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className);
                classReader.accept(change, ClassReader.EXPAND_FRAMES);
                code = classWriter.toByteArray();
                return code;

            } catch (Exception | Error ignore) {
                System.out.println(ignore);
            }
        } else {

            try {
                ClassReader reader = new ClassReader(inputStream);
                ClassWriter classWriter = new BallerinaClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className);
                reader.accept(change, ClassReader.EXPAND_FRAMES);
                code = classWriter.toByteArray();
                return code;
            } catch (Exception | Error e) {
                e.printStackTrace();
            }
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