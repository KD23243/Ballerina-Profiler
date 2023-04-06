package app;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import wrapper.MethodWrapperVisitor;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static app.App.*;

public class MethodWrapper extends ClassLoader {

    public static void invokeMethods() throws IOException, InterruptedException {
        String[] command = {"java", "-jar", "temp.jar"};
        command = balJarArgs != null ? Arrays.copyOf(command, command.length + 1) : command;
        if (balJarArgs != null) command[3] = balJarArgs;
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        System.out.println(ANSI_ORANGE + "[5/6] Running Executable..." + ANSI_RESET);
        new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines()
                .forEach(System.out::println);
        process.waitFor();
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestURL.openStream());
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");
        } catch (Exception | Error throwable) {
            System.out.println(throwable);
            return null;
        }
    }

    // This method takes an InputStream as a parameter and converts it to a byte array.
    public static byte[] streamToByte(InputStream stream) throws IOException {
        return stream.readAllBytes(); // The byte array is then returned
    }

    public static byte[] modifyMethods(InputStream inputStream, String mainClassPackage, String className) throws IOException {
        byte[] code;
        if (className.endsWith("init.class")) {
            try {
                ClassReader classReader = new ClassReader(inputStream); //Create a ClassReader object using the inputStream
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS); //Create a ClassWriter object using the classReader with only COMPUTE_MAXS
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className); //Create a ClassVisitor object to make changes to the class
                classReader.accept(change, ClassReader.EXPAND_FRAMES); //Accept the changes using the classReader
                code = classWriter.toByteArray(); //Convert the changed code into a Byte Array
                return code; //Return the Byte Array
            } catch (Exception | Error ignore) {
                System.out.println(ignore); //Print the exception or error message
            }
        } else {
            try {
                ClassReader reader = new ClassReader(inputStream); //Create a ClassReader object using the inputStream
                ClassWriter classWriter = new BallerinaClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES); //Create a BallerinaClassWriter object using the classReader with both COMPUTE_MAXS and COMPUTE_FRAMES
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className); //Create a ClassVisitor object to make changes to the class
                reader.accept(change, ClassReader.EXPAND_FRAMES); //Accept the changes using the classReader
                code = classWriter.toByteArray(); //Convert the changed code into a Byte Array
                return code; //Return the Byte Array
            } catch (Exception | Error e) {
                e.printStackTrace(); //Print the stack trace of the exception or error
            }
        }
        return null; //Return null if the code was not modified
    }

    // Print out the modified class code
    public static void printCode(String className, byte[] code) {
        int lastSlashIndex = className.lastIndexOf('/');
        String output = className.substring(0, lastSlashIndex);
        new File(output).mkdirs();
        try {
            //Create a FileOutputStream object using the className
            FileOutputStream fos = new FileOutputStream(className);
            fos.write(code); //Write the code to the output stream
            fos.close(); //Close the output stream
        } catch (IOException ignore) {}
    }
}