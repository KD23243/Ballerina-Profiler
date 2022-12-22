package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {

    private static final String FORMAT_STRING = "%-60s: TotalTime:- %6d ms, StackTrace:- %11s";
    private String name;
    private String trace;
    private long startTime;
    private long time;

    public Profile(String name, String trace) {
        this.name = name;
        this.trace = trace;
        this.startTime = 0L;
        this.time = Long.MAX_VALUE;
    }

    public void start() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public void stop() {
        this.time = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;
    }

    private String getFormattedStats(String format) {
        return String.format(format, this.name, this.time, this.trace);
    }

    public String toString() {
        return this.getFormattedStats(FORMAT_STRING);
    }
}
