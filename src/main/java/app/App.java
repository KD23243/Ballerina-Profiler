package app;

import profiler.Profiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarFile;

import static app.MethodWrapper.*;
import static parser.CpuParser.initializeCPUParser;
import static parser.MemoryParser.initializeMEMParser;

public class App {
    static String jarPathJava = null;   // variable to store the path of the jar file passed as an argument
    static int period = 5000;   // variable to store the time period for the profiler to stop in milliseconds, set to 5000 by default

    public static void main(String[] args) {

        if (!(args.length == 0)) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-f")) {
                    jarPathJava = args[i + 1];     // set the jar path variable
                }
                if (args[i].equals("-t")) {
                    period = Integer.parseInt(args[i + 1]);   // set the period variable
                }
            }
        }

        shutDownHook();
        initialize(jarPathJava, period);

    }

    private static void shutDownHook() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            profilerStop();
            try {
                initializeCPUParser();
                initializeMEMParser();
            } catch (Exception ignore) {}
        }));
    }

    private static void periodicStop(int period) {
        // Create a new Timer object and Schedule a task to run periodically
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("(Profiling...)");
                profilerStop(); // Stop the profiler
                // Create a new thread
                new Thread(() -> {
                    try {
                        initializeCPUParser();
                        initializeMEMParser(); // Initialize the parser
                    } catch (Exception ignore) {}
                }).start();

            }
        }, 0, period);
    }

    private static void initialize(String jarPath, int period) {
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            findAllClassNames(jarPath, classNames);
        } catch (Exception e) {
            System.out.println("(No such file or directory)");
        }

        // Create a JarFile object by passing in the path to the Jar file
        try (JarFile jarFile = new JarFile(jarPath)) {
            periodicStop(period);
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
                            printCode(className, code); // Print out the modified class code(DEBUG)
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
        invokeMethods(classFiles);  // Call the invokeMethods method, passing in the classFiles list
    }

    // Method to stop the profiler and print the output
    private static void profilerStop() {
        // Get the singleton instance of the Profiler class
        Profiler profiler = Profiler.getInstance();

        // Print the profiler output to a file
        profiler.printProfilerOutput(profiler.toStringCpu(), "CpuPre");
        profiler.printProfilerOutput(profiler.toStringMem(), "MemPre");
    }
}
