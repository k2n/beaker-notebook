/*
 *  Copyright 2014 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twosigma.beaker.sqlsh.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QueryParser {

  public static List<String> split(String script) {
    script = deleteInterval("/*", "*/", script);

    Scanner scanner = new Scanner(script);
    StringBuffer sb = new StringBuffer();

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      line.trim();
      //ignore comments #
      int commentIndex = line.indexOf('#');
      if (commentIndex != -1) {
        if (line.startsWith("#")) {
          line = new String("");
        } else {
          line = line.substring(0, commentIndex - 1);
        }
      }
      //ignore comments --
      commentIndex = line.indexOf("--");
      if (commentIndex != -1) {
        if (line.startsWith("--")) {
          line = new String("");
        } else {
          line = line.substring(0, commentIndex - 1);
        }
      }
      //ignore comments %%
      commentIndex = line.indexOf("%%");
      if (commentIndex != -1) {
        if (line.startsWith("%%")) {
          line = new String("");
        } else {
          line = line.substring(0, commentIndex - 1);
        }
      }

      if (line != null) {
        sb.append(line).append(" ");
      }
    }
    scanner.close();

    //ToDo replace to custom splitter - need for ignore 'xx;yy' etc.
    String[] splittedQueries = sb.toString().split(";");
    List<String> listOfQueries = new ArrayList<>();

    for (int i = 0; i < splittedQueries.length; i++) {
      if (!splittedQueries[i].trim().equals("") && !splittedQueries[i].trim().equals("\t")) {
        listOfQueries.add(splittedQueries[i].trim());
      }
    }
    return listOfQueries;
  }

  private static String deleteInterval(String start, String end, String source) {
    int startIndex = source.indexOf(start);
    while (startIndex >= 0) {
      int endIndex = source.indexOf(end);
      if (endIndex != -1) {
        if (endIndex < startIndex) {
          break;
        }
        source = source.substring(0, startIndex) + source.substring(endIndex + 2);
      } else {
        break;
      }

      startIndex = source.indexOf(start);
    }

    return source;
  }
}
