package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {

    private static final String CPU_FORMAT_STRING = "{'\"time\"': '\"%s\"', '\"stackTrace\"': '%s'},"; // constant string format for JSON output
    private static final String MEM_FORMAT_STRING = "{'\"mem\"': '\"%s\"', '\"stackTrace\"': '%s'},"; // constant string format for JSON output
    private String name; // name of the profile
    private String trace; // trace of the profile
    private long startTime; // start time of the profile
    private long time; // time duration of the profile
    private String displayTime; // display time of the profile

    private long minTime;
    private long maxTime;
    private long totalTime;

    // constructor
    public Profile(String name, String trace) {
        this.name = name;
        this.trace = trace;
        this.startTime = 0L;
        this.time = Long.MAX_VALUE;

        this.minTime = Long.MAX_VALUE;
        this.maxTime = Long.MIN_VALUE;
        this.totalTime = 0L;
    }

    // starts the profile
    public void start() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    // stops the profile
    public void stop() {
//        this.totalTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;

        long elapsed = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;

        if (elapsed < this.minTime) {
            this.minTime = elapsed;
        }

        if (elapsed > this.maxTime) {
            this.maxTime = elapsed;
        }

        this.totalTime += elapsed;
    }

    // get formatted stats of the profile
    private String getFormattedStats(String format) {

        if (this.totalTime == Long.MAX_VALUE) {
            displayTime = "-1";
        } else {
            displayTime = String.valueOf(this.totalTime);
        }
//        return String.format(format, this.displayTime, this.trace);
        return String.format(format, displayTime, this.trace);
    }

    // returns the string representation of the cpu profile
    public String toStringCpu() {
        return this.getFormattedStats(CPU_FORMAT_STRING);
    }
}