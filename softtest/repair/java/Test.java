package softtest.repair.java;

import java.io.File;
import java.io.IOException;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File(args[0]);		
		try {
			File tmp = File.createTempFile("tmp", ".java", file.getParentFile());
			FileRebuild.fileRebuild(file,tmp);
			//IOutil.copyFile(tmp,file);
			//tmp.delete();
		} catch (IOException e){
			e.printStackTrace();
		}
		
	}

}
