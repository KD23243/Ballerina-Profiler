package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Profiler {
    private static Profiler singletonInstance = null;
    private final HashMap<String, Profile> profiles = new HashMap<>();
    private final ArrayList<Profile> profilesStack = new ArrayList<>();

    protected Profiler() {
    }

    public static Profiler getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new Profiler();
        }
        return singletonInstance;
    }

    public void start() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";

        Profile p = (Profile) this.profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            this.profiles.put(name, p);
            this.profilesStack.add(p);
        }

        p.start();

    }

    public void stop() {

        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";

        Profile p = (Profile) this.profiles.get(name);
        if (p == null) {
            throw new RuntimeException("The profile " + name + " has not been created by a call to the start() method!");
        } else {
            p.stop();
        }

    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < this.profilesStack.size(); i++) {
            sb.append(this.profilesStack.get(i) + "\n");
        }

        String heading = "[ b7a profiler prototype ]\n";
        return heading + "\n" + sb;

    }

    public void printProfilerOutput(String dataStream) {

        try {
            FileWriter myWriter = new FileWriter("Profile.txt");
            myWriter.write(dataStream);
            myWriter.close();
        } catch (IOException var3) {
            System.out.println("An error occurred.");
            var3.printStackTrace();
        }

    }
}



