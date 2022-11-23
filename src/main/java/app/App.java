package app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarFile;

import static app.MethodWrapper.*;

public class App {
    public static void main(String[] args) throws IOException {
        //Fix comment
        String jarPathJava = "/home/wso2/Documents/scrapCode/balcode/test/target/bin/test.jar";
        initialize(jarPathJava);
    }

    private static void initialize(String jarPath) throws IOException {
        System.out.println("* Profiling Started *");

        //Class arrays
        ArrayList<Class<?>> classFiles = new ArrayList<>();
        ArrayList<String> classNames = new ArrayList<>();
        findAllClassNames(jarPath, classNames);

        //Jar files
        try (JarFile jarFile = new JarFile(jarPath)) {
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
                            code = modifyMethods(inputStream, mainClassPackage);
    //                        printCode(className,code);
                        } else {
                            code = streamToByte(inputStream);
                        }
                        classFiles.add(customClassLoader.loadClass(code));
                    }
                } catch (Exception | Error ignored) {

                }
            }
        }
        invokeMethods(classFiles);
    }
}
