# Ballerina Profiler

Ballerina Profiler is a profiler tool designed for Ballerina functions. It calculates the execution time of Ballerina functions and provides a flame graph with all the functions. This tool is available as a jar file that can run on any OS. 

## Installation


    1. Download the latest version of Ballerina Profiler from the Releases section of this GitHub repository.
    2. Place the `Profiler.jar` file in the same directory where the Ballerina jar is located.


## Usage

To launch the Ballerina jar with the profiler, use the following command:

```bash
  java -jar Profiler.jar --file jar_name.jar
```

If the Ballerina jar requires arguments to be passed, use the following command:


```bash
  java -jar Profiler.jar --file {jar_name}.jar --args {arg1 arg2}
```

This will run the program and generate a flame graph showing the execution time of all the functions.

Note: Please make sure to replace `jar_name` and `arg1 arg2` with the actual names and arguments of the Ballerina jar that you want to profile.
