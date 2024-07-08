package softtest.repair.java.repairfile;

import java.io.File;

public interface IDefectRepair {
	public void copyFileByBuf(File srcFile,File desFile,String condition,int repairLine);

}
