package softtest.repair.java;

import java.io.File;
import java.io.IOException;

import softtest.repair.java.IOutil;
import softtest.repair.java.DefectRepairTest;
import softtest.database.java.DBAccess;
public class DefectRepair {
	public static void RepairStr(String Defect,String category,int id,
			String fileName, String variable, int startLine, int iPLine,
			String iPLineCode, String description ){
		//
		//ReadMdb mdb = new ReadMdb();
		File file = new File(fileName);
		//File file = new File(mdb.getFileName());
		//DBFSMInstance Tfile;
		//File file = new File(Tfile.getFilename());
		
		try {
			File tmp = File.createTempFile("tmp", ".java", file.getParentFile());	
			//RepairUtil.conRepair(file,tmp,"NPD",8);
			if(category.equals("RL"))
				RLRepair.closeRepair(file,tmp,iPLine,variable);
			if(category.equals("NPD"))
				//RepairUtil.copyFileIf(file, tmp, 8);
				//NPDRepair.copyFileIf(file,tmp,8);
				//NPDRepair.copyFileByBuf(file,tmp,iPLine,variable);
			if(category.equals("IAO"))
				IAORepair.copyFileByBuf(file, tmp, iPLine,variable,iPLineCode);
			if(category.equals("OOB"))
				BoCheck.boundaryCheck(file,tmp,iPLine,variable);
			//判断多种故障类型
			IOutil.copyFile(tmp,file);
			tmp.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
			

		
	 
		
	}
	

}

