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
                "\n" +
                "<style>\n" +
                "      body {\n" +
                "         background-color: white;\n" +
                "      }\n" +
                "\n" +
                "      .container {\n" +
                "         padding-left: 0px;\n" +
                "         margin-left: 40px;\n" +
                "      }\n" +
                "\n" +
                "      .btn {\n" +
                "         border-radius: 0px;\n" +
                "         border: 1px solid #20b6b0;\n" +
                "         background-color: #20b6b0;\n" +
                "         color: #fff;\n" +
                "         font-family: pragmatica, sans-serif;\n" +
                "      }\n" +
                "\n" +
                "      .btn:hover {\n" +
                "         background-color: #494949;\n" +
                "         color: #fff;\n" +
                "      }\n" +
                "\n" +
                "      .btn:focus {\n" +
                "         border-radius: 0px;\n" +
                "         border: 1px solid #20b6b0;\n" +
                "         background-color: #20b6b0;\n" +
                "         color: #fff;\n" +
                "      }\n" +
                "\n" +
                "      .btn:active {\n" +
                "         color: #fff;\n" +
                "         background-color: #494949;\n" +
                "      }\n" +
                "\n" +
                "      .partition {\n" +
                "         margin-top: 40px;\n" +
                "         background-color: #F8F8F8;\n" +
                "      }\n" +
                "\n" +
                "      .header {\n" +
                "         margin-left: -40px;\n" +
                "         padding-right: 40px;\n" +
                "         width: 1920px;\n" +
                "         margin-bottom: 10px;\n" +
                "         padding-bottom: 20px;\n" +
                "         border-bottom: 0px solid #1d1d1d;\n" +
                "         background-color: #494949;\n" +
                "      }\n" +
                "\n" +
                "      .header h3 {\n" +
                "         margin-left: 40px;\n" +
                "         margin-top: 0px;\n" +
                "         margin-bottom: 0;\n" +
                "         line-height: 40px;\n" +
                "      }\n" +
                "\n" +
                "      .text-muted {\n" +
                "         padding-top: 20px;\n" +
                "      }\n" +
                "\n" +
                "      .pull-right {\n" +
                "         margin-top: 20px;\n" +
                "      }\n" +
                "\n" +
                "      .chart {\n" +
                "         margin-left: 50px;\n" +
                "      }\n" +
                "\n" +
                "      .highlight {\n" +
                "         fill: yellow !important;\n" +
                "      }\n" +
                "\n" +



                ".balLogo {\n" +
                "         padding-left: 20px !important;\n" +
                "         padding-top: 25px !important;\n" +
                "         width: 200px !important;\n" +
                "         height: 50px !important;\n" +
                "      }\n" +
                "\n" +



                ".form-control{\n" +
                "         border-radius: 0px !important;\n" +
                "         box-sizing:border-box !important;\n" +
                "         font-size:17px !important;\n" +
                "         height:2em !important;\n" +
                "         padding:.5em !important;\n" +
                "         transition:all 2s ease-in !important;\n" +
                "         width:300px !important;\n" +
                "         z-index:1 !important;\n" +
                "      }\n" +
                "\n" +
                "   </style>" +
                "   <title>b7a-flamegraph</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "   <div class=\"container\">\n" +
                "      <div class=\"header clearfix\">\n" +
                "         <nav>\n" +
                "            <div class=\"pull-right\">\n" +
                "               <form class=\"form-inline\" id=\"form\">\n" +
                "                  <a id=\"remove-me\" class=\"btn\" href=\"javascript: saveAsHtml();\">Save</a>\n" +
                "                  <a id=\"remove-me1\" class=\"btn\" href=\"javascript: stop();\">Stop</a>\n" +
                "                  <div class=\"form-group\">\n" +
                "                     <input placeholder=\"Search...\" id=\"searchBox\" type=\"text\" class=\"form-control\" id=\"term\">\n" +
                "                  </div>\n" +
                "                  <a class=\"btn btn-primary\" href=\"javascript: search();\">Search</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: resetZoom();\">Reset zoom</a>\n" +
                "                  <a class=\"btn\" href=\"javascript: clearSearch();\">Clear</a>\n" +
                "               </form>\n" +
                "            </div>\n" +
                "         </nav>\n" +
                "         <img class=\"balLogo\" src=\"https://ballerina.io/images/ballerina-logo-white.svg\" alt=\"B7A\"/>\n" +
                "      </div>\n" +
                "      <div id=\"chart\"></div>\n" +
                "      <hr>\n" +
                "      <div id=\"details\"></div>\n" +
                "   </div>\n" +
                "   <script type=\"text/javascript\" src=\"https://d3js.org/d3.v7.js\"></script>\n" +
                "   <script type=\"text/javascript\"\n" +
                "      src=\"https://cdn.jsdelivr.net/gh/spiermar/d3-flame-graph@2.0.3/dist/d3-flamegraph.min.js\"></script>" +
                "   <script type=\"text/javascript\">\n" +
                "\n" +
                "\n" +
                contents +
                "\n" +
                "\n" +
                "      var flameGraph = d3.flamegraph().width(1840).cellHeight(18).transitionDuration(750).minFrameSize(5).transitionEase(d3.easeCubic).sort(false).onClick(onClick).differential(false);\n" +
                "\n" +
                "      var details = document.getElementById(\"details\");\n" +
                "      flameGraph.setDetailsElement(details);\n" +
                "\n" +
                "      var start = data;" +
                "\n" +
                "      // Render the flame graph\n" +
                "      d3.select(\"#chart\")\n" +
                "         .datum(start)\n" +
                "         .call(flameGraph);\n" +
                "\n" +
                "function search() {\n" +
                "         var term = document.getElementById(\"searchBox\").value.toLowerCase();\n" +
                "         if (term) {\n" +
                "            var cells = d3.selectAll(\"rect\");\n" +
                "            cells.classed(\"highlight\", false);\n" +
                "            cells.each(function (d) {\n" +
                "               if (d.data.name.toLowerCase().indexOf(term) >= 0) {\n" +
                "                  d3.select(this).classed(\"highlight\", true);\n" +
                "               }\n" +
                "            });\n" +
                "         }\n" +
                "\n" +
                "         document.getElementById(\"searchBox\").addEventListener(\"input\", function () {\n" +
                "            if (this.value == \"\") {\n" +
                "               d3.selectAll(\"rect\").classed(\"highlight\", false);\n" +
                "            }\n" +
                "         });\n" +
                "      }\n" +
                "\n" +
                "      function clearSearch() {\n" +
                "         document.getElementById(\"searchBox\").value = \"\";\n" +
                "         d3.selectAll(\"rect\").classed(\"highlight\", false);\n" +
                "      }" +
                //func1
                "      // Define a clear function to clear the search bar and reset the flame graph\n" +
                "      function stop() {\n" +

                "const answer = window.confirm(\"Leaving this page will end the profiling process\");\n" +
                "\n" +
                "  // return the user's answer\n" +
                "  if (answer) {\n" +
                "         window.location.href = \"http://localhost:2324/terminate\";" +
                "           window.close();" +
                "  } " +
                "}\n" +
                "\n" +

                //func2
                "function saveAsHtml() {\n" +
                "  // Send an AJAX request to retrieve the current HTML content\n" +
                "  var xhr = new XMLHttpRequest();\n" +
                "  xhr.open(\"GET\", window.location.href, true);\n" +
                "  xhr.onreadystatechange = function() {\n" +
                "    if (xhr.readyState === 4 && xhr.status === 200) {\n" +
                "      // Get the response text (the HTML code)\n" +
                "      var htmlContent = xhr.responseText;\n" +
                "\n" +
                "      // Remove the button from the HTML content\n" +
                "      var parser = new DOMParser();\n" +
                "      var doc = parser.parseFromString(htmlContent, \"text/html\");\n" +
                "      var buttonToRemove = doc.getElementById(\"remove-me\");\n" +
                "      if (buttonToRemove) {\n" +
                "        buttonToRemove.parentNode.removeChild(buttonToRemove);\n" +
                "      }\n" +
                "      var buttonToRemove1 = doc.getElementById(\"remove-me1\");\n" +
                "      if (buttonToRemove1) {\n" +
                "        buttonToRemove1.parentNode.removeChild(buttonToRemove1);\n" +
                "      }\n" +
                "      htmlContent = doc.documentElement.outerHTML;\n" +
                "\n" +
                "      // Create a new Blob object with the modified HTML content\n" +
                "      var blob = new Blob([htmlContent], {type: \"text/html;charset=utf-8\"});\n" +
                "\n" +
                "      // Create a new anchor element to download the file\n" +
                "      var anchor = document.createElement(\"a\");\n" +
                "\n" +
                "      // Set the download attribute to the file name\n" +
                "      const currentTime = new Date().toLocaleString(); \n" +
                "      const fileName = `Profiler_Result_${currentTime}.html`;" +
                "      anchor.setAttribute(\"download\", fileName);\n" +
                "\n" +
                "      // Set the href attribute to the URL of the Blob object\n" +
                "      anchor.setAttribute(\"href\", URL.createObjectURL(blob));\n" +
                "\n" +
                "      // Click the anchor element to download the file\n" +
                "      anchor.click();\n" +
                "    }\n" +
                "  };\n" +
                "  xhr.send();\n" +
                "}\n" +


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

                "   <script>\n" +
                "      const myDiv = document.getElementById('details');\n" +
                "\n" +
                "      new MutationObserver(() => {\n" +
                "         const currentText = myDiv.textContent;\n" +
                "         if (currentText.includes('samples')) {\n" +
                "            const newText = currentText.replace('samples', 'ms');\n" +
                "            myDiv.textContent = newText;\n" +
                "         }\n" +
                "      }).observe(myDiv, { childList: true });\n" +
                "   </script>\n" +

                "</body>\n" +
                "\n" +
                "</html>";

        return html;
    }
}
