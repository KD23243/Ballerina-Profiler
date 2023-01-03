package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Profiler {
    private static Profiler singletonInstance = null;
    private final HashMap<String, Profile> profiles = new HashMap<>();
    private final ArrayList<Profile> profilesStack = new ArrayList<>();
    private ArrayList<String> blockedMethods = new ArrayList<>();
    private List<String> methodNames = new ArrayList<>();

    int counter;

    protected Profiler() {
    }

    public static Profiler getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new Profiler();
        }
        return singletonInstance;
    }

    public void start(int id) {
        if (!blockedMethods.contains(getMethodName() + id)) {
            Profile p = new Profile(getMethodName(),getStackTrace());
            this.profiles.put(getMethodName() + id, p);
            this.profilesStack.add(p);
            p.start();
            methodNames.add(getMethodName());

            removeDuplicates(blockedMethods);
        }
        blockedMethods.remove(getMethodName() + id);
    }

    public void stop(String strandState, int id) {
        Profile p = this.profiles.get(getMethodName() + id);
        if (strandState.equals("RUNNABLE")){
            if (p == null) {
                throw new RuntimeException("The profile " + getMethodName() + " has not been created by a call to the start() method!");
            } else {
                p.stop();
                counter++;
            }
        }else {
            blockedMethods.add(getMethodName() + id);
        }
    }

//TODO call modulestop inside init shutdownhook 1st, but gotta pass that runtime var2 thing

    public String toString() {

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < this.profilesStack.size(); i++) {
            stringBuffer.append(this.profilesStack.get(i) + "\n");
        }

        String heading = "[ b7a profiler prototype ]\n";
        return heading + "\n" + stringBuffer + "\nCall Count          : " + counter + "\n" + "Function Count      : " + removeDuplicates(methodNames).size();
    }

    public void printProfilerOutput(String dataStream) {
        try {
            FileWriter myWriter = new FileWriter("Profile.txt");
            myWriter.write(dataStream);
            myWriter.close();
        } catch (IOException var3) {
            System.out.println("An error occurred.");
        }

    }

    public String getMethodName() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        return stack.get(2).getMethodName() + "()";
    }

    public String getStackTrace(){
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        stack.subList(0, 2).clear();
        Collections.reverse(stack);
        stack.subList(0, 5).clear();

        return stack.toString();
    }

    public List<String> removeDuplicates(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}