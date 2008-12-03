package org.apache.servicemix.grid.agent.security;

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
