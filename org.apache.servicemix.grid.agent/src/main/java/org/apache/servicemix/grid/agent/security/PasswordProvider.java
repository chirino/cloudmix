package org.apache.servicemix.grid.agent.security;

/**
 * Provides a way of obtaining a password from different sources.
 *
 */
public interface PasswordProvider {

	/**
	 * Get a password as a char array.  The array should be cleared after use.
	 * @return the password or null if the password could not be obtained.
	 */
	char[] getPassword();
}
