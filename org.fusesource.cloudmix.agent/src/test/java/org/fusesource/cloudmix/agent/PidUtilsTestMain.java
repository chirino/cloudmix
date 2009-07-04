/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

public class PidUtilsTestMain {

	public static void main(String[] args) throws InterruptedException {
//		System.err.println(System.getProperty("java.class.path"));
		System.out.println(PidUtils.getPid());
		while(true) {
			Thread.sleep(1000);
		}
	}

}
