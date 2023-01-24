package parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

import static parser.CpuParser.readFileAsString;
import static parser.CpuParser.writer;


public class MemoryParser {
    public static void initializeMEMParser() throws Exception {
        int counter = 1;

        String file = "MemPre.json";    // File path of the Profiler Output json file
        String jsonInput = readFileAsString(file);  // Read the json file as a string

        StringBuffer jsonInputStringBuffer= new StringBuffer(jsonInput);
        jsonInputStringBuffer.deleteCharAt(jsonInputStringBuffer.length()-3);
        jsonInput = jsonInputStringBuffer.toString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonInput);

        List<StackTraceMemory> stackTraceMemories = new ArrayList<>();
        for (JsonNode json : jsonNode) {
            int memory = json.get("mem").asInt();
            JsonNode stackTraceNode = json.get("stackTrace");
            String lastTrace = stackTraceNode.get(stackTraceNode.size() - 1).asText(); // get the last element of the stackTrace array
            stackTraceMemories.add(new StackTraceMemory(lastTrace, memory, counter));
            counter++;
        }

        writer(stackTraceMemories.toString(),"MemPost.json");  // write the json object to a file
    }

    private static class StackTraceMemory {
        private String stackTrace;
        private int memory;
        private int counter;

        public StackTraceMemory(String stackTrace, int memory, int counter) {
            this.stackTrace = stackTrace;
            this.memory = memory;
            this.counter = counter;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public int getMemory() {
            return memory;
        }

        @Override
        public String toString() {
            String output = "{" + "\"StackTrace\": " + "\"" + this.counter + ") " + stackTrace + "\"" + ", \"Memory\": " + memory + "}";
            return output;
        }
    }
}

