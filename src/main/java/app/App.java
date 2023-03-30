package app;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static app.MethodWrapper.*;
import static parser.CpuParser.initializeCPUParser;
import static server.ProfilerServer.initServer;

public class App {
    // Define ANSI escape codes for colored console output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_ORANGE = "\033[1;38;2;255;165;0m";

    // Define public static variables for the program
    public static long profilerStartTime = 0;
    public static int exitCode = 0; // Exit code for the program
    public static String originArgs = null; // Original command line arguments for the JAR
    public static String jarPathJava = null; // Path to JAR file
    public static String skipString = "kd"; // Path to JAR file

    public static ArrayList<String> instrumentedPaths = new ArrayList<>(); // Paths of instrumented JAR files
    public static ArrayList<String> instrumentedFiles = new ArrayList<>(); // Names of instrumented JAR files
    public static ArrayList<String> utilInitPaths = new ArrayList<>(); // Paths of utility JAR files
    public static ArrayList<String> utilPaths = new ArrayList<>(); // Additional utility JAR files
    public static ArrayList<String> usedPaths = new ArrayList<>(); // Additional utility JAR files
    public static Map<String, byte[]> modifiedClassDef = new HashMap<String, byte[]>();

    public static void main(String[] args) {
        profilerStartTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        shutDownHookApp(); // Register a shutdown hook to handle graceful shutdown of the application
        printHeader(); // Print the program header
        handleArguments(args); // Handle command line arguments
        extractTheProfiler(); // Extract the profiler used by the program
        createTheTempJar(jarPathJava); // Create a temporary JAR file at the given location
        initialize(jarPathJava); // Initialize profiling
    }

    private static void printHeader() {
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println(ANSI_ORANGE + "Ballerina Profiler" + ANSI_RESET + ": Profiling...");
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println("WARNING : Ballerina Profiler is an experimental feature.");
    }

    private static void handleArguments(String[] args) {
        // Check if there are any arguments passed
        if (!(args.length == 0)) {
            // Loop through each argument
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    // Set the path to the JAR file
                    case "--file":
                        jarPathJava = args[i + 1];
                        break;
                    // Set the original command line arguments
                    case "--args":
                        originArgs = args[i + 1];
                        break;
                    case "--skip":
                        skipString = args[i + 1];
                        break;
                }
            }
        }
    }

    // Extract the Profiler JAR file
    private static void extractTheProfiler() {
        // Print a message indicating that the Profiler is being initialized
        System.out.println(ANSI_ORANGE + "[1/7] Initializing Profiler..." + ANSI_RESET);
        try {
            // Create a new ProcessBuilder to execute the command
            ProcessBuilder pb = new ProcessBuilder("jar", "xvf", "Profiler.jar", "profiler");
            // Start the process
            Process process = pb.start();
            // Wait for the process to finish before continuing
            process.waitFor();
        } catch (IOException | InterruptedException ignore) {
            // Ignore any exceptions that occur and continue with the program
        }
    }

    // Create a temporary JAR file by copying the source JAR file
    public static void createTheTempJar(String from) {
        try {
            // Print a message indicating that the executable is being copied
            System.out.println(ANSI_ORANGE + "[2/7] Copying Executable..." + ANSI_RESET);
            System.out.println(" ○ Source: " + from);

            // Delete the temporary JAR file if it already exists
            File file = new File("temp.jar");
            if (file.exists()) {
                file.delete();
            }

            // Create Path objects for the source and destination files
            Path src = Paths.get(from);
            Path dest = Paths.get("temp.jar");

            // Copy the source file to the destination file
            Files.copy(src.toFile().toPath(), dest.toFile().toPath());
        } catch (Exception e) {
            // Set the exit code to 2 and print an error message if an exception occurs
            exitCode = 2;
            System.out.println("Error" + e.getMessage());
            System.exit(0);
        }
    }

    private static void initialize(String jarPath) {

        //Step 1: Performing Analysis

        // Print a message that analysis is being performed
        System.out.println(ANSI_ORANGE + "[3/7] Performing Analysis..." + ANSI_RESET);

        // Create two empty ArrayLists to hold class files and class names
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        // Attempt to find all class names in the given jar file and then find util classes
        try {
            findAllClassNames(jarPath, classNames);
            findUtilClasses(classNames);
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }
        System.out.println(" ○ Classes Reachable: " + classNames.size()); // Print out the number of classes reachable

        //Step 2: Instrumenting Functions
        System.out.println(ANSI_ORANGE + "[4/7] Instrumenting Functions..." + ANSI_RESET);

        // Create a JarFile object by passing in the path to the Jar file
        try (JarFile jarFile = new JarFile(jarPath)) {

            URL[] urls = new URL[]{new File(jarPath).toURI().toURL()}; // Create an array of URLs, with a single URL being the Jar file's location

            URLClassLoader parentClassLoader = new URLClassLoader(urls); // Create a new URLClassLoader using the array of URLs
            MethodWrapperClassLoader customClassLoader = new MethodWrapperClassLoader(parentClassLoader); // Create a new URLClassLoader using the array of URLs
            String mainClassPackage = mainClassFinder(parentClassLoader); // Find the main class package using the mainClassFinder method

            // Iterate through the class names in the "classNames" list
            for (String className : classNames) {
                byte[] code;
                InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className)); // Get an InputStream for the current class in the Jar file
                try {
                    assert mainClassPackage != null;
                    String pathOrigin = mainClassPackage.split("/")[0];

                    boolean baseSatisfied = className.startsWith(mainClassPackage + "/$value$$anonType$_") || !className.endsWith("asd");

                    //fix this
                    if (className.startsWith(pathOrigin) || utilPaths.contains(className)) {
                        String replacedPath = className.replace(".class", "");
                        replacedPath = replacedPath.replace("/", ".");
                        usedPaths.add(replacedPath);
                        if (baseSatisfied) {
                            code = modifyMethods(inputStream, mainClassPackage, className); // Modify the methods in the current class
                            printCode(className, code); // Print out the modified class code
                            modifiedClassDef.put(className, code);
                        } else {
                            code = streamToByte(inputStream); // Otherwise, just get the class code
                        }
                        classFiles.add(customClassLoader.loadClass(code)); // Load the class using the custom class loader and add it to the classFiles list
                    }
                } catch (IOException ignored) {
                    System.out.println(ignored);
                }
            }
            PrintWriter pr1 = new PrintWriter("usedPaths.txt");
            String listString = String.join(", ", usedPaths);
            pr1.println(listString);
            pr1.close();
            System.out.println(" ○ Classes Reached: " + classFiles.size());
        } catch (Exception | Error ignored) {}
        try {
            modifyTheJar(); //Modify the JAR file by overwriting the original files with the instrumented files.

        } catch (Exception | Error ignored) {}
    }

    private static void modifyTheJar() throws InterruptedException, IOException {
        try {
            final File userDir = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDir); // List all files in the user directory and its subdirectories
            List<String> changedDirs = instrumentedFiles.stream().distinct().collect(Collectors.toList()); // Get a list of the directories containing instrumented files
            System.out.println(" ○ Classes Modified: " + instrumentedFiles.size()); // Print the number of instrumented files
            loadDirectories(changedDirs);
        }finally {
            for (String instrumentedPath : instrumentedPaths) {
                FileUtils.deleteDirectory(new File(instrumentedPath));
            }
            // Delete the profiler and temporary jar directories.
            FileUtils.deleteDirectory(new File("profiler"));
            invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        int changedDirsSize = changedDirs.size() - 1;
        try {
            List<String> command = new ArrayList<>();
            command.add("jar");
            command.add("uf");
            command.add("temp.jar");
            command.addAll(changedDirs);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.start().waitFor();
            System.out.println(" ○ Directories Loaded: " + changedDirsSize);
        } catch (Exception e) {
            System.err.println("Error loading directories: " + e.getMessage());
        }
    }

    public static void listAllFiles(final File userDirectory) {

        // Get the absolute path to the temporary jar file
        String absolutePath = Paths.get("temp.jar").toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll("temp.jar", "");

        // Iterate through all files and directories in the userDirectory
        for (final File fileEntry : userDirectory.listFiles()) {

            // If the fileEntry is a directory, recursively call this method to check for any nested files
            if (fileEntry.isDirectory()) {
                listAllFiles(fileEntry);
            }
            // If the fileEntry is a file
            else {
                // Check if the file is a class file
                String fileEntryString = String.valueOf(fileEntry);
                if (fileEntryString.endsWith(".class")) {

                    // Remove the absolute path to the userDirectory from the fileEntryString to get the path relative to the userDirectory
                    fileEntryString = fileEntryString.replaceAll(absolutePath, "");

                    // Get the path to the directory containing the class file
                    int index = fileEntryString.lastIndexOf('/');
                    fileEntryString = fileEntryString.substring(0, index);

                    // Split the path into individual directory names
                    String[] fileEntryParts = fileEntryString.split("/");

                    // Add the directory name and the fileEntryString to their respective lists
                    instrumentedPaths.add(fileEntryParts[0]);
                    instrumentedFiles.add(fileEntryString);

                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(jarPath)); // Create a ZipInputStream to read the jar file
        // Iterate through all the entries in the jar file
        for (ZipEntry entry = zipFile.getNextEntry(); entry != null; entry = zipFile.getNextEntry()) {
            // Check if the entry is a directory or if it's a class file
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry)); // If it's a class file, add its name to the classNames list
            }
        }
    }

    private static void findUtilClasses(ArrayList<String> classNames) {

        // Look for utility initialization classes and add their paths to the utilInitPaths list.
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                int lastSlashIndex = className.lastIndexOf('/');
                String output = className.substring(0, lastSlashIndex) + "/";
                utilInitPaths.add(output);
            }
        }

        // Remove duplicates from the utilInitPaths list.
        utilInitPaths = (ArrayList<String>) utilInitPaths.stream().distinct().collect(Collectors.toList());

        // Look for other utility classes and add their paths to the utilPaths list.
        for (String s1 : classNames) {
            for (String s2 : utilInitPaths) {
                if (s1.startsWith(s2)) {
                    String process = s1.replace(s2, "");
                    if (!process.contains("/")) {
                        utilPaths.add(s1);
                    }
                }
            }
        }
    }

    private static void shutDownHookApp() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Delete the instrumented directories.
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - profilerStartTime;

                FileUtils.delete(new File("temp.jar"));
                System.out.println("\n" + ANSI_ORANGE + "[6/6] Generating Output..." + ANSI_RESET);
                Thread.sleep(1000);
                initializeCPUParser(skipString); // Initialize the CPU parser.
                // Delete the used paths text file and CPU pre JSON file.
                FileUtils.delete(new File("usedPaths.txt"));
                FileUtils.delete(new File("CpuPre.json"));
                System.out.println(" ○ Execution Time: " + profilerTotalTime/1000 + " Seconds");
                deleteTmpData();
                initServer();
//                FileUtils.delete(new File("performance_report.json"));
                System.out.println("--------------------------------------------------------------------------------");

            } catch (Exception ignore) {}
        }));
    }

    private static void deleteTmpData() {
        String directoryPath = System.getProperty("user.dir");
        String filePrefix = "jartmp";

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.getName().startsWith(filePrefix)) {
                FileUtils.deleteQuietly(file);
            }
        }
    }
}
