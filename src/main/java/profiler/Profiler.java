package profiler;

import java.io.BufferedWriter;
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

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < this.profilesStack.size(); i++) {
            stringBuffer.append(this.profilesStack.get(i) + "\n");
        }
        return stringBuffer.toString();
    }

    public void printProfilerOutput(String dataStream, String name){

        dataStream = "[" + dataStream.replaceAll("'", "");
        if(dataStream.indexOf(",") != -1){
            dataStream = dataStream.substring(0,dataStream.length() - 2);
        }
        dataStream = dataStream + "]";

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(name + ".json"));
            out.write(dataStream);
            out.flush();
        } catch (IOException ignore) {} catch (Exception e) {
            throw new RuntimeException(e);
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

        ArrayList<String> stackTraceArray = new ArrayList<String>();

        for (StackWalker.StackFrame element : stack) {
            stackTraceArray.add("\"" + element.toString().replaceAll("\\(.*\\)", "") + "()" + "\"");
        }

        return stackTraceArray.toString();

    }

    public List<String> removeDuplicates(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }


}