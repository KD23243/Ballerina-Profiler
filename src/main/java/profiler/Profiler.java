package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Profiler {
    private static Profiler singletonInstance = null;
    private final HashMap<String, Profile> profiles = new HashMap<>();
    private final ArrayList<Profile> profilesStack = new ArrayList<>();
    private ArrayList<String> blockedMethods = new ArrayList<>();
    private List<String> methodNames = new ArrayList<>();

    int counter = 0;
    int counter1 = 0;

    protected Profiler() {
    }

    public static Profiler getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new Profiler();
        }
        return singletonInstance;
    }

    public void start() {
        if (!blockedMethods.contains(getMethodName())) {
            methodNames.add(getMethodName());
            Profile p = this.profiles.get(getMethodName() + counter);
            if (p == null) {
                p = new Profile(getMethodName(),getStackTrace());
                this.profiles.put(getMethodName(), p);
                this.profilesStack.add(p);
            }
            p.start();
            counter++;
        }
    }


    public void stop(String strandState) {
        if (strandState.equals("RUNNABLE")){
            Profile p = this.profiles.get(getMethodName());
            if (p == null) {
                throw new RuntimeException("The profile " + getMethodName() + " has not been created by a call to the start() method!");
            } else {
                p.stop();
            }
            counter1++;
        }else {
            blockedMethods.add(getMethodName());
        }

    }

    public String toString() {


        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < this.profilesStack.size(); i++) {
            sb.append(this.profilesStack.get(i) + "\n");
        }

        String heading = "[ b7a profiler prototype ]\n";
        return heading + "\n" + sb + "\nCall Count          : " + counter + "\n" + "Function Count      : " + removeDuplicate(methodNames).size();
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

    public static List<String> removeDuplicate(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}



