package profiler;

import java.util.concurrent.TimeUnit;

public class Profile {
    private String name;
    private long startTime;
    private long totalTime;
    private long minTime;
    private long maxTime;

    public Profile(String name) {
        this.name = name;
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
    }

    private String getFormattedStats() {

        int time = (int) this.totalTime;
        String[] stackTrace = new String[] {this.name};

        // create the string representation of the output
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"time\": \"").append(time).append("\", ");
        sb.append("\"stackTrace\": ");
        for (int i = 0; i < stackTrace.length; i++) {
            sb.append(stackTrace[i]);
            if (i < stackTrace.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("},");
        return sb.toString();
    }

    public String toString() {
        return this.getFormattedStats();
    }
}

//the problem is we are not profiling init correctly. it just takes the value of the child.

//we go up to ballerina.http.2.http_client.createClient() but after that, the other 4 we don't profile