package softtest.repair.java.repairfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import softtest.ast.java.*;

import softtest.database.java.DBFSMInstance;

public class NPDRepair {
	public static void copyFileByBuf(File srcFile,File desFile,String condition,int repairstartLine,int repairendLine) throws IOException{
		
		//if(!srcFile.exists()){
		//	throw new IllegalArgumentException("文件："+srcFile+"不存在");
	//	}
		//if(!srcFile.isFile()){
		//	throw new IllegalArgumentException(srcFile+"不是文件");
		//}
		//BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
		//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desFile));
		
		FileReader FReader = new FileReader(srcFile);
		FileWriter FWriter = new FileWriter(desFile);
		BufferedReader bi = new BufferedReader(FReader); 
		BufferedWriter bo = new BufferedWriter(FWriter);
		//DBFSMInstance ss;
		int i=0;
		String str = null;			
		while((str=bi.readLine())!=null){
			i++;
			if(i == repairstartLine){//if(i == ss.getIPLine()){
				//if(ss.getDefect()=="NPD")//判定修复
				//bo.write("\t\t"+"if(!("+ss.getVariable()+")){");
				//bo.write("\t\t"+"if("+condition+"){"+str+"}");
				if(repairstartLine != repairendLine){
					bo.write(""+"if("+condition+"){");
					bo.newLine();
					bo.write(str);					
				}
				else{
					bo.write("\t\t"+condition);
					bo.newLine();
					bo.write(str);
					bo.newLine();
					bo.write("}");
					continue;
				}
				//bo.newLine();
			//	bo.write("\t\t}");
				bo.newLine();
				bo.flush();
			}
		
			if(i==repairendLine)
				bo.write(str+"}");
			else{
				bo.write(str);
				bo.newLine();
				bo.flush();
			}
		}
		bi.close();
		bo.close();
		}

	public static void copyFileIf(File srcFile,File desFile,int Ipline,String variable) throws IOException{
		
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
		int i=0;
		String str = null;
		String ss = null;
		String st = null;
		String content = "true";
		while((str=bi.readLine())!=null){
			i++;
			if(i == Ipline-1){//if(i == DBFInstance.getIPLine()){
				if(str.contains("if")&&!(str.contains("{"))){
					int index = str.lastIndexOf(")");
					StringBuffer tr = new StringBuffer(str);
					ss = tr.insert(index, "&&"+content).toString();
					st = tr.append("{").toString();
					//bo.write("\t\t"+"if("+content+"){");
					bo.write(ss);
					bo.write(st);
					bo.newLine();
					bo.flush();
					}
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
