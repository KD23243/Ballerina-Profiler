package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {

//    private static final String FORMAT_STRING = "%-60s: TotalTime:- %6s ms, StackTrace:- %11s";

    private static final String FORMAT_STRING = "{'\"time\"': '\"%s\"', '\"stackTrace\"': '%s'},";

    private String name;
    private String trace;
    private long startTime;
    private long time;
    private String displayTime;

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

        if (this.time == Long.MAX_VALUE){
            displayTime = "INC";
        }else {
            displayTime = String.valueOf(this.time);
        }
//        return String.format(format, this.name, this.displayTime, this.trace);

        return String.format(format, this.displayTime, this.trace);
    }

    public String toString() {
        return this.getFormattedStats(FORMAT_STRING);
    }
}