package app;

import profiler.Profiler;

import java.io.*;

import org.apache.commons.io.FileUtils;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static app.MethodWrapper.*;
import static parser.CpuParser.initializeCPUParser;
import static parser.MemoryParser.initializeMEMParser;

public class App {
    static String jarPathJava = null;   // variable to store the path of the jar file passed as an argument
    static ArrayList<String> changedPaths = new ArrayList<String>();

    public static void main(String[] args) throws Exception {

        if (!(args.length == 0)) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-f")) {
                    jarPathJava = args[i + 1];     // set the jar path variable
                }
            }
        }

        System.out.println("Loading....");

        unzipProfiler();
        shutDownHook();
        copyFile(jarPathJava,"temp.jar");;
        initialize(jarPathJava);
        invokeMethods();


    }

    private static void unzipProfiler() {
        try {
            ProcessBuilder pb = new ProcessBuilder("jar", "xf", "profiler.jar");
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void shutDownHook() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                ProcessBuilder pb3 = new ProcessBuilder("java", "-jar", "parser.jar");

                pb3.redirectErrorStream(true);
                Process p = pb3.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                p.waitFor();

                cleanUp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static void cleanUp() throws IOException {
        cleanFiles();
        for (String changedPath : changedPaths){
            cleanDirectory(changedPath);
        }
    }


    public static void cleanFiles() {
        File tempJar = new File("temp.jar");
        tempJar.delete();
    }

    public static void cleanDirectory(String pathNameToDelete) throws IOException {
        File deleteProfiler = new File(pathNameToDelete);
        if (deleteProfiler.exists()) {
            deleteDirectory(deleteProfiler);
        }
    }

    private static boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }

    public static void copyFile(String from, String to) throws IOException{

        File file = new File("temp.jar");
        if (file.exists()) {
            file.delete();
        }

        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        Files.copy(src.toFile().toPath(), dest.toFile().toPath());
    }


    private static void initialize(String jarPath) {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            findAllClassNames(jarPath, classNames);
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }

        // Create a JarFile object by passing in the path to the Jar file
        try (JarFile jarFile = new JarFile(jarPath)) {
//            periodicStop(period);
            URL[] urls = new URL[]{new File(jarPath).toURI().toURL()};  // Create an array of URLs, with a single URL being the Jar file's location
            URLClassLoader parentClassLoader = new URLClassLoader(urls);    // Create a new URLClassLoader using the array of URLs
            MethodWrapperClassLoader customClassLoader = new MethodWrapperClassLoader(parentClassLoader);   // Create a new URLClassLoader using the array of URLs
            String mainClassPackage = mainClassFinder(parentClassLoader);   // Find the main class package using the mainClassFinder method

            // Iterate through the class names in the "classNames" list
            for (String className : classNames) {
                byte[] code;
                InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className));   // Get an InputStream for the current class in the Jar file
                try {
                    assert mainClassPackage != null;
                    if (className.startsWith(mainClassPackage)) {
                        if (className.startsWith(mainClassPackage + "/$value$$anonType$_") || !className.endsWith("Frame.class") && !className.substring(className.lastIndexOf("/") + 1).startsWith("$") || className.endsWith("$_init.class")) {
                            code = modifyMethods(inputStream, mainClassPackage, className); // Modify the methods in the current class
                            printCode(mainClassPackage, className, code); // Print out the modified class code(DEBUG)
                        } else {
                            code = streamToByte(inputStream);   // Otherwise, just get the class code
                        }
                        classFiles.add(customClassLoader.loadClass(code));  // Load the class using the custom class loader and add it to the classFiles list
                    }
                } catch (IOException ignored) {
                    // Ignoring IOExceptions
                }
            }
        } catch (Exception | Error ignored) {
            // Ignoring Exceptions and Errors
        }

        addClassFiles();

    }

    private static void addClassFiles() {
        final File folder = new File(System.getProperty("user.dir"));
        listFilesForFolder(folder);
    }


    public static void listFilesForFolder(final File folder) {

        String absolutePath = Paths.get("temp.jar").toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll("temp.jar", "");

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                String replacedStr = String.valueOf(fileEntry);
                if (replacedStr.endsWith(".class")){
                    String replacedStr2 = replacedStr.replaceAll(absolutePath, "");

                    int index=replacedStr2.lastIndexOf('/');
                    replacedStr2 = replacedStr2.substring(0,index);

                    String parts[]=replacedStr2.split("/");
                    String beforeFirstDot = parts[0];

                    changedPaths.add(beforeFirstDot);

                    try {
                        ProcessBuilder pb3 = new ProcessBuilder("jar", "uf", "temp.jar", replacedStr2);
                        pb3.redirectErrorStream(true);
                        Process process = pb3.start();
                        process.waitFor();
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            }
        }
    }
}
