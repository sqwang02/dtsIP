package softtest.repair.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BoCheck {
	public static void boundaryCheck(File srcFile,File desFile,int Ipline,String variable) throws IOException{//����Խ���޸�

		if(!srcFile.exists()){
			throw new IllegalArgumentException("�ļ���"+srcFile+"������");
		}
		if(!srcFile.isFile()){
			throw new IllegalArgumentException(srcFile+"�����ļ�");
		}
		//BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
		//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desFile));
		FileReader FReader = new FileReader(srcFile);
		FileWriter FWriter = new FileWriter(desFile);
		BufferedReader bi = new BufferedReader(FReader); 
		BufferedWriter bo = new BufferedWriter(FWriter);
		int i=0;
		String str = null;
		//String ss = null;
		//String str1 = null;
		String st = null;
		String content = "true";
		while((str=bi.readLine())!=null){
			i++;
			if((i == Ipline-1)&&(str.contains("if"))&&!(str.contains("{"))){//if(i == DBFInstance.getIPLine()){
			
				//int index = str.lastIndexOf(")");
				StringBuffer tr = new StringBuffer(str);
				//ss = tr.insert(index, "&&"+content).toString();
				st = tr.append("{").toString();
					//bo.write("\t\t"+"if("+content+"){");
					//bo.write(ss);
				bo.write(st);
				bo.newLine();
				bo.flush();
				}
			else if(i==Ipline){
				bo.write("\t\t"+"if(!("+content+")){");
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

