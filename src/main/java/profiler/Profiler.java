package profiler;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Profiler {
    private static Profiler singletonInstance = null;
    private final HashMap < String, Profile > profiles = new HashMap < > ();
    private final ArrayList < Profile > profilesStack = new ArrayList < > ();
    private ArrayList < String > blockedMethods = new ArrayList < > ();
    private List < String > methodNames = new ArrayList < > ();

    private static List < String > skippedList = new ArrayList < > ();

    int counter;

    protected Profiler() {
        shutDownHookProfiler();

        try {
            // Create a BufferedReader object to read from the "skippedPaths.txt" file.
            BufferedReader bf = new BufferedReader(new FileReader("skippedPaths.txt"));
            // Read the first line of the file.
            String line = bf.readLine();
            // While there are still lines to read, add the current line to myList and read the next line.
            while (line != null) {
                skippedList.add(line);
                line = bf.readLine();
            }
            // Close the BufferedReader object.
            bf.close();

            // Add the string "$_init" to myList.
            skippedList.add("$_init");
        } catch (Exception ignored) {
            // If there is an exception while reading the file, ignore it and continue.
        }
    }

    public static Profiler getInstance() {

        if (singletonInstance == null) {
            singletonInstance = new Profiler();
        }
        return singletonInstance;
    }

    /* This method starts a new profile for the current method being executed and adds it to the profiles map and stack
    It also adds the current method name to the methodNames set and removes duplicates from the blockedMethods list */
    public void start(int id) {

        // check if the current method + id combination is not already blocked
        if (!blockedMethods.contains(getMethodName() + id)) {
            Profile p = new Profile(getMethodName(), getStackTrace()); // create a new profile for the current method and add it to the profiles map and stack
            this.profiles.put(getMethodName() + id, p);
            this.profilesStack.add(p);
            p.start();
            methodNames.add(getMethodName()); // add the current method name to the methodNames set
            removeDuplicates(blockedMethods); //remove duplicates from blockedMethods list
        }
        blockedMethods.remove(getMethodName() + id); // remove the current method + id combination from the blockedMethods list
    }

    /* This method stops the profile for the current method being executed and increments the counter if the strand state is "RUNNABLE"
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
        final List < StackWalker.StackFrame > stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        return stack.get(2).getMethodName() + "()";
    }

    // This method returns a string representation of the current call stack in the form of a list of strings
    public String getStackTrace() {
        ArrayList < String > stackTraceArray = new ArrayList < > ();

        //Uses the StackWalker class to get a list of stack frames representing the current call stack
        final List < StackWalker.StackFrame > stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));

        stack.subList(0, 2).clear(); //Removes the first 2 stack frames (index 0 and 1) and reverses the order of the remaining stack frames

        Collections.reverse(stack); //Reverse the collection

        stack.subList(0, 2).clear(); //Removes the top 2 stack frames

        //Iterates over the remaining stack frames, removing the details between the parentheses and adding "()" to the end

        // The following code iterates through a StackWalker stack, and for each StackFrame, checks if it contains a keyword from a skipped list.
        // If the StackFrame does not contain any keyword from the list, it adds the string representation of the StackFrame to an array called stackTraceArray.

        for (StackWalker.StackFrame element: stack) {
            // Set a boolean variable to false, which will be used to check if the StackFrame contains any keyword from the skipped list.
            boolean containsKeyword = false;
            // Iterate through the skippedList, and for each keyword, check if the current StackFrame contains it.
            for (String keyword: skippedList) {
                if (element.toString().contains(keyword)) {
                    // If the StackFrame contains the keyword, set containsKeyword to true and break out of the loop.
                    containsKeyword = true;
                    break;
                }
            }

            // If the current StackFrame does not contain any keyword from the skipped list, add it to the stackTraceArray.
            if (!containsKeyword) {
                // Use the toString() method to get the string representation of the StackFrame.
                // Remove the method arguments from the string representation, and add it to the stackTraceArray.
                stackTraceArray.add("\"" + element.toString().replaceAll("\\(.*\\)", "") + "()" + "\"");
            }
        }

        return stackTraceArray.toString();

    }

    //Remove the duplicates from the arraylist
    public List < String > removeDuplicates(List < String > list) {
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