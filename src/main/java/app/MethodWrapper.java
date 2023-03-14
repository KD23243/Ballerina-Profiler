package app;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import wrapper.MethodWrapperVisitor;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import static app.App.*;

public class MethodWrapper extends ClassLoader {

    public static void invokeMethods() throws IOException, InterruptedException {

        // If originArgs is not null, create a ProcessBuilder object with arguments, start it, and read the output.
        if (originArgs != null) {
            ProcessBuilder pb3 = new ProcessBuilder("java", "-jar", "temp.jar", originArgs);
            pb3.redirectErrorStream(true);
            Process p = pb3.start();
            p.waitFor();
            System.out.println(ANSI_ORANGE + "[5/6] Running Executable..." + ANSI_RESET);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
        }
        // If originArgs is null, create a ProcessBuilder object without arguments, start it, and read the output.
        else {
            ProcessBuilder pb6 = new ProcessBuilder("java", "-jar", "temp.jar");
            pb6.redirectErrorStream(true);
            Process p1 = pb6.start();
            System.out.println(ANSI_ORANGE + "[5/6] Running Executable..." + ANSI_RESET);
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            p1.waitFor();
        }
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF"); // Find the URL of the manifest file
            Manifest manifest = new Manifest(manifestURL.openStream()); // Create a new Manifest object using the input stream from the manifest file
            Attributes attributes = manifest.getMainAttributes(); // Get the main attributes of the manifest

            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/"); // Return the value of the Main-Class attribute, replacing any ".$_init" and "." with "/"
        } catch (Exception | Error ignore) {
            System.out.println(ignore); // Print the error message if any exception or error occurs and return null if the main class could not be found
            return null;
        }
    }

    // This method takes an InputStream as a parameter and converts it to a byte array.
    public static byte[] streamToByte(InputStream stream) throws IOException {
        byte[] byteArray = stream.readAllBytes(); // The readAllBytes() method reads all the bytes from the input stream and returns them in a byte array.
        return byteArray; // The byte array is then returned
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

    // Print out the modified class code(DEBUG)
    public static void printCode(String className, byte[] code) {
        String pathName = className;
        int lastSlashIndex = pathName.lastIndexOf('/');
        String output = pathName.substring(0, lastSlashIndex);
        new File(output).mkdirs();
        try {
            //Create a FileOutputStream object using the className
            FileOutputStream fos = new FileOutputStream(pathName);
            fos.write(code); //Write the code to the output stream
            fos.close(); //Close the output stream
        } catch (IOException ignore) {
            // Ignoring IOExceptions
        }
    }
}