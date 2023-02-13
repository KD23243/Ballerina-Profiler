package app;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


import static app.MethodWrapper.*;
import static parser.CpuParser.initializeCPUParser;


public class App {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_ORANGE = "\033[1;38;2;255;165;0m";
    public static final String ANSI_YELLOW = "\033[1;93m";

    public static int exitCode = 0;

    static String originArgs = null;
    static String jarPathJava = null;   // variable to store the path of the jar file passed as an argument
    static ArrayList<String> changedPaths = new ArrayList<String>();
    static ArrayList<String> changedFiles = new ArrayList<String>();

    public static void main(String[] args) throws Exception {

        shutDownHook();

        System.out.println();
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println(ANSI_ORANGE + "Ballerina Profiler"+ ANSI_RESET + ": Profiling...");
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println("WARNING : Ballerina Profiler is an experimental feature.");

        if (!(args.length == 0)) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-f")) {
                    jarPathJava = args[i + 1];     // set the jar path variable
                }else if (args[i].equals("-a")) {
                    originArgs = args[i + 1];     // set the jar path variable
                }
            }
        }

        extractProfiler();
        copyJar(jarPathJava,"temp.jar");;
        initialize(jarPathJava);
        invokeMethods();
    }

    private static void extractProfiler() {
        System.out.println(ANSI_ORANGE + "[1/7] Initializing Profiler..." + ANSI_RESET);
        try {
            ProcessBuilder pb = new ProcessBuilder("jar", "xvf", "Profiler.jar", "profiler");
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    public static void copyJar(String from, String to){
        System.out.println(ANSI_ORANGE + "[2/7] Copying Executable..." + ANSI_RESET);
        System.out.println(" ○ Source: " + from);
        System.out.println(" ○ Destination: " + to);

        File file = new File("temp.jar");
        if (file.exists()) {
            file.delete();
        }
        Path src = Paths.get(from);
        Path dest = Paths.get(to);
        try {
            Files.copy(src.toFile().toPath(), dest.toFile().toPath());
        }catch (Exception e){
            exitCode = 2;
            System.out.println("Error" + e.getMessage());
            System.exit(0);
        }
    }

    private static void initialize(String jarPath) {
        System.out.println(ANSI_ORANGE + "[3/7] Performing Analysis..." + ANSI_RESET);

        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            findAllClassNames(jarPath, classNames);
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }

        System.out.println(" ○ Classes Reachable: " + classNames.size());

        System.out.println(ANSI_ORANGE + "[4/7] Instrumenting Functions..." + ANSI_RESET);

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
                    String pathOrigin = mainClassPackage.split("/")[0];
                  if (className.startsWith(pathOrigin)) {
                        if (className.startsWith(mainClassPackage + "/$value$$anonType$_") || !className.endsWith("Frame.class") && !className.substring(className.lastIndexOf("/") + 1).startsWith("$") ) {

//                            System.out.println(className);

                            code = modifyMethods(inputStream, mainClassPackage, className); // Modify the methods in the current class
                            printCode(className, className, code); // Print out the modified class code(DEBUG)
                        } else {
                            code = streamToByte(inputStream);   // Otherwise, just get the class code
                        }
                        classFiles.add(customClassLoader.loadClass(code));  // Load the class using the custom class loader and add it to the classFiles list
                    }
                } catch (IOException ignored) {}
            }

            System.out.println(" ○ Classes Reached: " + classFiles.size());
        } catch (Exception | Error ignored) {}

        try {
            modifyTheJar();
            System.out.println();
        }catch (Exception | Error e){

        }
    }

    private static void modifyTheJar() {
        final File folder = new File(System.getProperty("user.dir"));
        listFilesForFolder(folder);
        System.out.println(" ○ Classes Changed: " + changedFiles.size());

        List<String> changedDirs = changedFiles.stream().distinct().collect(Collectors.toList());

        System.out.print(" ○ Directories Loaded: ");
        for (int i = 1; i < changedDirs.size() + 1; i++) {

            System.out.print(i);
            System.out.print("/" + changedDirs.size());
            overWrite(changedDirs.get(i));

            if (i<10){
                System.out.print("\b");
            }else if (i<100){
                System.out.print("\b\b");
            }else if (i<1000){
                System.out.print("\b\b\b");
            }

            System.out.print("\b\b\b");
        }
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
                    changedFiles.add(replacedStr2);

                }
            }
        }
    }

    private static void overWrite(String replacedStr2) {
        try {
            ProcessBuilder pb3 = new ProcessBuilder("jar", "uf", "temp.jar", replacedStr2);
            pb3.redirectErrorStream(true);
            Process process = pb3.start();
            process.waitFor();
        }catch (Exception e){
            System.out.println(e);
        }
    }


    private static void shutDownHook() {

        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                for (String changedPath : changedPaths){
                    FileUtils.deleteDirectory(new File(changedPath));
                }
                FileUtils.deleteDirectory(new File("profiler"));
                FileUtils.delete(new File("temp.jar"));
                System.out.println("\n" + ANSI_ORANGE + "[6/6] Generating Output..." + ANSI_RESET);
                Thread.sleep(1000);
                initializeCPUParser();
                FileUtils.delete(new File("CpuPre.json"));

                System.out.println("--------------------------------------------------------------------------------");
                System.out.println(ANSI_YELLOW + "Produced Artifacts" + ANSI_RESET);
                System.out.println(" ○ ProfilerOutput.json");
            } catch (Exception ignore) {}
        }));
    }
}
