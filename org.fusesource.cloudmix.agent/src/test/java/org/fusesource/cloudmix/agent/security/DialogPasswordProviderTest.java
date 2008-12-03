package org.fusesource.cloudmix.agent.security;

import junit.framework.TestCase;
import org.fusesource.cloudmix.agent.security.DialogPasswordProvider;

public class DialogPasswordProviderTest extends TestCase {

	public void testDummy() {
		// Complete
	}
	
	public void xtestVisible() throws Exception {
	
		DialogPasswordProvider provider = new DialogPasswordProvider();
		provider.setUsername("Agent");
		provider.setTitle("Test");
		char[] password = provider.getPassword();
		if (password == null) {
			System.out.println("No password");
		} else {
			System.out.println("Password: " + String.valueOf(password));
		}
	}
	
	// Unfortunately need an Window system to run this test
	public void xtestDialogPasswordProvider() throws Exception {
		
		final DialogPasswordProvider provider = new DialogPasswordProvider();
		provider.setUsername("Agent");		
		provider.setTitle("Authentication required");
		provider.setTimeout(5);
				
		provider.visible = false;
		new Thread(new Runnable() {
			public void run() {
				delay(2);
				provider.password.setText("hunter2");
				provider.finished(true);				
			}			
		}).start();
		char[] password = provider.getPassword();
		assertNotNull(password);
		assertEquals("hunter2", String.valueOf(password));

		new Thread(new Runnable() {
			public void run() {
				delay(2);
				provider.finished(false);				
			}			
		}).start();
		password = provider.getPassword();
		assertNull(password);

	}
	
	private void delay(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
			// Complete
		}
	}
}
