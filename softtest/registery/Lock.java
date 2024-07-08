package softtest.registery;
public class Lock {
	static {
		System.loadLibrary("Lock");
	}

	static public native int Create(int appID);
	static public native void Destroy(int handle);

	static public native boolean CheckPermission(int handle, String pin);
	static public native boolean CheckFile(int handle, String pin, String filename);
}
