package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {

    private static final String FORMAT_STRING = "{'\"time\"': '\"%s\"', '\"stackTrace\"': '%s'},";  // constant string format for JSON output
    private String name;    // name of the profile
    private String trace;   // trace of the profile
    private long startTime; // start time of the profile
    private long time;  // time duration of the profile
    private String displayTime; // display time of the profile

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
    }

    // stops the profile
    public void stop() {
        this.time = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - this.startTime;
    }

    // get formatted stats of the profile
    private String getFormattedStats(String format) {

        if (this.time == Long.MAX_VALUE){
            displayTime = "-1";
        }else {
            displayTime = String.valueOf(this.time);
        }
        return String.format(format, this.displayTime, this.trace);
    }

    // returns the string representation of the profile
    public String toString() {
        return this.getFormattedStats(FORMAT_STRING);
    }
}