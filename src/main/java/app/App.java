package app;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static app.MethodWrapper.*;
import static parser.CpuParser.initializeCPUParser;

public class App {
    // Define ANSI escape codes for colored console output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_ORANGE = "\033[1;38;2;255;165;0m";
    public static final String ANSI_YELLOW = "\033[1;93m";

    // Define public static variables for the program
    public static int exitCode = 0; // Exit code for the program
    public static boolean showAll = true; // Whether to show all output or not
    public static String unwantedString = null; // String of all the function to exclude from output
    public static String originArgs = null; // Original command line arguments for the JAR
    public static String jarPathJava = null; // Path to JAR file

    public static ArrayList < String > instrumentedPaths = new ArrayList < > (); // Paths of instrumented JAR files
    public static ArrayList < String > instrumentedFiles = new ArrayList < > (); // Names of instrumented JAR files
    public static ArrayList < String > utilInitPaths = new ArrayList < > (); // Paths of utility JAR files
    public static ArrayList < String > utilPaths = new ArrayList < > (); // Additional utility JAR files
    public static String[] unwantedPaths = null; // Paths to exclude from instrumenting

    public static void main(String[] args) throws Exception {
        shutDownHookApp(); // Register a shutdown hook to handle graceful shutdown of the application
        printHeader(); // Print the program header
        handleArguments(args); // Handle command line arguments
        extractTheProfiler(); // Extract the profiler used by the program
        createTheTempJar(jarPathJava); // Create a temporary JAR file at the given location
        initialize(jarPathJava); // Initialize profiling
        invokeMethods(); // Invoke the methods in the JAR file
    }

    private static void printHeader() {
        System.out.println();
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println(ANSI_ORANGE + "Ballerina Profiler" + ANSI_RESET + ": Profiling...");
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println("WARNING : Ballerina Profiler is an experimental feature.");
    }

    private static void handleArguments(String[] args) throws FileNotFoundException {
        // Check if there are any arguments passed
        if (!(args.length == 0)) {
            // Loop through each argument
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    // Set the path to the JAR file
                    case "--filename":
                        jarPathJava = args[i + 1];
                        break;
                    // Set the original command line arguments
                    case "--arguments":
                        originArgs = args[i + 1];
                        break;
                    // Set the unwanted string to exclude from output and store the paths to skip in a file
                    case "--skip":
                        unwantedString = args[i + 1];
                        unwantedPaths = unwantedString.split(",");
                        PrintWriter pr = new PrintWriter("skippedPaths.txt");
                        for (int x = 0; x < unwantedPaths.length; x++) {
                            pr.println(unwantedPaths[x]);
                        }
                        pr.close();
                        break;
                    // Set the program to only show minimum output
                    case "--minimum":
                        showAll = false;
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
        ArrayList < Class < ? >> classFiles = new ArrayList < > ();
        ArrayList < String > classNames = new ArrayList < > ();

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

            URL[] urls = new URL[] {
                    new File(jarPath).toURI().toURL()
            }; // Create an array of URLs, with a single URL being the Jar file's location
            URLClassLoader parentClassLoader = new URLClassLoader(urls); // Create a new URLClassLoader using the array of URLs
            MethodWrapperClassLoader customClassLoader = new MethodWrapperClassLoader(parentClassLoader); // Create a new URLClassLoader using the array of URLs
            String mainClassPackage = mainClassFinder(parentClassLoader); // Find the main class package using the mainClassFinder method

            // Iterate through the class names in the "classNames" list
            for (String className: classNames) {

                byte[] code;
                InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className)); // Get an InputStream for the current class in the Jar file
                try {
                    assert mainClassPackage != null;
                    String pathOrigin = mainClassPackage.split("/")[0];

                    boolean baseSatisfied = className.startsWith(mainClassPackage + "/$value$$anonType$_") || !className.endsWith("Frame.class") && !className.substring(className.lastIndexOf("/") + 1).startsWith("$");

                    if (showAll) {
                        if (className.startsWith(pathOrigin) || utilPaths.contains(className)) {
                            if (baseSatisfied) {
                                code = modifyMethods(inputStream, mainClassPackage, className); // Modify the methods in the current class
                                printCode(className, className, code); // Print out the modified class code(DEBUG)
                            } else {
                                code = streamToByte(inputStream); // Otherwise, just get the class code
                            }
                            classFiles.add(customClassLoader.loadClass(code)); // Load the class using the custom class loader and add it to the classFiles list
                        }
                    } else {
                        if (className.startsWith(pathOrigin)) {
                            if (baseSatisfied) {
                                code = modifyMethods(inputStream, mainClassPackage, className); // Modify the methods in the current class
                                printCode(className, className, code); // Print out the modified class code(DEBUG)
                            } else {
                                code = streamToByte(inputStream); // Otherwise, just get the class code
                            }
                            classFiles.add(customClassLoader.loadClass(code)); // Load the class using the custom class loader and add it to the classFiles list
                        }
                    }

                } catch (IOException ignored) {}
            }

            System.out.println(" ○ Classes Reached: " + classFiles.size());
        } catch (Exception | Error ignored) {}
        try {
            modifyTheJar(); //Modify the JAR file by overwriting the original files with the instrumented files.
        } catch (Exception | Error ignored) {}
    }

    private static void modifyTheJar() {
        final File userDir = new File(System.getProperty("user.dir")); // Get the user directory
        listAllFiles(userDir); // List all files in the user directory and its subdirectories
        System.out.println(" ○ Classes Changed: " + instrumentedFiles.size()); // Print the number of instrumented files

        List < String > changedDirs = instrumentedFiles.stream().distinct().collect(Collectors.toList()); // Get a list of the directories containing instrumented files

        System.out.print(" ○ Directories Loaded: ");
        for (int i = 0; i < changedDirs.size() + 1; i++) { // Iterate through the directories
            System.out.print("*"); // Print a * for each directory loaded
            overWriteJar(changedDirs.get(i)); // Overwrite the JAR file with the instrumented files in the current directory
        }
    }

    public static void listAllFiles(final File userDirectory) {

        // Get the absolute path to the temporary jar file
        String absolutePath = Paths.get("temp.jar").toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll("temp.jar", "");

        // Iterate through all files and directories in the userDirectory
        for (final File fileEntry: userDirectory.listFiles()) {

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

    private static void findAllClassNames(String jarPath, ArrayList < String > classNames) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(jarPath)); // Create a ZipInputStream to read the jar file
        // Iterate through all the entries in the jar file
        for (ZipEntry entry = zipFile.getNextEntry(); entry != null; entry = zipFile.getNextEntry()) {
            // Check if the entry is a directory or if it's a class file
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry)); // If it's a class file, add its name to the classNames list
            }
        }
    }

    private static void findUtilClasses(ArrayList < String > classNames) {

        // Look for utility initialization classes and add their paths to the utilInitPaths list.
        for (String className: classNames) {
            if (className.endsWith("$_init.class")) {
                int lastSlashIndex = className.lastIndexOf('/');
                String output = className.substring(0, lastSlashIndex) + "/";
                utilInitPaths.add(output);
            }
        }

        // Remove duplicates from the utilInitPaths list.
        utilInitPaths = (ArrayList < String > ) utilInitPaths.stream().distinct().collect(Collectors.toList());

        // Look for other utility classes and add their paths to the utilPaths list.
        for (String s1: classNames) {
            for (String s2: utilInitPaths) {
                if (s1.startsWith(s2)) {
                    String process = s1.replace(s2, "");
                    if (!process.contains("/")) {
                        utilPaths.add(s1);
                    }
                }
            }
        }
    }

    private static void overWriteJar(String changedDirs) {
        try {
            // Use ProcessBuilder to run the command and redirect its output to the console
            ProcessBuilder pb3 = new ProcessBuilder("jar", "uf", "temp.jar", changedDirs);
            pb3.redirectErrorStream(true);
            Process process = pb3.start();
            // Wait for the process to finish before continuing
            process.waitFor();
        } catch (Exception ignored) {}
    }

    private static void shutDownHookApp() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Delete the instrumented directories.
            try {
                for (String instrumentedPath: instrumentedPaths) {
                    FileUtils.deleteDirectory(new File(instrumentedPath));
                }
                // Delete the profiler and temporary jar directories.
                FileUtils.deleteDirectory(new File("profiler"));
                FileUtils.delete(new File("temp.jar"));
                System.out.println("\n" + ANSI_ORANGE + "[6/6] Generating Output..." + ANSI_RESET);
                Thread.sleep(1000);
                initializeCPUParser(); // Initialize the CPU parser.
                // Delete the skipped paths text file and CPU pre JSON file.
                FileUtils.delete(new File("skippedPaths.txt"));
                FileUtils.delete(new File("CpuPre.json"));
                // Print the produced artifacts.
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println(ANSI_YELLOW + "Produced Artifacts" + ANSI_RESET);
            } catch (Exception ignore) {}
        }));
    }
}