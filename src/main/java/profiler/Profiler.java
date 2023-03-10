package profiler;

import java.io.BufferedWriter;
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

    int counter;

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

    /* This method starts a new Profile for the current method being executed and adds it to the Profiles map and stack
    It also adds the current method name to the methodNames set and removes duplicates from the blockedMethods list */
    public void start(int id) {

        // check if the current method + id combination is not already blocked
        if (!blockedMethods.contains(getMethodName() + id)) {
            Profile p = new Profile(getMethodName() + id, getStackTrace()); // create a new Profile for the current method and add it to the Profiles map and stack
            this.profiles.put(getMethodName() + id, p);
            this.profilesStack.add(p);
            p.start();
            methodNames.add(getMethodName()); // add the current method name to the methodNames set
            removeDuplicates(blockedMethods); //remove duplicates from blockedMethods list
        }
        blockedMethods.remove(getMethodName() + id); // remove the current method + id combination from the blockedMethods list
    }

    /* This method stops the Profile for the current method being executed and increments the counter if the strand state is "RUNNABLE"
    If the strand state is not "RUNNABLE", it adds the current method + id combination to the blockedMethods list */
    public void stop(String strandState, int id) {
        Profile p = this.profiles.get(getMethodName() + id); // retrieve the profile for the current method + id combination
        if (strandState.equals("RUNNABLE")) {
            if (p == null) {
                // if profile is not found, throw an exception
                throw new RuntimeException("The profile " + getMethodName() + " has not been created by a call to the start() method!");
            } else {
                p.stop(); // stop the profile and increment the counter
                counter++;
            }
        } else {
            blockedMethods.add(getMethodName() + id); // add the current method + id combination to the blockedMethods list
        }
    }

    public String toStringCpu() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.profilesStack.size(); i++) {
            stringBuffer.append(this.profilesStack.get(i).toStringCpu() + "\n");
        }

        return stringBuffer.toString();
    }

    public void printProfilerOutput(String dataStream, String fileName) {
        dataStream = dataStream.replace("'", "");
        dataStream = "[" + dataStream + "]"; // Add square brackets at the start and end of the string, and removes all single quotes within the string

        // Create a BufferedWriter object and write the modified dataStream to a file with the name "Output.json"
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName + ".json"))) {
            out.write(dataStream);
        } catch (IOException e) {
            throw new RuntimeException(e); // If there are any IOExceptions or other exceptions, it will be caught and handled
        }
    }

    /* This method returns the method name of the calling method in the form of a String
    It uses the StackWalker class to get a list of stack frames representing the current call stack
    It then returns the method name of the 3rd stack frame (2nd index) in the form of a String "methodName()" */
    public String getMethodName() {
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        return stack.get(2).getMethodName() + "()";
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

    //Remove the duplicates from the arraylist
    public List<String> removeDuplicates(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    //This method starts the profile for the current method
    public void start() {
        Profile p = new Profile(getMethodName(), getStackTrace());
        this.profiles.put(getMethodName(), p);
        this.profilesStack.add(p);
        p.start();
    }

    //This method stops the profile for the current method
    public void stop() {
        Profile p = this.profiles.get(getMethodName());
        if (p == null) {
            throw new RuntimeException("The profile " + getMethodName() + " has not been created by a call to the start() method!");
        } else {
            p.stop();
        }
    }

    public static void shutDownHookProfiler() {
        // add a shutdown hook to stop the profiler and parse the output when the program is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Profiler profiler = Profiler.getInstance();
            profiler.printProfilerOutput(profiler.toStringCpu(), "CpuPre");
        }));
    }
}


//remove to string with getmethodname
//fix the time.Call billion times and should show that time.