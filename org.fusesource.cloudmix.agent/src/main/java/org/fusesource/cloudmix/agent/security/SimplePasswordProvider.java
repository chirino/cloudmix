/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.security;

public class SimplePasswordProvider implements PasswordProvider {

	private String password;

	public SimplePasswordProvider() {
	}
	
	public void setRawPassword(String pw) {
		password = pw;
	}
	
	public char[] getPassword() {
		if (password == null) {
			return null;
		}
		return password.toCharArray();
	}

}
