package profiler;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Profiler {
    private static Profiler singletonInstance = null;
    private final HashMap<String, Profile> profiles = new HashMap<>();
    private final ArrayList<Profile> profilesStack = new ArrayList<>();
    private ArrayList<String> blockedMethods = new ArrayList<>();
    private List<String> methodNames = new ArrayList<>();
    private static List<String> skippedList = new ArrayList<>();
    private static Set<String> skippedClasses = new HashSet<>(skippedList);

    protected Profiler() {
        shutDownHookProfiler();
        try {
            String content = Files.readString(Paths.get("skippedPaths.txt"));
            List<String> skippedListRead = new ArrayList<String>(Arrays.asList(content.split(", ")));
            skippedList.addAll(skippedListRead);
            skippedClasses.addAll(skippedList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Profiler getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new Profiler();
        }
        return singletonInstance;
    }

    public String getMethodName() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        return stack.get(2).getMethodName() + "()";
    }

    public List<String> removeDuplicates(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public void start(int id) {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";
        name = getStackTrace();
        if (!blockedMethods.contains(getMethodName() + id)) {
            Profile p = (Profile) this.profiles.get(name);
            if (p == null) {
                p = new Profile(name);
                this.profiles.put(name, p);
                this.profilesStack.add(p);
            }
            p.start();
            methodNames.add(getMethodName()); // add the current method name to the methodNames set
            removeDuplicates(blockedMethods); //
        }
        blockedMethods.remove(getMethodName() + id);
    }


    public void start() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";
        name = getStackTrace();
        Profile p = (Profile) this.profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            this.profiles.put(name, p);
            this.profilesStack.add(p);
        }
        p.start();
        methodNames.add(getMethodName()); // add the current method name to the methodNames set
        removeDuplicates(blockedMethods); //
    }


    public void stop(String strandState, int id) {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";
        name = getStackTrace();
        Profile p = (Profile) this.profiles.get(name);
        if (strandState.equals("RUNNABLE")) {
            if (p == null) {
                throw new RuntimeException("The profile " + name + " has not been created by a call to the start() method!");
            } else {
                p.stop();
            }
        } else {
            blockedMethods.add(getMethodName() + id);
        }
    }

    public void stop() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        String name = stack.get(1).getClassName() + " :" + stack.get(1).getMethodName() + "()";
        name = getStackTrace();
        Profile p = (Profile) this.profiles.get(name);
        if (p == null) {
            throw new RuntimeException("The profile " + name + " has not been created by a call to the start() method!");
        } else {
            p.stop();
        }
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < this.profilesStack.size(); i++) {
            sb.append(this.profilesStack.get(i) + "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public void printProfilerOutput(String dataStream) {
        try {
            FileWriter myWriter = new FileWriter("CpuPre.json");
            myWriter.write(dataStream);
            myWriter.close();
        } catch (IOException var3) {
            System.out.println("An error occurred.");
            var3.printStackTrace();
        }
    }

    public static void shutDownHookProfiler() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Profiler profiler = Profiler.getInstance();
            profiler.printProfilerOutput(profiler.toString());
        }));
    }

    // This method returns a string representation of the current call stack in the form of a list of strings
    public String getStackTrace() {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> filteredFrames = new ArrayList<>();
        //Uses the StackWalker class to get a list of stack frames representing the current call stack
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        stack.subList(0, 2).clear(); //Removes the first 2 stack frames (index 0 and 1) and reverses the order of the remaining stack frames
        Collections.reverse(stack); //Reverse the collection
        stack.subList(0, 2).clear(); //Removes the top 2 stack frames
        // Loop over stack frames and add filtered frames to a list of strings
        for (StackWalker.StackFrame frame : stack) {
            if (skippedClasses.contains(frame.getClassName())) {
                String frameString = frame.toString();
                frameString = "\"" + frameString.replaceAll("\\(.*\\)", "") + "()" + "\"";
                filteredFrames.add(frameString);
            }
        }
        // Loop over filtered frame strings and add non-generated ones to a result list
        for (String frameString : filteredFrames) {
            if (!frameString.contains("$gen")) {
                result.add(frameString);
            }
        }
        // Convert result list to a string and return it
        return result.toString();
    }
}


