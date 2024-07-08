package softtest.repair.java;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;

import softtest.DefUseAnalysis.java.DUAnaysisVistor;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.cfg.java.ControlFlowVisitor;
import softtest.repair.java.conSynthesis.ConditionSynthesisFacade;
import softtest.repair.java.location.ILocation;
import softtest.repair.java.location.LocSetFactory;
import softtest.repair.java.location.LocationSetFacade;
import softtest.repair.java.repairfile.RepairFile;
import softtest.symboltable.java.SymbolFacade;

public class DTSFix {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			long st = System.currentTimeMillis();
			//���ȱ����Ϣ
			ReadDefect defectInformation =new ReadDefect();
			defectInformation=readFileACCESS.readDefectInformation(args[0]);
			//ʵ���������﷨��
			//ASTCompilationUnit astroot=new ASTCompilationUnit();
			//��ȡ���ݿ�
			resultSets rst =new resultSets();
			ResultSet rs=null;
			rs=rst.getResultSet(args[0]);
			try {
				while(rs.next()){
					int i=1;
					String ss=null;
					//if(!(defectInformation.getFileName().equals(ss))){
						//�ؽ�ȱ���ļ��ĳ����﷨��
						//astroot=ASTbuild.astBuild(defectInformation.getFileName());System.out.println("���ɳ����﷨��...");
					String parsefilename=defectInformation.getFileName(); 
					JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(parsefilename)));
					parser.setJDK15();
					ASTCompilationUnit astroot = parser.CompilationUnit();
					//}				
					
					//����������ͼ
					System.out.println("���ɿ�����ͼ...");
					astroot.jjtAccept(new ControlFlowVisitor(), null);
					
					//�������ű�
					System.out.println("���ɷ��ű�...");
					new SymbolFacade().initializeWith(astroot);
					
					//������ʹ��
					System.out.println("���ɶ���ʹ����...");
					astroot.jjtAccept(new DUAnaysisVistor() , null);
					
				    ss=defectInformation.getFileName();
					ConditionSynthesisFacade ConditionSynthesis=new ConditionSynthesisFacade();
					String condition= ConditionSynthesis.getConditionSynthesis(astroot, defectInformation);
					LocationSetFacade repairLocation =new LocationSetFacade();
					int repairstartLine=repairLocation.getRepairLine(astroot,defectInformation)[0];
					int repairendLine=repairLocation.getRepairLine(astroot,defectInformation)[1];
					RepairFile.repairFile(astroot,defectInformation,condition,repairstartLine,repairendLine);
					long et = System.currentTimeMillis();
					System.out.println( et - st);
					
					
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

	}

}
