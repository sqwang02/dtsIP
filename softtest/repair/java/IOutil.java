package softtest.repair.java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import softtest.database.java.DBFSMInstance;

public class IOutil {
		/*
		 * 文件拷贝，按字节批量读取
		 * */
		public static void copyFile(File srcFile,File desFile) throws IOException{
			if(!srcFile.exists()){
				throw new IllegalArgumentException("文件："+srcFile+"不存在");
			}
			if(!srcFile.isFile()){
				throw new IllegalArgumentException(srcFile+"不是文件");
			}
			FileInputStream in =new FileInputStream(srcFile);
			FileOutputStream out=new FileOutputStream(desFile);
			byte[] buf = new byte[8*1024];
			int b;
			while((b=in.read(buf, 0, buf.length))!=-1){
				out.write(buf, 0, b);
				out.flush();
			}
			in.close();
			out.close();
			
		}
	/*
	 *文件拷贝， 利用带缓冲的字节流*/
	public static void copyFileByBuf(File srcFile,File desFile) throws IOException{
		
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
		//int c;
		String str = null;
		
		while((str=bi.readLine())!=null){
			if(DBFSMInstance.getDefect() == "fault"){
				bo.write(str);
				bo.newLine();
			}
			
			
			bo.flush();
			
		}
		bi.close();
		bo.close();
		
	}
	/*
	 * 单字节，不带缓冲进行拷贝
	 * */
	public static void copyFileByByte(File srcFile,File desFile) throws IOException{
		if(!srcFile.exists()){
			throw new IllegalArgumentException("文件："+srcFile+"不存在");
		}
		if(!srcFile.isFile()){
			throw new IllegalArgumentException(srcFile+"不是文件");
		}
		FileInputStream in=new FileInputStream(srcFile);
		FileOutputStream out=new FileOutputStream(desFile);
		int c;
		while((c=in.read())!=-1){
			out.write(c);
			out.flush();
		}
 		in.close();
 		out.close();
	}
	public static void isrAndisw(File srcFile,File desFile){
		
	}
	public static void FileCreate(File srcFile){
		if(!srcFile.exists()){
			try {
				srcFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			srcFile.delete();
		}
		
	}
}
