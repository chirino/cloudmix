/*
 * Copyright (c) 1999 Progress Software Corporation. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Progress
 * Software Corporation. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Progress.
 *
 * PROGRESS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PROGRESS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package org.fusesource.testrunner;

public final class Version {
    static final short MAJOR_VERSION = 5;
    static final byte MINOR_VERSION = 0;
    static final int BUILD_NUMBER = 100;
    static final String PRODUCT_NAME = "TestRunner";

    static String RELEASE_NAME = MAJOR_VERSION + "." + MINOR_VERSION;
    static String VERSION_STRING = PRODUCT_NAME + " v" + RELEASE_NAME + " build " + BUILD_NUMBER;

    public static void main(String[] args) {
        System.out.println("MAJOR_VERSION=" + MAJOR_VERSION);
        System.out.println("MINOR_VERSION=" + MINOR_VERSION);
        System.out.println("BUILD_NUMBER=" + BUILD_NUMBER);
    }

    public static void printVersion() {
        System.out.println(VERSION_STRING);
    }

    public static short getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static byte getMinorVersion() {
        return MINOR_VERSION;
    }

    public static int getBuildNumber() {
        return BUILD_NUMBER;
    }

    public static String getReleaseName() {
        return RELEASE_NAME;
    }

    public static String getVersionString() {
        return VERSION_STRING;
    }
}
