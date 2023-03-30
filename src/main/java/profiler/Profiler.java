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

    private static final String GENERATED_METHOD_PREFIX = "$gen$";

    protected Profiler() {
        shutDownHookProfiler();
        try {
            String content = Files.readString(Paths.get("usedPaths.txt"));
            List<String> skippedListRead = new ArrayList<String>(Arrays.asList(content.split(", ")));
            skippedList.addAll(skippedListRead);
            skippedClasses.addAll(skippedList);
        } catch (IOException e) {
//            throw new RuntimeException(e);
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
        String name = getStackTrace();
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
        String name = getStackTrace();
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
        String name = getStackTrace();
        Profile p = (Profile) this.profiles.get(name);
        if (strandState.equals("RUNNABLE")) {
            if (p == null) {
//                throw new RuntimeException("The profile " + name + " has not been created by a call to the start() method!");
            } else {
                p.stop();
            }
        } else {
            blockedMethods.add(getMethodName() + id);
        }
    }

    public void stop() {
        String name = getStackTrace();
        Profile p = (Profile) this.profiles.get(name);
        if (p == null) {
//            throw new RuntimeException("The profile " + name + " has not been created by a call to the start() method!");
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
        //Uses the StackWalker class to get a list of stack frames representing the current call stack
        final List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.collect(Collectors.toList()));
        stack.subList(0, 2).clear(); //Removes the first 2 stack frames (index 0 and 1) and reverses the order of the remaining stack frames
        Collections.reverse(stack); //Reverse the collection
//        stack.subList(0, 2).clear(); //Removes the top 2 stack frames
        // Loop over stack frames and add filtered frames to a list of strings
        for (StackWalker.StackFrame frame : stack) {
            if (skippedClasses.contains(frame.getClassName())) {
                String frameString = frame.toString();
                frameString = "\"" + frameString.replaceAll("\\(.*\\)", "") + "()" + "\"";
                result.add(decodeIdentifier(frameString));
            }
        }
        // Convert result list to a string and return it
        return result.toString();
    }

    // This method takes an encoded identifier string as input and returns the decoded version of it
    public static String decodeIdentifier(String encodedIdentifier) {
        // If the input string is null, just return null
        if (encodedIdentifier == null) {
            return null;
        }
        // Create a StringBuilder to hold the decoded identifier
        StringBuilder sb = new StringBuilder();
        // Initialize the index to 0
        int index = 0;
        // Loop through the characters in the encoded identifier
        while (index < encodedIdentifier.length()) {
            // If the current character is a '$' and there are at least 4 characters left in the string
            if (encodedIdentifier.charAt(index) == '$' && index + 4 < encodedIdentifier.length()) {
                // Check if the next 4 characters are a Unicode code point
                if (isUnicodePoint(encodedIdentifier, index)) {
                    // If they are, append the character corresponding to that code point to the StringBuilder
                    sb.append((char) Integer.parseInt(encodedIdentifier.substring(index + 1, index + 5)));
                    // Update the index to skip over the 5 characters that were just decoded
                    index += 5;
                } else {
                    // If the next 4 characters are not a Unicode code point, just append the '$' character
                    sb.append(encodedIdentifier.charAt(index));
                    // Update the index to move to the next character
                    index++;
                }
            } else {
                // If the current character is not a '$' or there are less than 4 characters left in the string,
                // just append the current character
                sb.append(encodedIdentifier.charAt(index));
                // Update the index to move to the next character
                index++;
            }
        }
        // Once all the characters have been decoded and added to the StringBuilder, call the decodeGeneratedMethodName method
        // to remove any generated method prefixes from the identifier, and return the result
        return decodeGeneratedMethodName(sb.toString());
    }

    // This method checks if the substring of the encoded identifier starting from the given index represents a Unicode code point
    private static boolean isUnicodePoint(String encodedName, int index) {
        // Check if the substring contains only digits
        return (containsOnlyDigits(encodedName.substring(index + 1, index + 5)));
    }

    // This method takes a decoded method name as input and removes any generated method prefixes from it
    private static String decodeGeneratedMethodName(String decodedName) {
        return decodedName.startsWith(GENERATED_METHOD_PREFIX) ?
                decodedName.substring(GENERATED_METHOD_PREFIX.length()) : decodedName;
    }

    // This method checks if the given string contains only digits
    private static boolean containsOnlyDigits(String digitString) {
        // Loop through each character in the string and check if it is a digit
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                // If any character is not a digit, return false
                return false;
            }
        }
        // If all characters are digits, return true
        return true;
    }
}

