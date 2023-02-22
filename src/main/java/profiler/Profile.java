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
    private int memoryUsage;
    // constructor
    public Profile(String name, String trace) {
        this.name = name;
        this.trace = trace;
        this.startTime = 0L;
        this.time = Long.MAX_VALUE;
    }

    // starts the profile
    public void start() {
        this.startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        this.memoryUsage = ((int)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
    }

    // stops the profile
    public void stop() {
        this.time = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;
    }

    // get formatted stats of the profile
    private String getFormattedStats(String format) {

        if (this.time == Long.MAX_VALUE) {
            displayTime = "-1";
        } else {
            displayTime = String.valueOf(this.time);
        }
        return String.format(format, this.displayTime, this.trace);
    }

    private String getFormattedMem(String format) {
        return String.format(format, this.memoryUsage, this.trace);
    }

    // returns the string representation of the cpu profile
    public String toStringCpu() {
        return this.getFormattedStats(CPU_FORMAT_STRING);
    }
    // returns the string representation of the memory profile
    public String toStringMem() {
        return this.getFormattedMem(MEM_FORMAT_STRING);
    }
}