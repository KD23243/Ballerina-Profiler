package server;

public class ServerFrontEnd {

    static String getSiteData(String contents) {

        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "   <meta charset=\"utf-8\">\n" +
                "   <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "   <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">\n" +
                "   <link rel=\"stylesheet\" type=\"text/css\"\n" +
                "      href=\"https://cdn.jsdelivr.net/gh/spiermar/d3-flame-graph@2.0.3/dist/d3-flamegraph.css\">\n" +
                "   <!-- <link rel=\"stylesheet\" type=\"text/css\" href=\"flamegraph.css\"> -->\n" +
                "\n" +
                "   <style>\n" +
                "      body {\n" +
                "         padding-top: 20px;\n" +
                "         padding-bottom: 20px;\n" +
                "      }\n" +
                "\n" +
                "      .header {\n" +
                "         padding-bottom: 20px;\n" +
                "         padding-right: 15px;\n" +
                "         padding-left: 15px;\n" +
                "         border-bottom: 1px solid #e5e5e5;\n" +
                "      }\n" +
                "\n" +
                "      .header h3 {\n" +
                "         margin-top: 0;\n" +
                "         margin-bottom: 0;\n" +
                "         line-height: 40px;\n" +
                "      }\n" +
                "\n" +
                "      .container {\n" +
                "         width: 1840px;\n" +
                "      }\n" +
                "\n" +
                "      .btn-primary {\n" +
                "         color: black;\n" +
                "         background-color: white;\n" +
                "         border-color: orange;\n" +
                "      }\n" +
                "\n" +
                "      .btn-primary:hover {\n" +
                "         color: black;\n" +
                "         background-color: orange;\n" +
                "      }\n" +
                "\n" +
                "      .btn-primary:focus {\n" +
                "         color: white;\n" +
                "         background-color: red;\n" +
                "      }\n" +
                "\n" +
                "      .btn {\n" +
                "         color: black;\n" +
                "         background-color: white;\n" +
                "         border-color: orange;\n" +
                "      }\n" +
                "   </style>\n" +
                "   <title>b7a-flamegraph</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "   <div class=\"container\">\n" +
                "      <div class=\"header clearfix\">\n" +
                "         <nav>\n" +
                "            <div class=\"pull-right\">\n" +
                "               <form class=\"form-inline\" id=\"form\">\n" +
                "                  <a class=\"btn\" href=\"javascript: resetZoom();\">Reset zoom</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: clear();\">Clear</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: stop();\">Stop</a>\n" +
                "                  <div class=\"form-group\">\n" +
                "                     <input type=\"text\" class=\"form-control\" id=\"term\">\n" +
                "                  </div>\n" +
                "                  <a class=\"btn btn-primary\" href=\"javascript: search();\">Search</a>\n" +
                "               </form>\n" +
                "            </div>\n" +
                "         </nav>\n" +
                "         <h3 class=\"text-muted\">b7a-flamegraph</h3>\n" +
                "      </div>\n" +
                "      <div id=\"chart\"></div>\n" +
                "      <hr>\n" +
                "      <div id=\"details\"></div>\n" +
                "   </div>\n" +
                "   <!-- <script src=\"/home/wso2/Documents/ProfilerDemo/profiler/Ballerina-Profiler/build/libs/performance_report1.json\"></script> -->\n" +
                "   <script src=\"https://d3js.org/d3.v4.min.js\" charset=\"utf-8\"></script>\n" +
                "   <script type=\"text/javascript\" src=\"https://cdn.jsdelivr.net/gh/spiermar/d3-flame-graph@2.0.3/dist/d3-flamegraph.min.js\"></script>\n" +
                "   <script type=\"text/javascript\">\n" +
                "\n" +
                "\n" +
                contents +
                "\n" +
                "\n" +
                "      var flameGraph = d3.flamegraph()\n" +
                "         .width(1840)\n" +
                "         .cellHeight(18)\n" +
                "         .transitionDuration(750)\n" +
                "         .minFrameSize(5)\n" +
                "         .transitionEase(d3.easeCubic)\n" +
                "         .sort(true)\n" +
                "         .title(\"\")\n" +
                "         .onClick(onClick)\n" +
                "         .differential(false);\n" +
                "\n" +
                "      // .selfValue(true);\n" +
                "\n" +
                "      // Set up the details element for the flame graph\n" +
                "      var details = document.getElementById(\"details\");\n" +
                "      flameGraph.setDetailsElement(details);\n" +
                "\n" +
                "      // Set the starting data for the flame graph\n" +
                "      var start = data;\n" +
                "\n" +
                "      // Render the flame graph\n" +
                "      d3.select(\"#chart\")\n" +
                "         .datum(start)\n" +
                "         .call(flameGraph);\n" +
                "\n" +
                "      // Add an event listener to the search bar form\n" +
                "      document.getElementById(\"form\").addEventListener(\"submit\", function (event) {\n" +
                "         event.preventDefault();\n" +
                "         search();\n" +
                "      });\n" +
                "\n" +
                "      // Define a search function to search the flame graph for a term\n" +
                "      function search() {\n" +
                "         var term = document.getElementById(\"term\").value;\n" +
                "         flameGraph.search(term);\n" +
                "      }\n" +
                "\n" +
                "      // Define a clear function to clear the search bar and reset the flame graph\n" +
                "      function clear() {\n" +
                "         document.getElementById('term').value = '';\n" +
                "         flameGraph.clear();\n" +
                "      }\n" +
                "\n" +

                "      // Define a clear function to clear the search bar and reset the flame graph\n" +
                "      function stop() {\n" +
                "         window.location.href = \"http://localhost:2324/off\";" +
                "      }\n" +
                "\n" +

                "      // Define a function to reset the zoom on the flame graph\n" +
                "      function resetZoom() {\n" +
                "         flameGraph.resetZoom();\n" +
                "      }\n" +
                "\n" +
                "      // Define a function to logs a message to the console\n" +
                "      function onClick(d) {\n" +
                "         console.info(\"Clicked on \" + d.data.name);\n" +
                "      }\n" +
                "\n" +
                "   </script>\n" +
                "</body>\n" +
                "\n" +
                "</html>";

        return html;
    }
}
