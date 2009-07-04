package org.fusesource.cloudmix.agent.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Interface to access some low level Windows kernel functions.
 * 
 * @author chirino
 */
public interface Kernel32 extends StdCallLibrary {

	public static final int FORMAT_MESSAGE_FROM_SYSTEM = 0x1000;
	public static final int FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x100;
	
	// Constants from http://msdn.microsoft.com/en-us/library/ms684880%28VS.85%29.aspx
	public static final int DELETE = 0x00010000;
	public static final int READ_CONTROL = 0x00020000;
	public static final int SYNCHRONIZE = 0x00100000;
	public static final int WRITE_DAC = 0x00040000;
	public static final int WRITE_OWNER = 0x00080000;
	
	public static final int PROCESS_TERMINATE = 0x0001;
	public static final int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;
	
	public class Factory {
		public static Kernel32 INSTANCE = create();

		public static Kernel32 create() {
			if( !Platform.isWindows() ) {
				return null;
			}
			return (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
		}

		public static Kernel32 get() {
			return INSTANCE;
		}

		public static String getLastErrorAsString() {
			int errorCode = INSTANCE.GetLastError();
			Memory buffer = new Memory(160);
			INSTANCE.FormatMessageW(
					FORMAT_MESSAGE_FROM_SYSTEM, Pointer.NULL, errorCode, 0,
					buffer, (int)buffer.getSize());
			return buffer.getString(0, true);
		}
	}

	/**
	 * http://msdn.microsoft.com/en-us/library/ms683180%28VS.85%29.aspx
	 * 
	 * @return
	 */
	int GetCurrentProcessId();

	/**
	 * http://msdn.microsoft.com/en-us/library/ms683179%28VS.85%29.aspx
	 * 
	 * @return
	 */
	Pointer GetCurrentProcess();

	/**
	 * See: http://msdn.microsoft.com/en-us/library/ms684320%28VS.85%29.aspx
	 * 
	 * @param desiredAccess
	 * @param inheritHandles
	 * @param processId
	 * @return
	 */
	Pointer OpenProcess(int desiredAccess, int inheritHandle, int processId);

	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms724211%28VS.85%29.aspx
	 * 
	 * @param handle
	 * @return
	 */
	int CloseHandle(Pointer handle);

	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms686714%28VS.85%29.aspx
	 * 
	 * @param process
	 * @param exitCode
	 * @return
	 */
	int TerminateProcess(Pointer process, int exitCode);

	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms683213%28VS.85%29.aspx
	 * 
	 * @param process
	 * @param processAffinityMask
	 * @param systemAffinityMask
	 * @return
	 */
	int GetProcessAffinityMask(Pointer process,
			IntByReference processAffinityMask,
			IntByReference systemAffinityMask);

	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms683182%28VS.85%29.aspx
	 * 
	 * @return
	 */
	Pointer GetCurrentThread();

	/**
	 * http://msdn.microsoft.com/en-us/library/ms686253%28VS.85%29.aspx
	 * 
	 * @param thread
	 * @param idealProcessor
	 * @return
	 */
	int SetThreadIdealProcessor(Pointer thread, int idealProcessor);

	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms679360%28VS.85%29.aspx
	 * 
	 * @return
	 */
	int GetLastError();

	/**
	 * 
	 * @param flags
	 * @param source
	 * @param messageId
	 * @param languageId
	 * @param buffer
	 * @param size
	 * @param arguments
	 * @return
	 */
	int FormatMessageW(int flags, Pointer source, int messageId,
			int languageId, Memory buffer, int size, Object... arguments);
	
	static final int WAIT_OBJECT_0 = 0x00000000;
	
	/**
	 * see: http://msdn.microsoft.com/en-us/library/ms687032%28VS.85%29.aspx
	 * @param handle
	 * @param milliseconds
	 * @return
	 */
	int WaitForSingleObject(Pointer handle, int milliseconds);

}
