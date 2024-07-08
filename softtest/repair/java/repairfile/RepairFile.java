package softtest.repair.java.repairfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import softtest.ast.java.ASTCompilationUnit;
import softtest.database.java.DBFSMInstance;
import softtest.repair.java.BoCheck;
import softtest.repair.java.IAORepair;
import softtest.repair.java.IOutil;
import softtest.repair.java.RLRepair;
import softtest.repair.java.ReadDefect;
import softtest.repair.java.conSynthesis.ConSysFactory;
import softtest.repair.java.conSynthesis.IConditionSynthesis;

public class RepairFile {
	public static void repairFile(ASTCompilationUnit astroot,ReadDefect defectfile,String conditionSyn,int repairstartLine,int repairendLine ){
		File srcFile=new File(defectfile.getFileName());
		File desFile;
		try {
			//������ʱ�ļ������ɺ�ѡ����
			desFile = File.createTempFile("tmp", ".java", srcFile.getParentFile());
			NPDRepair.copyFileByBuf(srcFile,desFile,conditionSyn,repairstartLine,repairendLine);
			
			//�����ع���֤
			
			//����ȷ���������滻Դ��������޸�
			//IOutil.copyFile(desFile,srcFile);
			//ɾ����ʱ�ļ�
			//desFile.delete();
			
			System.out.println("�ɹ������޸�����");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
