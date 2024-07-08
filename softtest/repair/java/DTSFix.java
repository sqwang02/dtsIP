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
			//获得缺陷信息
			ReadDefect defectInformation =new ReadDefect();
			defectInformation=readFileACCESS.readDefectInformation(args[0]);
			//实例化抽象语法树
			//ASTCompilationUnit astroot=new ASTCompilationUnit();
			//读取数据库
			resultSets rst =new resultSets();
			ResultSet rs=null;
			rs=rst.getResultSet(args[0]);
			try {
				while(rs.next()){
					int i=1;
					String ss=null;
					//if(!(defectInformation.getFileName().equals(ss))){
						//重建缺陷文件的抽象语法树
						//astroot=ASTbuild.astBuild(defectInformation.getFileName());System.out.println("生成抽象语法树...");
					String parsefilename=defectInformation.getFileName(); 
					JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(parsefilename)));
					parser.setJDK15();
					ASTCompilationUnit astroot = parser.CompilationUnit();
					//}				
					
					//产生控制流图
					System.out.println("生成控制流图...");
					astroot.jjtAccept(new ControlFlowVisitor(), null);
					
					//产生符号表
					System.out.println("生成符号表...");
					new SymbolFacade().initializeWith(astroot);
					
					//处理定义使用
					System.out.println("生成定义使用链...");
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
