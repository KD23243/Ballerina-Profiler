# Ballerina Profiler

Ballerina Profiler is a Java application designed to provide execution time analysis and visualization for Ballerina functions. It is a simple tool that can help developers optimize their code and identify performance bottlenecks.

## Installation
    1. Download the latest version of Ballerina Profiler from the Releases section of this GitHub repository.
    2. Place the `Profiler.jar` file in the same directory where the Ballerina jar is located.


## Usage

To launch the ballerina program with the profiler, use the following command:

```bash
  java -jar Profiler.jar --file {jar_name}.jar
```

If the ballerina program requires arguments to be passed, use the following command:


```bash
  java -jar Profiler.jar --file {jar_name}.jar --args {arg1 arg2}
```

This will run the ballerina program with the profiler.

Note: Please make sure to replace `jar_name` and `arg1 arg2` with the actual names and arguments of the ballerina jar.
