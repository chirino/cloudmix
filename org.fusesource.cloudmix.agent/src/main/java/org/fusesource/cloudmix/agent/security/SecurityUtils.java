package org.fusesource.cloudmix.agent.security;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

public final class SecurityUtils {
	
	private SecurityUtils() {
		// Complete.
	}
	
	/**
	 * Create HTTP Basic Authentication header given username and password.
	 * 
	 * @param username 
	 * @param password
	 * @return String containing HTTP Basic Auth header suitable to be send via the
	 * HTTP Authorization header.
	 */
	public static String toBasicAuth(String username, char[] password) {
		StringBuilder sb = new StringBuilder().append(username).append(":").append(String.valueOf(password));
		// Clear array.
		Arrays.fill(password, ' ');
		return "Basic " + new String(Base64.encodeBase64(sb.toString().getBytes()));
	}
	
}
