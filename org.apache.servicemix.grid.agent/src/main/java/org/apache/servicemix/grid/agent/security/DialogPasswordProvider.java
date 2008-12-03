package org.apache.servicemix.grid.agent.security;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Password provider that pops up a Swing dialog.
 *
 */
public class DialogPasswordProvider implements PasswordProvider {
	
	private static final String DEFAULT_TITLE = "Depot Authentication Required";
	private static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;
	
	JPasswordField password;
	boolean visible = true;

	private boolean status = true;
	private JFrame frame;
	private String title = DEFAULT_TITLE;
	private String username;
	private boolean finished;
	private long timeout = DEFAULT_TIMEOUT;
	

	public DialogPasswordProvider() {
	}

	
	public void setTitle(String t) {
		title = t;
	}
	
	public void setUsername(String u) {
		username = u;
	}
	
	public void setTimeout(long secs) {
		timeout = secs;
	}
	
	public char[] getPassword() {
				
		if (waitForCompletion()) {
			return password.getPassword();
		}
		return null;
	}

	private void init() {

		frame = new JFrame(title);
		JLabel prompt = new JLabel(getPrompt());
		password = new JPasswordField(20);
		password.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finished(true);
			}			
		});
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finished(true);
			}			
		});
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finished(false);
			}			
		});
		
		Container pane = frame.getContentPane();
		GridLayout layout = new GridLayout(3, 1);
		pane.setLayout(layout);		
		pane.add(prompt);
		
		JPanel passwordPanel = new JPanel();
		passwordPanel.add(password);
		pane.add(passwordPanel);

		
		JPanel panel = new JPanel();
		panel.add(ok);
		panel.add(cancel);
				
		pane.add(panel);
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension size = toolkit.getScreenSize();
		frame.setLocation( (int) (size.getWidth() - frame.getWidth()) / 2,
				           (int) (size.getHeight() - frame.getHeight()) / 2);
		frame.pack();
		frame.setVisible(visible);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finished(false);
			}
		});
		
	
	}


	private String getPrompt() {
		if (username != null) {
			return "Enter password for Depot user '" + username + "'";
		}
		return "Enter password for Depot";
	}


	private synchronized boolean waitForCompletion() {	
		try {
			finished = false;
			init();
			for (int i = 0; !finished && i < timeout; i++) {
				wait(1000);
			}
			if (!finished) {
				finished(false);
			}
		} catch (InterruptedException e) {
			return false;
		}
		return status;
	}	

	synchronized void finished(boolean s) {
		status = s;
		frame.dispose();
		finished = true;
		notify();
	}

}
