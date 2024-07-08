package softtest.repair.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RLRepair {
	public static void closeRepair(File srcFile,File desFile,int Ipline,String variable) throws IOException{
		
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
		String content = "Stream";
		while((str=bi.readLine())!=null){
			i++;
			if(i == Ipline){//if(i == DBFInstance.getIPLine()){
					bo.write("\t\t"+variable+".close;");
					bo.newLine();
					bo.write(str);
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
