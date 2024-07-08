package softtest.repair.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.java_cup.internal.runtime.Scanner;

import softtest.database.java.DBFSMInstance;

public class FileRebuild {
	public static void fileRebuild(File srcFile,File desFile) throws IOException{
		FileReader FReader = new FileReader(srcFile);
		FileWriter FWriter = new FileWriter(desFile);
		BufferedReader bi = new BufferedReader(FReader); 
		BufferedWriter bo = new BufferedWriter(FWriter);
		//int c;
		String str = null;	
		String str1=null;
		String str2=null;
		String str3=null;
		while((str=bi.readLine())!=null){
			if(str.contains("^;[A-Za-z0-9];)")){//^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$
				str1=str.replace(";",";\n");
				str2=str1.replace("{","{\n");
				str3=str2.replace("}","}\n");
			}else
				str3 = str;
				//String[] statements = str.split(";");
			//for(String sta:statements)
			//{
			System.out.println(str3);
			bo.write(str3);
			bo.newLine();
			//}			
			bo.flush();
			
		}
		bi.close();
		bo.close();
	
		
	}

}
