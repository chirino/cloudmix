package org.fusesource.cloudmix.agent.security;

import org.fusesource.cloudmix.agent.security.SecurityUtils;

import junit.framework.TestCase;

public class SecurityUtilsTest extends TestCase {

	public void testBasicAuthCredentials() {
		doBasicAuth("alice", "hunter2", "Basic YWxpY2U6aHVudGVyMg==");
		doBasicAuth("alice", "foo", "Basic YWxpY2U6Zm9v");
		doBasicAuth("bob", "secret", "Basic Ym9iOnNlY3JldA==");
	}

	private void doBasicAuth(String username, String password, String expected) {
		
		char[] passwordChars = password.toCharArray();
		String creds = SecurityUtils.toBasicAuth(username, passwordChars);
		assertEquals(expected, creds);
		for (int i = 0; i < passwordChars.length; i++) {
			assertEquals(' ', passwordChars[i]);
		}
		
	}
}