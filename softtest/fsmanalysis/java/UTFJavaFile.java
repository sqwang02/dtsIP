package softtest.fsmanalysis.java;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;

import softtest.config.java.Config;

import info.monitorenter.cpdetector.io.ASCIIDetector;   
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;   
import info.monitorenter.cpdetector.io.JChardetFacade;   
import info.monitorenter.cpdetector.io.ParsingDetector;   
import info.monitorenter.cpdetector.io.UnicodeDetector; 

public class UTFJavaFile {

	/**
	 * @param args
	 */
	private File file;
	
	public UTFJavaFile(File f){
		file=f;
	}
	
	public boolean processUTF()
	{
		if(!isUTF8(file))
			return false;
		
		boolean flag=true;
		
		RandomAccessFile raf=null;
		try
		{
			raf=new RandomAccessFile(file,"rw");
			byte[] fileStart = new byte[3];
			raf.read(fileStart);
			int utfFlag=0;
			if (fileStart[0] == -17 && fileStart[1] == -69 && fileStart[2] == -65)
			{
				utfFlag=3;																	
			}
			raf.close();
			File tempFile=new File("temp\\"+file.getName());
			copy(file,tempFile,utfFlag);
			transferFile(tempFile,file);		
		}catch(Exception e)
		{
			throw new RuntimeException("Java source file access has a proplem!", e);
		}					
		return flag;
	}
	
	private  void transferFile(File srcFile, File destFile) throws IOException {
		  String line_separator = System.getProperty("line.separator"); 
		  
		  FileInputStream fis = new FileInputStream(srcFile);
		  String content;
		  DataInputStream in = new DataInputStream(fis);
		  BufferedReader d = new BufferedReader(new InputStreamReader(in, "UTF-8")); 
		  Writer ow = new OutputStreamWriter(new FileOutputStream(destFile), "GBK");
		  String line = null;
		 
		  while ((line = d.readLine()) != null)
		  {
		     content=line + line_separator;
		     ow.write(content.toString());
		  }
		  d.close();
		  in.close();
		  fis.close();
		  srcFile.delete();
		  		  		  
		  ow.close();
		 }
	
	public boolean copy(File src, File dest,int utfFlag) {
		
		RandomAccessFile fin=null;
		RandomAccessFile fout=null;
						
		try {
			fin=new RandomAccessFile(src,"r");
			fout=new RandomAccessFile(dest,"rw");
			fin.seek(utfFlag);
			for(int i=0;i<fin.length()-utfFlag;i++)
			{
				fout.writeByte(fin.readByte());
			}
			fin.close();
			fout.close();

		} catch (IOException e) {
			e.printStackTrace();
		}		
		return false;
		
	}
	
    public static boolean isUTF8(File file){   
        try{   
            CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();   
            detector.add(new ParsingDetector(false));   
            detector.add(JChardetFacade.getInstance());   
            detector.add(ASCIIDetector.getInstance());   
            detector.add(UnicodeDetector.getInstance());   
            java.nio.charset.Charset charset = detector.detectCodepage(file.toURI().toURL());   
            if (charset!=null) {   
                if (charset.name().equalsIgnoreCase("UTF-8")||charset.name().equalsIgnoreCase("UTF8")) {   
                    return true;   
                }   
                return false;   
            }   
            return false;   
        }catch(Exception e){   
            e.printStackTrace();   
            return false;   
        }   
    }  

}
