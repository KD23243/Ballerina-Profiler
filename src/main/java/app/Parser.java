package app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static void initializeParser() throws Exception {
        // Parse the input JSON data

        String file = "Output.json";
        String jsonInput = readFileAsString(file);

        ObjectMapper mapper = new ObjectMapper();
        List<Item> input = mapper.readValue(jsonInput, new TypeReference<List<Item>>(){});

        Data output = new Data();
        output.name = input.get(0).stackTrace.get(0);
        output.value = input.get(0).time;
        output.children = new ArrayList<>();

        for (Item item : input) {
            if (item.stackTrace.size() == 1) {
                output.value = Math.max(output.value, item.time);
            } else {
                Data current = output;
                for (int i = 1; i < item.stackTrace.size(); i++) {
                    String name = item.stackTrace.get(i);
                    boolean found = false;
                    for (Data child : current.children) {
                        if (child.name.equals(name)) {
                            child.value = Math.max(child.value, item.time);
                            current = child;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
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

        String json = mapper.writeValueAsString(output);
        JSONObject jsonObject = new JSONObject(json);

        int total = getGeneratedRoot(jsonObject);
        int defaultTime = getDefaultRoot(jsonObject);

        if (defaultTime == -1){
            jsonObject.remove("value");
            jsonObject.put("value", total);
        }

        writer(jsonObject.toString());
    }

    public static int getGeneratedRoot(JSONObject node) {
        int total = 0;
        JSONArray children = node.optJSONArray("children");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                if (children.getJSONObject(i).getInt("value") != -1){
                    total += children.getJSONObject(i).getInt("value");
                }
            }
        }
        return total;
    }


    public static int getDefaultRoot(JSONObject node) {
        return (int) node.get("value");
    }

    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    private static void writer(String parsedJson) {
        parsedJson = "var data = " + parsedJson;
        try {
            FileWriter myWriter = new FileWriter("Output1.json");
            myWriter.write(parsedJson);
            myWriter.flush();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
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
