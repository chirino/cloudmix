package org.fusesource.cloudmix.agent.unix;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;

/**
 * Interface to access some POSIX API functions.
 * 
 * @author chirino
 */
public interface Posix extends Library {

	static final int SIGTERM = 15;
	static final int SIGKILL = 9;
	
	static final int WNOHANG = 1;
	static final int WUNTRACED = 2;


	public class Factory {
		public static Posix INSTANCE = create();
		public static Posix create() {
			// Yeah windows does support a POSIX API.. but it's a not very fully featured.
			return (Posix) Native.loadLibrary(Platform.isWindows() ? "msvcrt" : "c", Posix.class);
		}
		public static Posix get() {
			return INSTANCE;
		}
	}

	/**
	 * see: http://man-wiki.net/index.php/2:exit
	 * @param status
	 */
	void exit(int status);

	/**
	 * see: http://man-wiki.net/index.php/3:execvp
	 * @param filename
	 * @param argv
	 * @return
	 */
	int execvp(String filename, String[] argv);
	
	/**
	 * see: http://man-wiki.net/index.php/2:fork
	 * @return
	 */
	int fork();

	/**
	 * see: http://man-wiki.net/index.php/2:pipe
	 * @param filedes
	 * @return
	 */
	int pipe(int fds[]);
	
	/**
	 * see: http://man-wiki.net/index.php/2:close
	 * @param fd
	 * @return
	 */
	int close(int fd);

	/**
	 * see: http://man-wiki.net/index.php/2:umask
	 * @param mask
	 */
	void umask(int mask);
	
	/**
	 * see: http://man-wiki.net/index.php/2:setsid
	 * @return
	 */
	int setsid();

	/**
	 * see: http://man-wiki.net/index.php/2:kill
	 * @param pid
	 * @param signum
	 * @return
	 */
	int kill(int pid, int signum);

	/**
	 * see: http://man-wiki.net/index.php/2:waitpid
	 * @param pid
	 * @param stat_loc
	 * @param options
	 * @return
	 */
	int waitpid(int pid, IntByReference stat_loc, int options);

	/**
	 * see: http://man-wiki.net/index.php/2:chdir
	 * @param path
	 * @return
	 */
	int chdir(String path);
	
	/**
	 * see: http://man-wiki.net/index.php/2:nice
	 * @param increment
	 * @return
	 */
	int nice(int increment);

	/**
	 * see: http://man-wiki.net/index.php/2:getpid
	 * @return
	 */
	int getpid();

	/**
	 * see: http://man-wiki.net/index.php/2:symlink
	 * @param oldname
	 * @param newname
	 * @return
	 */
	int symlink(String oldname, String newname);

	/**
	 * see: http://man-wiki.net/index.php/2:chmod
	 * @param filename
	 * @param mode
	 * @return
	 */
	int chmod(String filename, int mode);

	/**
	 * see: http://man-wiki.net/index.php/3:strerror
	 * @param errnum
	 * @return
	 */
	String strerror(int errnum);

}
