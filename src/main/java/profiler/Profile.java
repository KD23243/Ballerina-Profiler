package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {
    private static final String FORMAT_STRING = "%-60.60s: %3d calls, total time: %5d ms, avg time: %5d ms, min time: %5d ms, max time: %5d ms";
    private String name;
    private long startTime;
    private long callCount;
    private long totalTime;
    private long minTime;
    private long maxTime;

    public Profile(String name) {
        this.name = name;
        this.callCount = 0L;
        this.totalTime = 0L;
        this.startTime = 0L;
        this.minTime = Long.MAX_VALUE;
        this.maxTime = Long.MIN_VALUE;
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
        ++this.callCount;
    }

    private String getFormattedStats(String format) {
        long avgTime = this.callCount == 0L ? 0L : this.totalTime / this.callCount;
        return String.format(format, this.name, this.callCount, this.totalTime, avgTime, this.minTime, this.maxTime);
    }

    public String toString() {
        return this.getFormattedStats(FORMAT_STRING);
    }
}
