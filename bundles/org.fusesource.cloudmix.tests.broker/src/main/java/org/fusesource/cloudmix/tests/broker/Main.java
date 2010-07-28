/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.tests.broker;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision: 1.1 $
 */
public final class Main {
    private Main()  {
        //never constructed
    }
    
    private static final boolean verbose = true;

    public static void main(String[] args) {
        if (verbose || (args.length > 0 && args[0].equals("-debug"))) {
            Map<Object, Object> properties = new TreeMap<Object, Object>();
            properties.putAll(System.getProperties());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                System.out.println(" " + entry.getKey() + " = " + entry.getValue());
            }
        }

        ClassPathXmlApplicationContext applicationContext 
            = new ClassPathXmlApplicationContext("META-INF/spring/activemq.xml");
        applicationContext.start();

        System.out.println("Enter quit to stop");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = reader.readLine();
                if (line == null || line.trim().equalsIgnoreCase("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Caught: " + e);
            e.printStackTrace(System.err);
        }

        applicationContext.close();
    }
}
