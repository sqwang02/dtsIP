package softtest.summary.lib.java;


import java.io.File;

/**
 * 
 * �⺯������ժҪ�������������ָ��Ŀ¼�м��ؿ⺯������ժҪ�����������ɺ�����������������ժҪ
 * @author cjie
 *
 */
public class LibManager {

	/**
	 * <p>��ǰ�������ļ��м��ص����к����⣬�����ж������������Ϣ�ĺ����б�</p>
	 */
	private static LibManager instance;
	
	private LibManager() {
	}
	
	public static LibManager getInstance() {
		if (instance == null) {
			instance = new LibManager();
		}
		return instance;
	}
	
	/**
	 * ����ָ��Ŀ¼�����е������ļ������ĺ���ժҪ��Ϣ
	 * 
	 * @param path ��������ժҪ��Ϣ���ļ�Ŀ¼
	 */
	public void loadLib(String path) {
		File dir = new File(path);
		if(!dir.isDirectory()) {
			return;
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".xml")) {
				LibLoader.loadLibSummarys(files[i].getAbsolutePath());	
			}
		}
	}

}
