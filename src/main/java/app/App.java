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
import static app.Parser.initializeParser;

public class App {

    static String jarPathJava = null;
    static int period = 5000;

    public static void main(String[] args) throws IOException {

        if (!(args.length == 0)){
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-f")){
                    jarPathJava = args[i+1];
                }
                if (args[i].equals("-t")){
                    period = Integer.parseInt(args[i+1]);
                }
            }
        }

        shutDownHook();
        initialize(jarPathJava,period);

    }

    private static void shutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            profilerStop();
            try {
                initializeParser();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static void periodicStop(int period) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("(Profiling...)");
                profilerStop();
            }
        }, 0, period);
    }

    private static void initialize(String jarPath, int period) throws IOException {
//        System.out.println("* Profiling Started *");

        //Class arrays
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();

        try {
            findAllClassNames(jarPath, classNames);
        }catch (Exception e){
            System.out.println("(No such file or directory)");
        }



        //Jar files
        try (JarFile jarFile = new JarFile(jarPath)) {
            periodicStop(period);
            URL[] urls = new URL[]{new File(jarPath).toURI().toURL()};



            //ClassLoaders
            URLClassLoader parentClassLoader = new URLClassLoader(urls);
            MethodWrapperClassLoader customClassLoader = new MethodWrapperClassLoader(parentClassLoader);

            //Finds the main class name from the manifest
            String mainClassPackage = mainClassFinder(parentClassLoader);

            for (String className : classNames) {

                byte[] code;


                InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className));

                try {
                    assert mainClassPackage != null;
                    if (className.startsWith(mainClassPackage)) {
                        if (!className.endsWith("Frame.class") && !className.substring(className.lastIndexOf("/") + 1).startsWith("$") || className.endsWith("$_init.class")) {
                            code = modifyMethods(inputStream, mainClassPackage, className);
                            printCode(className,code);
                        } else {
                            code = streamToByte(inputStream);
                        }
                        classFiles.add(customClassLoader.loadClass(code));
                    }
                } catch (IOException ignored) {

                }
            }
        }catch (Exception | Error ignored) {

        }
        invokeMethods(classFiles);

    }
    private static void profilerStop() {
        Profiler.getInstance().printProfilerOutput(Profiler.getInstance().toString(), "Output");
    }
}
