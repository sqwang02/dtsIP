package softtest.database.java;

import java.io.*;
import java.util.*;

import com.healthmarketscience.jackcess.*;

public class GeneralDBAccess extends DBAccess {
	private Database db = null;
	/*		strSQL="insert into IP (Defect,Category,File,Variable,StartLine,IPLine," +
	"Description,IPLineCode,PreConditions,TraceInfo, Method)" +
	" values (?,?,?,?,?,?,?,?,?,?,?)";*/
	/** 文件拷贝 */
	private static void copyFileToFile(String F1, String F2) {
		// 现文件对拷,从F1拷贝到F2,若F2存在则会被覆盖;适用于任何文件.
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			F1 = F1.replace('\\', File.separatorChar).replace('/', File.separatorChar);
			F2 = F2.replace('\\', File.separatorChar).replace('/', File.separatorChar);
			
			fis = new FileInputStream(F1); // 建立文件输入流
			fos = new FileOutputStream(F2);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		} catch (FileNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("File not found" ,ex);
		} catch (IOException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("IO Exception",ex);
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (fos != null)
					fos.close();
			} catch (IOException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("IO Exception",ex);
			}
		}
	}
	
	/** 清空mdb文件的数据 */
	private static void clearData(String pathname) {
		try {
			pathname = pathname.replace('\\', File.separatorChar).replace('/', File.separatorChar);
			
			Database db = Database.open(new File(pathname));
			Table ipTable = db.getTable("IP");
			ipTable.reset();
			while (ipTable.getRowCount() > 0) {
				ipTable.deleteCurrentRow();
			}
			db.close();
		} catch (IOException e) {			
			throw new RuntimeException("error occurred when clearing the mdb file \""+pathname+"\"",e);
		}
	}
	
	/** 创建一个新的空的mdb文件 */
	public static void createMdbFile(String pathname) {
		// 拷贝一个备份
		copyFileToFile("..\\set\\Dbtemplate.Mdb", pathname);
		clearData(pathname);
	}
	
	/** 读取扫描设置 */
	public static List<DBConfig> getScanTypes(String pathname){
		ArrayList<DBConfig> list=new ArrayList<DBConfig>();
		
		try {
			pathname = pathname.replace('\\', File.separatorChar).replace('/', File.separatorChar);
			
			Database db = Database.open(new File(pathname));
			Table confTable = db.getTable("ScanSet");
			confTable.reset();
			
			while (true) {
				Map<String,Object> row = confTable.getNextRow();
				if (row == null) {
					break;
				}
				
				if (row.get("ScanFlag") instanceof Boolean && row.get("ScanFlag").equals(Boolean.TRUE)) {					
					DBConfig c=new DBConfig();
					c.defect=(String)row.get("Defect");
					c.category=(String)row.get("Category");					
					list.add(c);
				}
			}
		}  catch (IOException e) {			
			throw new RuntimeException("error occurred when scan the mdb file \""+pathname+"\"",e);
		}
		return list;
	}

	/** 打开数据库连接 */
	@Override
	public void openDataBase(String pathname) {
		if (db != null) {
			return;
		}
		File file = new File(pathname);
		if (!file.exists()) {
			GeneralDBAccess.createMdbFile(pathname);
		}		
		
		try {
			db = Database.open(file);
		} catch (IOException e) {
			throw new RuntimeException("Access database connect error", e);
		}
	}

	/** 关闭数据库连接 */
	@Override
	public void closeDataBase() {
		if (db != null) {
			try {
				db.close();
			} catch (IOException e) {
				throw new RuntimeException("Access database connect error", e);
			}
		}
		db = null;
	}

	
	@Override
	public void exportErrorDataBuff(String eclass, String ekind, int id,String pathname, String variable, int beginline, int errorline, String description, String code, String preconditions, String traceinfo) {
		exportErrorData(eclass, ekind, pathname, variable, beginline,
				errorline, description, code, preconditions, traceinfo);
	}
	@Override
	public void exportErrorDataBuff(String eclass, String ekind,int id,String pathname, String variable, int beginline, int errorline, String description, String code, String preconditions, String traceinfo, String emethod) {
		exportErrorData(eclass, ekind,id,pathname, variable, beginline,
				errorline, description, code, preconditions, traceinfo, emethod);
	}

	/**
	 * 向数据库输出一条错误记录
	 * @param eclass 缺陷
	 * @param ekind 类别
	 * @param pathname 文件
	 * @param variable 变量名
	 * @param beginline 起始行
	 * @param errorline 错误行
	 * @param description 错误描述
	 * @param code 错误行代码
	 * @param preconditions 前置条件
	 * @param traceinfo 跨函数跟踪信息
	 */
	@Override
	public boolean exportErrorData(String eclass, String ekind, String pathname, String variable, int beginline, int errorline, String description,String code,String preconditions,String traceinfo) {
		if (db == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			Table ipTable = db.getTable("IP");			
			
			// insert into IP (Num,Defect,Category,File,Variable,StartLine,IPLine,IPLineCode)
			// values (?,?,?,?,?,?,?,?)
			ipTable.addRow(null,
					Boolean.FALSE,
							null,
							ekind,
							eclass,
							pathname,
							(variable!=null && variable.length()>50)?variable.substring(0,50):variable,
							beginline,
							errorline,
							code,
							description,
							preconditions,
							traceinfo,
							null,
							null);
		} catch (IOException e) {
			throw new RuntimeException("Access database connect error", e);
		}
		return true;
	}
	@Override
	public boolean exportErrorData(String eclass, String ekind,int id, String pathname, String variable, int beginline, int errorline, String description,String code,String preconditions,String traceinfo, String emethod) {
		if (db == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			Table ipTable = db.getTable("IP");			
			
			// insert into IP (Num,Defect,Category,File,Variable,StartLine,IPLine,IPLineCode)
			// values (?,?,?,?,?,?,?,?)
			ipTable.addRow(null,
					Boolean.FALSE,
							null,
							ekind,
							eclass,
							pathname,
							(variable!=null && variable.length()>50)?variable.substring(0,50):variable,
							beginline,
							errorline,
							code,
							description,
							preconditions,
							traceinfo,
							emethod,
							null,
							null);
		} catch (IOException e) {
			throw new RuntimeException("Access database connect error", e);
		}
		return true;
	}
	
	public static void main(String args[]) {
		//DBAccess test = new DBAccess();
		// 第一次会自动创建一个mdb文件，以后会附加方式
		//test.openDataBase("softtest\\database\\java\\test.mdb");
		//test.exportErrorData("test", "test", "test", "test", 4, 4);
		//test.exportErrorData("test", "test", "test", "test", 6, 6);
		//test.closeDataBase();
		//System.out.println("ok");
		/*List<DBConfig> list=GeneralDBAccess.getScanTypes("softtest\\database\\java\\config_CH.mdb");
		for(DBConfig s:list){
			System.out.println(s.category);
		}*/
		try {
			Database db = Database.open(new File("E:\\forlinux\\dts_java_test\\results\\test4.mdb"));
			System.out.print(db.getTable("IP").display());
			for (Column col : db.getTable("IP").getColumns()) {
				System.out.println(col.getName()+" "+col.getLengthInUnits()+" "+col.getType());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
		
	}
	
	/**
	 * 根据参数读取源码信息，文件路径，起始行号，起始列号，终止行号，终止列号（都是从1开始）。 如果要读的行上，列号不合法将抛出越界异常
	 */
	public static String getSouceCode(String path, int beginline, int begincolumn,
			int endline, int endcolumn) {
		LineNumberReader os = null;
		String line = null;
		StringBuffer buff = new StringBuffer();
		// 判断参数合法性
		if ((beginline > endline)
				|| (beginline == endline && begincolumn > endcolumn)) {
			return "";
		}
		if(path==null||path.equals("")||!path.toLowerCase().endsWith(".java")){
			return "";
		}
		try {
			os = new LineNumberReader(new FileReader(path));
			// 跳到起始行处
			do {
				line = os.readLine();
			} while (line != null && os.getLineNumber() < beginline);

			// 添加起始行上的源码到buff
			if (line != null) {
				if (beginline == endline) {
					buff.append(line.substring(begincolumn - 1, endcolumn));
				} else {
					buff.append(line.substring(begincolumn - 1));
				}
			}

			// 如果起始行和终止行不在同一行则继续读
			while (line != null && os.getLineNumber() < endline) {
				line = os.readLine();
				buff.append("\n");
				buff.append(line.substring(0, endcolumn));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buff.toString();
	}
	
	/**
	 * 根据参数读取源码信息，文件路径，起始行号，终止行号， 如果要读的行上不合法将抛出越界异常
	 */
	public static String getSouceCode(String path, int beginline, int endline) {
		LineNumberReader os = null;
		String line = null;
		StringBuffer buff = new StringBuffer();
		// 判断参数合法性
		if (beginline > endline) {
			return "";
		}
		if(path==null||path.equals("")||!path.toLowerCase().endsWith(".java")){
			return "";
		}
		try {
			os = new LineNumberReader(new FileReader(path));
			// 跳到起始行处
			do {
				line = os.readLine();
			} while (line != null && os.getLineNumber() < beginline);

			// 添加起始行上的源码到buff
			if (line != null) {
				if (beginline == endline) {
					buff.append(line);
				} else {
					buff.append(line);
				}
			}

			// 如果起始行和终止行不在同一行则继续读
			while (line != null && os.getLineNumber() < endline) {
				line = os.readLine();
				buff.append("\n");
				buff.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buff.toString();
	}
}


