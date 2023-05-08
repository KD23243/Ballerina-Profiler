package io.ballerina.runtime.profiler.util;

public class ExceptionP extends Exception {
    public ExceptionP() {
        System.out.println("My custom exception message.");
    }

    public ExceptionP(String message) {
        System.out.println(message);
    }

    public ExceptionP(Throwable cause) {
        System.out.println(cause);
    }

    public ExceptionP(String message, Throwable cause) {
        System.out.println(message + cause);
    }
}
