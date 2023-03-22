# **Ballerina Profiler**

Ballerina Profiler is a Java application designed to provide execution time analysis and visualization for Ballerina functions. It is a simple tool that can help developers optimize their code and identify performance bottlenecks.


## **Installation**
To install the Ballerina Profiler, simply download the latest release from the GitHub repository and extract the Profiler.jar to the directory where the ballerina jar is located.


## **CLI Usage**

To launch the ballerina program with the profiler, use the following command:

```bash
  java -jar Profiler.jar --file {jar_name}.jar
```

If the ballerina program requires arguments to be passed, use the following command:


```bash
  java -jar Profiler.jar --file {jar_name}.jar --args {arg1 arg2}
```
Note: Please make sure to replace `jar_name` and `arg1 arg2` with the actual names and arguments of the ballerina jar.

This command will run the ballerina program with the profiler and it will start collecting data on function execution times and start generating a flamegraph. The user can use `CTRL+C` to terminate the original program. Once the original program is done, the terminal will show a localhost URL (`http://localhost:2324/`)
 
The user can then go to this URL in their browser to access the Ballerina Profiler GUI with the flamegraph.

## **GUI Usage**
The Flamegraph Visualizer provides several functionalities to help users navigate and analyze the flamegraph:

### Search function
> The search box and button allow users to search for a specific function by name. When a function is found, the corresponding rectangles will be highlighted in green. This is useful for quickly finding specific functions of interest.

### Reset zoom
> The reset zoom button allows users to reset the zoom level of the flamegraph to its default state. This is useful for quickly returning to the original view after zooming in or out.

### Clear search
> The clear button allows users to clear any highlights that have been applied to the flamegraph. This is useful for resetting the flamegraph to its default state.

### Save output
> The save button allows users to save the profiler output as an interactive HTML file. This file can be shared with others, allowing them to view and analyze the flamegraph in their own environment.

### Close program
> The close button stops the profiler, closes the GUI, and terminates the terminal. This is useful for quickly exiting the tool once the analysis is complete.
