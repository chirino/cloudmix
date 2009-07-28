/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuse.testrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * DataInputClass
 * <p>
 * Description:
 * </p>
 *
 * @author cmacnaug
 * @version 1.0
 */
public class DataInputTestApplication {

    public static void main(String[] args) {
        new DataInputTestApplication().run();
    }

    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                StringTokenizer tok = new StringTokenizer(command, ":");
                command = tok.nextToken();

                if (command.equalsIgnoreCase("exit")) {
                    System.exit(Integer.parseInt(tok.nextToken()));
                } else if (command.equalsIgnoreCase("echo")) {
                    System.out.println(tok.nextToken());
                } else if (command.equals("error")) {
                    System.err.println(tok.nextToken());
                } else {
                    System.err.println("Unknown command: " + command);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
