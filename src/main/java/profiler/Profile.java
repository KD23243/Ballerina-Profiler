package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {

    private static final String FORMAT_STRING = "%-20.20s: Total Time: %5d ms, StackTrace: %5s";
    private String name;
    private String trace;
    private long startTime;
    private long totalTime;
    private long minTime;
    private long maxTime;

    public Profile(String name, String trace) {
        this.name = name;
        this.trace = trace;
    }

    public void start() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public void stop() {

        long elapsed = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;
        if (elapsed < this.minTime) {
            this.minTime = elapsed;
        }

        if (elapsed > this.maxTime) {
            this.maxTime = elapsed;
        }

        this.totalTime += elapsed;
    }

    private String getFormattedStats(String format) {
        return String.format(format, this.name, this.totalTime, this.trace);
    }

    public String toString() {
        return this.getFormattedStats(FORMAT_STRING);
    }
}
