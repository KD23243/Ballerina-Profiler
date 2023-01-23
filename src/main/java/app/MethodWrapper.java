package app;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
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

public class MethodWrapper extends ClassLoader {

    public static void invokeMethods(ArrayList<Class<?>> classFiles) {
        Object[] arguments = new Object[1];
        arguments[0] = new String[]{};


        //TODO inject and see if it's there

//        System.getenv().get(arguments);

        // Iterate through each Class object in the ArrayList
        for (Class<?> classFile : classFiles) {
            // Check if the name of the Class is "$_init"
            if (classFile.getName().endsWith("$_init")) {
                // Try to invoke the "main" method of the Class with the specified arguments
                try {
                    classFile.getDeclaredMethod("main", String[].class).invoke(null, arguments);
                } catch (Error | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println(e);  // If an exception is thrown, print the exception message
                }
            }
        }
    }

    public static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(jarPath));  // Create a ZipInputStream to read the jar file
        // Iterate through all the entries in the jar file
        for (ZipEntry entry = zipFile.getNextEntry(); entry != null; entry = zipFile.getNextEntry()) {
            // Check if the entry is a directory or if it's a class file
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry));  // If it's a class file, add its name to the classNames list
            }
        }
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF"); // Find the URL of the manifest file
            Manifest manifest = new Manifest(manifestURL.openStream()); // Create a new Manifest object using the input stream from the manifest file
            Attributes attributes = manifest.getMainAttributes();   // Get the main attributes of the manifest
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");  // Return the value of the Main-Class attribute, replacing any ".$_init" and "." with "/"
        } catch (Exception | Error ignore) {
            System.out.println(ignore); // Print the error message if any exception or error occurs and return null if the main class could not be found
            return null;
        }
    }

    // This method takes an InputStream as a parameter and converts it to a byte array.
    public static byte[] streamToByte(InputStream stream) throws IOException {
        byte[] byteArray = stream.readAllBytes();   // The readAllBytes() method reads all the bytes from the input stream and returns them in a byte array.
        return byteArray;   // The byte array is then returned
    }

    public static byte[] modifyMethods(InputStream inputStream, String mainClassPackage, String className) throws IOException {
        byte[] code;
        if (className.endsWith("init.class")) {
            try {
                ClassReader classReader = new ClassReader(inputStream); //Create a ClassReader object using the inputStream
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);   //Create a ClassWriter object using the classReader with only COMPUTE_MAXS
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className);   //Create a ClassVisitor object to make changes to the class
                classReader.accept(change, ClassReader.EXPAND_FRAMES);  //Accept the changes using the classReader
                code = classWriter.toByteArray();   //Convert the changed code into a Byte Array
                return code;    //Return the Byte Array

            } catch (Exception | Error ignore) {
                System.out.println(ignore); //Print the exception or error message
            }
        } else {
            try {
                ClassReader reader = new ClassReader(inputStream);  //Create a ClassReader object using the inputStream
                ClassWriter classWriter = new BallerinaClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);  //Create a BallerinaClassWriter object using the classReader with both COMPUTE_MAXS and COMPUTE_FRAMES
                ClassVisitor change = new MethodWrapperVisitor(classWriter, mainClassPackage, className);   //Create a ClassVisitor object to make changes to the class
                reader.accept(change, ClassReader.EXPAND_FRAMES);   //Accept the changes using the classReader
                code = classWriter.toByteArray();   //Convert the changed code into a Byte Array
                return code;    //Return the Byte Array
            } catch (Exception | Error e) {
                e.printStackTrace();    //Print the stack trace of the exception or error
            }
        }
        return null;    //Return null if the code was not modified
    }

    // Print out the modified class code(DEBUG)
    public static void printCode(String className, byte[] code) {
        try {
            //Create a FileOutputStream object using the className
            FileOutputStream fos = new FileOutputStream("debug/" + className.substring(className.lastIndexOf("/") + 1));
            fos.write(code);    //Write the code to the output stream
            fos.close();    //Close the output stream
        } catch (IOException ignore) {
            // Ignoring IOExceptions
        }
    }
}