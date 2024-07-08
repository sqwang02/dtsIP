package softtest.registery.file;

import javax.swing.filechooser.FileSystemView;

public class RegConstants
{
	// public final static String LICENSE_FILE_PATH_0 =
	// FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()
	// + "\\DTS";
	public final static String LICENSE_FILE_PATH_0 = "..\\..\\license";

	public final static String LICENSE_FILE_NAME_0 = "license_Java.dts";

	public final static String LICENSE_FILE_PATH_1 = "..\\set";

	public final static String LICENSE_FILE_NAME_1 = "q3tvbj.dat";

	public final static String LICENSE_REG_KEY = "Software\\DTS";

	public final static String LICENSE_REG_VALUE = "regInfo_Java";

	/*static
	{
		String path = null;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("windows") > -1)
		{
			path = RegUtils.getWinSystemRoot();
			path = path + "\\system32";
		}
		
		 * if(os.indexOf("linux") > -1) { path = RegUtils.getLinuxSystemRoot();
		 * }
		 
		LICENSE_FILE_PATH_1 = (path == null) ? "..\\..\\license" : path;
	}*/

}
