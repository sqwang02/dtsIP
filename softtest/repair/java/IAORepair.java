package softtest.repair.java;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import  softtest.ast.java.*;
import softtest.database.java.DBFSMInstance;
public class IAORepair {
public static void copyFileByBuf(File srcFile,File desFile,int Ipline,String variable,String Iplinecode) throws IOException{
		
		if(!srcFile.exists()){
			throw new IllegalArgumentException("文件："+srcFile+"不存在");
		}
		if(!srcFile.isFile()){
			throw new IllegalArgumentException(srcFile+"不是文件");
		}
		//BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
		//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desFile));
		FileReader FReader = new FileReader(srcFile);
		FileWriter FWriter = new FileWriter(desFile);
		BufferedReader bi = new BufferedReader(FReader); 
		BufferedWriter bo = new BufferedWriter(FWriter);
		DBFSMInstance ss;
		int i=0;
		String str = null;
		String content = " ";
//		ASTbuild.astBuild(srcFile);
		//String content = ConditionSynthesis.conditionSyn(variable);
//		ASTbuild.astBuild(srcFile);
		
		while((str=bi.readLine())!=null){
			i++;
			if(i == Ipline){//if(i == ss.getIPLine()){
				//if(ss.getDefect()=="NPD")//判定修复
				//bo.write("\t\t"+"if(!("+ss.getVariable()+")){");
				//if()
				bo.write("\t\t"+"if("+content+"){");
				bo.newLine();
				bo.write(str);
				bo.newLine();
				bo.write("\t\t}");
				bo.newLine();
				bo.flush();
			}
			else{
				bo.write(str);
				bo.newLine();
				bo.flush();
			}
		}
		bi.close();
		bo.close();
		}

}
