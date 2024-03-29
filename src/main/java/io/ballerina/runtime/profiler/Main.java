package io.ballerina.runtime.profiler;

import io.ballerina.runtime.profiler.codegen.ClassLoaderP;
import io.ballerina.runtime.profiler.codegen.MethodWrapperP;
import io.ballerina.runtime.profiler.util.ExceptionP;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.ballerina.runtime.profiler.ui.JSONParser.initializeCPUParser;
import static io.ballerina.runtime.profiler.ui.HTTPServer.initializeServer;

public class Main {
    // Define ANSI escape codes for colored console output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_CYAN = "\033[1;38;2;32;182;176m";

    // Define public static variables for the program
    public static long profilerStartTime = 0;
    public static int exitCode = 0; // Exit code for the program
    public static String balJarArgs = null; // Original command line arguments for the JAR
    public static String balJarName = null; // Path to JAR file
    public static String skipFunctionString = null; // Path to JAR file

    public static ArrayList<String> instrumentedPaths = new ArrayList<>(); // Paths of instrumented JAR files
    public static ArrayList<String> instrumentedFiles = new ArrayList<>(); // Names of instrumented JAR files
    public static ArrayList<String> utilInitPaths = new ArrayList<>(); // Paths of utility JAR files
    public static ArrayList<String> utilPaths = new ArrayList<>(); // Additional utility JAR files

    public static void main(String[] args) throws ExceptionP {
        profilerStartTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        tempFileCleanupShutdownHook(); // Register a shutdown hook to handle graceful shutdown of the application
        printHeader(); // Print the program header
        handleProfilerArguments(args); // Handle command line arguments
        extractTheProfiler(); // Extract the profiler used by the program
        createTempJar(balJarName); // Create a temporary JAR file at the given location
        initialize(balJarName); // Initialize profiling
    }

    private static void printHeader() {
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Ballerina Profiler" + ANSI_RESET + ": Profiling...");
        System.out.println(ANSI_GRAY + "================================================================================" + ANSI_RESET);
        System.out.println("WARNING : Ballerina Profiler is an experimental feature.");
    }

    private static void handleProfilerArguments(String[] args) {
        if (!(args.length == 0)) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--file":
                        balJarName = args[i + 1];
                        if ((balJarName.startsWith("[") && balJarName.endsWith("]"))) {
                            balJarName = balJarName.substring(1, balJarName.length() - 1);
                        }else {
                            System.out.println("Invalid CLI Argument");
                            System.exit(0);
                        }
                        break;
                    case "--args":
                        balJarArgs = args[i + 1];
                        if (balJarArgs != null && balJarArgs.startsWith("[") && balJarArgs.endsWith("]")) {
                            balJarArgs = balJarArgs.substring(1, balJarArgs.length() - 1);
                        }else {
                            System.out.println("Invalid CLI Argument");
                            System.exit(0);
                        }
                        break;
                    case "--skip":
                        skipFunctionString = args[i + 1];
                        if (skipFunctionString != null && skipFunctionString.startsWith("[") && skipFunctionString.endsWith("]")) {
                            skipFunctionString = skipFunctionString.substring(1, skipFunctionString.length() - 1);
                        }else {
                            System.out.println("Invalid CLI Argument");
                            System.exit(0);
                        }
                        break;
                }
            }
        }
    }

    private static void extractTheProfiler() throws ExceptionP {
        System.out.println(ANSI_CYAN + "[1/7] Initializing Profiler..." + ANSI_RESET);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "xvf", "Profiler.jar", "io/ballerina/runtime/profiler/runtime");
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException exception) {
            throw new ExceptionP(exception);
        }
    }

    public static void createTempJar(String balJarName) {
        try {
            System.out.println(ANSI_CYAN + "[2/7] Copying Executable..." + ANSI_RESET);
            System.out.println(" ○ Source: " + balJarName);
            Path sourcePath = Paths.get(balJarName);
            Path destinationPath = Paths.get("temp.jar");
            Files.copy(sourcePath.toFile().toPath(), destinationPath.toFile().toPath());
        } catch (Exception exception) {
            exitCode = 2;
            System.err.println("Invalid File Name");
            System.exit(0);
        }
    }

    private static void initialize(String balJarName) throws ExceptionP {
        System.out.println(ANSI_CYAN + "[3/7] Performing Analysis..." + ANSI_RESET);

        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            findAllClassNames(balJarName, classNames);
            findUtilityClasses(classNames);
            System.out.println(" ○ Java Classes Reachable: " + classNames.size());
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }

        System.out.println(ANSI_CYAN + "[4/7] Instrumenting Functions..." + ANSI_RESET);

        try (JarFile jarFile = new JarFile(balJarName)) {
            String mainClassPackage = MethodWrapperP.mainClassFinder(new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            ClassLoaderP customClassLoader = new ClassLoaderP(new URLClassLoader(new URL[]{new File(balJarName).toURI().toURL()}));
            Set<String> usedPaths = new HashSet<>();

            for (String className : classNames) {
                if (mainClassPackage == null) continue;
                if (className.startsWith(mainClassPackage.split("/")[0]) || utilPaths.contains(className)) {
                    try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
                        byte[] code = MethodWrapperP.modifyMethods(inputStream);
                        classFiles.add(customClassLoader.loadClass(code));
                        usedPaths.add(className.replace(".class", "").replace("/", "."));
                        MethodWrapperP.printCode(className, code);
                    }
                }
            }

            try (PrintWriter printWriter = new PrintWriter("usedPathsList.txt")) {
                printWriter.println(String.join(", ", usedPaths));
            }
            System.out.println(" ○ Java Classes Reached: " + classFiles.size());
        } catch (Throwable throwable) {
            throw new ExceptionP(throwable);
        }

        try {
            modifyTheJar();
        } catch (Throwable throwable) {
            throw new ExceptionP(throwable);
        }
    }

    private static void modifyTheJar() throws InterruptedException, IOException {
        try {
            final File userDirectory = new File(System.getProperty("user.dir")); // Get the user directory
            listAllFiles(userDirectory); // List all files in the user directory and its subdirectories
            List<String> changedDirectories = instrumentedFiles.stream().distinct().collect(Collectors.toList()); // Get a list of the directories containing instrumented files
            System.out.println(" ○ Modified Java Classes: " + instrumentedFiles.size()); // Print the number of instrumented files
            loadDirectories(changedDirectories);
        } finally {
            for (String instrumentedFilePath : instrumentedPaths) {
                FileUtils.deleteDirectory(new File(instrumentedFilePath));
            }
            FileUtils.deleteDirectory(new File("io/ballerina/runtime/profiler/runtime"));
            MethodWrapperP.invokeMethods();
        }
    }

    private static void loadDirectories(List<String> changedDirs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("jar", "uf", "temp.jar");
            processBuilder.command().addAll(changedDirs);
            processBuilder.start().waitFor();
            System.out.println(" ○ Directories Loaded: " + (changedDirs.size() - 1));
        } catch (Exception e) {
            System.err.println("Error loading directories: " + e.getMessage());
        }
    }

    public static void listAllFiles(final File userDirectory) {
        String absolutePath = Paths.get("temp.jar").toFile().getAbsolutePath();
        absolutePath = absolutePath.replaceAll("temp.jar", "");
        for (final File fileEntry : userDirectory.listFiles()) {
            if (fileEntry.isDirectory()) {
                listAllFiles(fileEntry);
            }
            else {
                String fileEntryString = String.valueOf(fileEntry);
                if (fileEntryString.endsWith(".class")) {
                    fileEntryString = fileEntryString.replaceAll(absolutePath, "");
                    int index = fileEntryString.lastIndexOf('/');
                    fileEntryString = fileEntryString.substring(0, index);
                    String[] fileEntryParts = fileEntryString.split("/");
                    instrumentedPaths.add(fileEntryParts[0]);
                    instrumentedFiles.add(fileEntryString);
                }
            }
        }
    }

    private static void findAllClassNames(String jarPath, ArrayList<String> classNames) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarPath)); // Create a ZipInputStream to read the jar file
        for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                classNames.add(String.valueOf(entry));
            }
        }
    }

    private static void findUtilityClasses(ArrayList<String> classNames) {
        for (String className : classNames) {
            if (className.endsWith("$_init.class")) {
                String path = className.substring(0, className.lastIndexOf('/') + 1);
                if (!utilInitPaths.contains(path)) {
                    utilInitPaths.add(path);
                }
            }
        }

        for (String name : classNames) {
            for (String path : utilInitPaths) {
                if (name.startsWith(path)) {
                    String subPath = name.substring(path.length());
                    if (subPath.indexOf('/') == -1) {
                        utilPaths.add(name);
                    }
                }
            }
        }
    }


    private static void deleteTempData() {
        String filePrefix = "jartmp";
        File[] files = new File(System.getProperty("user.dir")).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(filePrefix)) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }
    }


    private static void tempFileCleanupShutdownHook() {
        // Add a shutdown hook to stop the profiler and parse the output when the program is closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                long profilerTotalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - profilerStartTime;
                FileUtils.delete(new File("temp.jar"));
                System.out.println("\n" + ANSI_CYAN + "[6/6] Generating Output..." + ANSI_RESET);
                Thread.sleep(100);
                initializeCPUParser(skipFunctionString);
                FileUtils.delete(new File("usedPathsList.txt"));
                FileUtils.delete(new File("CpuPre.json"));
                System.out.println(" ○ Execution Time: " + profilerTotalTime / 1000 + " Seconds");
                deleteTempData();
                initializeServer();
                FileUtils.delete(new File("performance_report.json"));
                System.out.println("--------------------------------------------------------------------------------");
            } catch (Exception ignore) {}
        }));
    }
}

//handle try catch