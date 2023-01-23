package parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CpuParser {
    public static void initializeCPUParser() throws Exception {

        String file = "CpuPre.json";    // File path of the Profiler Output json file
        String jsonInput = readFileAsString(file);  // Read the json file as a string

        // Removes the trailing comma
        StringBuffer jsonInputStringBuffer= new StringBuffer(jsonInput);
        jsonInputStringBuffer.deleteCharAt(jsonInputStringBuffer.length()-3);
        jsonInput = jsonInputStringBuffer.toString();

        ObjectMapper mapper = new ObjectMapper();   // Create an ObjectMapper object to map json to Java objects
        List<Item> input = mapper.readValue(jsonInput, new TypeReference<List<Item>>(){});  // Map the json input to a list of Item objects

        // Create a Data object to store the output
        Data output = new Data();
        output.name = "Root";
        output.value = input.get(0).time;
        output.children = new ArrayList<>();

        // Iterate through the input list
        for (Item item : input) {
            if (item.stackTrace.size() == 1) {
                output.value = Math.max(output.value, item.time);   // Update the value of the root node
            } else {
                Data current = output;
                // Iterate through the stack trace
                for (int i = 1; i < item.stackTrace.size(); i++) {
                    String name = item.stackTrace.get(i);
                    boolean found = false;
                    // Check if the child node already exists
                    for (Data child : current.children) {
                        if (child.name.equals(name)) {
                            // Update the value of the existing child node
                            child.value = Math.max(child.value, item.time);
                            current = child;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // Create a new child node if it doesn't exist
                        Data newChild = new Data();
                        newChild.name = name;
                        newChild.value = item.time;
                        newChild.children = new ArrayList<>();
                        current.children.add(newChild);
                        current = newChild;
                    }
                }
            }
        }

        String jsonString = mapper.writeValueAsString(output);  // Convert the output data object to a json string
        JSONObject jsonObject = new JSONObject(jsonString); // Convert the json string to a JSONObject

        int totalTime = getTotalTime(jsonObject);   // Calculate the total time
        int defaultTime = getDefaultTime(jsonObject);   // Calculate the default time
        int leastTime = getLeastTime(jsonObject);   // Calculate the least time it takes for a function

        // Check if the default time is less than or equal to 0
        if (defaultTime <= 0 || defaultTime < leastTime){
            jsonObject.remove("value"); // Remove the "value" key
            jsonObject.put("value", totalTime); // Add the total time as the value
        }
        writer(jsonObject.toString(),"CpuPost.json");  // write the json object to a file
    }

    public static int getTotalTime(JSONObject node) {
        int total = 0;  // Initialize total time
        JSONArray children = node.optJSONArray("children"); // Get the "children" array from the JSONObject
        if (children != null) {
            // Iterate through the children array
            for (int i = 0; i < children.length(); i++) {
                // Get the value of the child node and check if the value is not equal to -1
                if (children.getJSONObject(i).getInt("value") != -1){
                    total += children.getJSONObject(i).getInt("value");  // Add the value to the total time
                }
            }
        }
        return total;   // Return the total time
    }

    public static int getLeastTime(JSONObject node) {
        ArrayList<Integer> timeStamps = new ArrayList<>();
        JSONArray children = node.optJSONArray("children"); // Get the "children" array from the JSONObject
        if (children != null) {
            // Iterate through the children array
            for (int i = 0; i < children.length(); i++) {
                // Get the value of the child node and check if the value is not equal to -1
                if (children.getJSONObject(i).getInt("value") != -1){
                    timeStamps.add(children.getJSONObject(i).getInt("value")); // Add the value to the timestamps list
                }
            }
        }
        return Collections.min(timeStamps);   // Return the minimum timestamp
    }


    public static int getDefaultTime(JSONObject node) {
        return (int) node.get("value"); // Get the default Timestamp
    }

    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));  // Read Files as a String
    }

    static void writer(String parsedJson, String fileName) {
        // Add a variable declaration to the parsed json string
        parsedJson = "var data = " + parsedJson;
        try {
            FileWriter myWriter = new FileWriter(fileName);   // Create a FileWriter object to write to the specified file
            myWriter.write(parsedJson); // Write the parsed json string to the file
            myWriter.flush();   // Flush the writer
        } catch (IOException e) {
            System.out.println("An error occurred.");    // Print an error message
            e.printStackTrace();    // Print the stack trace of the exception
        }
    }

    private static class Item {
        public int time;
        public List<String> stackTrace;
    }

    private static class Data {
        public String name;
        public int value;
        public List<Data> children;
    }
}
