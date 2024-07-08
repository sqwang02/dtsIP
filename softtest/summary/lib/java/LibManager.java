package softtest.summary.lib.java;


import java.io.File;

/**
 * 
 * 库函数函数摘要管理器，负责从指定目录中加载库函数函数摘要，并编译生成函数声明，构建函数摘要
 * @author cjie
 *
 */
public class LibManager {

	/**
	 * <p>当前从配置文件中加载的所有函数库，及库中定义的有特征信息的函数列表</p>
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
	 * 加载指定目录下所有的配置文件描述的函数摘要信息
	 * 
	 * @param path 包含函数摘要信息的文件目录
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
