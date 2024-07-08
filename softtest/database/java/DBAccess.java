package softtest.database.java;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.Date;

import softtest.config.java.Config;
import softtest.fsmanalysis.java.ProjectAnalysis;

public class DBAccess {
	/** 数据库连接 */
	private Connection dbcon;
	private Connection statcon;
	
	private int IP_ID = 0;
	
	private Map getIpCountByCategory() {		
		PreparedStatement select = null;
		Map stat = new HashMap();
		try {
			String strSQL="SELECT defect,category FROM IP WHERE Num > ?";
			select = dbcon.prepareStatement(strSQL);
			select.setInt(1, IP_ID);
			ResultSet result = select.executeQuery();
			
			while (result.next()) {
				String defect = result.getString(1);
				String category = result.getString(2);
				String key = defect.concat(" ").concat(category);
				if (stat.containsKey(key)) {
					int num = (Integer) stat.get(key);
					stat.put(key, num+1);
				} else {
					stat.put(key, 1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Access database connect error",e);
		}
		finally {
			if (select != null) {
				try { select.close(); } catch (Exception e) {}
			}
		}
		return stat;
	}
	
	private int getMaxIpId() {
		int key = 0;
		Statement select = null;
		try {
			String strSQL="SELECT max(Num) FROM IP";
			select = dbcon.createStatement();
			ResultSet result = select.executeQuery(strSQL);
			// 产生一个新的唯一编号
			if (result.next()) {
				String str = result.getString(1);
				if (str != null) {
					key = Integer.parseInt(str);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Access database connect error",e);
		}
		finally {
			if (select != null) {
				try { select.close(); } catch (Exception e) {}
			}
		}
		return key;
	}
	
	private int getMaxScanId() {
		int key = 0;
		Statement select = null;
		try {
			String strSQL="SELECT MAX(id) FROM total";
			select = statcon.createStatement();
			ResultSet result = select.executeQuery(strSQL);
			// 产生一个新的唯一编号
			if (result.next()) {
				String str = result.getString(1);
				if (str != null) {
					key = Integer.parseInt(str);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Access database connect error",e);
		}
		finally {
			if (select != null) {
				try { select.close(); } catch (Exception e) {}
			}
		}
		return key;
	}
	
	private int getIpCount() {
		return getMaxIpId() - IP_ID;
	}
	
	private void statistics() {
		openStatDataBase("stat\\stat.mdb");
		
		int ipCount = getIpCount();
		String date = (new Date()).toString().replace(' ', '_');		
		
		exportStatisticsAll(ProjectAnalysis.project, 
							date, 
							ProjectAnalysis.usedtime, 
							"java", 
							Config.version, 
							ProjectAnalysis.filecount, 
							ProjectAnalysis.linecount, 
							ipCount, 
							0);
		
		int scanid = getMaxScanId();
		Map ipstat = getIpCountByCategory();
		for (Object obj : ipstat.keySet()) {
			String key = (String) obj;
			String keys[] = key.split(" ");
			String defect = keys[0];
			String category = keys[1];
			exportStatistics(ProjectAnalysis.project, 
							 date,
							 ProjectAnalysis.usedtime, 
							 "java", 
							 Config.version, 
							 ProjectAnalysis.filecount, 
							 ProjectAnalysis.linecount, 
							 defect, 
							 category, 
							 (Integer)ipstat.get(key), 
							 0, 
							 scanid);
		}
	}
	
	/**
	 * 输出统计信息
	 * 增加一个统计功能,每次扫描完成后,将下列结果自动统计到一个数据库或者表中.
	 * 包括: 项目名、程序语言、文件个数、代码行、ip总数或每种缺陷ip数、测试时间、
	 * 扫描过程时间、确认故障数(保留字段,供用户填写)。
	 */	
	private void exportStatisticsAll(String projectName,
			                            String date,
			                            long usedTime,
			                            String language, 
			                            String version,
			                            int fileCount, 
			                            int lineCount, 
			                            int ipCount,
			                            int ackIpCount) {
		PreparedStatement pstmt = null;
		if (statcon == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			// 预编译指令
			String strSQL="insert into total (project,scandate,usedTime,language,dtsversion,filecount,linecount,ipcount,ackipcount) values (?,?,?,?,?,?,?,?,?)";

			pstmt = statcon.prepareStatement(strSQL);
			// 设置插入的值
			pstmt.setString(1, projectName);
			pstmt.setString(2, date);
			pstmt.setInt(3, (int)usedTime);
			pstmt.setString(4, language);
			pstmt.setString(5, version);
			pstmt.setInt(6, fileCount);
			pstmt.setInt(7, lineCount);
			pstmt.setInt(8, ipCount);
			pstmt.setInt(9, ackIpCount);
			// 插入
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
	}
	
	private void exportStatistics(String projectName, 
										String date, 
										long usedTime, 
										String language,
										String version,
										int fileCount, 
										int lineCount,
										String eclass,
										String ekind, 
										int ipCount, 
										int ackIpCount,
										int scanid) {
		PreparedStatement pstmt = null;
		if (statcon == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			// 预编译指令
			String strSQL="insert into detail (project,scandate,usedtime,language,dtsversion,filecount,linecount,defect,category,cat_ipcount,cat_ackipcount,scanid) " +
					"values (?,?,?,?,?,?,?,?,?,?,?,?)";

			pstmt = statcon.prepareStatement(strSQL);
			// 设置插入的值
			pstmt.setString(1, projectName);
			pstmt.setString(2, date);
			pstmt.setInt(3, (int)usedTime);
			pstmt.setString(4, language);
			pstmt.setString(5, version);
			pstmt.setInt(6, fileCount);
			pstmt.setInt(7, lineCount);
			pstmt.setString(8, eclass);
			pstmt.setString(9, ekind);
			pstmt.setInt(10, ipCount);
			pstmt.setInt(11, ackIpCount);
			pstmt.setInt(12, scanid);
			// 插入
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
	}

	/** 文件拷贝 */
	private static void copyFileToFile(String F1, String F2) {
		// 现文件对拷,从F1拷贝到F2,若F2存在则会被覆盖;适用于任何文件.
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(F1); // 建立文件输入流
			fos = new FileOutputStream(F2);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		} catch (FileNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("File path not found",ex);
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
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=" + pathname;
		Connection conn = null;
		Statement stmt = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, "", "");
			stmt = conn.createStatement();
			String sql = "delete * from IP";
			stmt.executeUpdate(sql);
		} catch (ClassNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database driver error",ex);
		} catch (SQLException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
	}

	/** 创建一个新的空的mdb文件 */
	public static void createMdbFile(String pathname) {
		// 拷贝一个备份
		createMdbFile(pathname,"");
	}
	
	/** 创建一个新的空的mdb文件 */
	public static void createMdbFile(String pathname, String template) {
		// 拷贝一个备份
		copyFileToFile(template, pathname);
	}
	
	/** 读取扫描设置 */
	public static List<DBConfig> getScanTypes(String pathname){
		ArrayList<DBConfig> list=new ArrayList<DBConfig>();
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	//	String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=" + pathname;
		
		String url ="jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb, *.accdb);DBQ=D:\\Code\\MyEclipse_workspace\\set\\ConfigJAVA.mdb";
		Connection conn = null;
 		Statement stmt = null;	
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, "","");
			stmt = conn.createStatement();
			String sql = "";
			sql = "select * from ScanSet";
			
			ResultSet rs = stmt.executeQuery(sql);
			/*
			 * 判断config.mdb的ScanSet表的ScanFlag列，该列为真的就将该模式加入模式列表。
			 */
			while (rs.next()) {
				if (rs.getBoolean(5)) {
					DBConfig c=new DBConfig();
					c.defect=rs.getString(2);
					c.category=rs.getString(3);
					list.add(c);
				}
			}
		} catch (ClassNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database driver error",ex);
		} catch (SQLException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		return list;
	}
	
	public void openStatDataBase(String pathname) {
		if (statcon == null) {
			statcon = openDataBase(pathname, "..\\set\\stattemplate.mdb");
		}
	}

	public void openIPDataBase(String pathname) {
		if (dbcon == null) {
			dbcon = openDataBase(pathname, "D:\\Code\\MyEclipse_workspace\\set\\Dbtemplate.Mdb");
			IP_ID = getMaxIpId();
		}
	}
	
	/** 打开数据库连接 */
	public void openDataBase(String pathname) {
		dbcon = openDataBase(pathname, "D:\\Code\\MyEclipse_workspace\\set\\Dbtemplate.Mdb");
		IP_ID = getMaxIpId();
	}
	
	/** 打开数据库连接 */
	public Connection openDataBase(String pathname, String template) {
		Connection con = null;
		File file = new File(pathname);
		if (!file.exists()) {
			DBAccess.createMdbFile(pathname, template);
		}
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	//String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);PWD=" + getPassword() + ";DBQ=" + pathname;
		//String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);PWD=" + getPassword() + ";DBQ=D:\\set\\java_set\\Dbtemplate.mdb";
		String url ="jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb, *.accdb);DBQ=D:\\Code\\MyEclipse_workspace\\set\\Dbtemplate.Mdb";
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, "", "");
		} catch (ClassNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database driver error",ex);
		} catch (SQLException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database connect error:".concat(pathname),ex);
		}
		return con;
	}
	
	public void writeIP(){
		Hashtable<String, List<IPRecord>> table=new Hashtable<String, List<IPRecord>>();
		for(IPRecord r:list_ip){
			List<IPRecord> list=table.get(r.ekind);
			if(list==null){
				list=new ArrayList<IPRecord>();
				table.put(r.ekind, list);
			}
			list.add(r);
		}
		
		for(Enumeration<List<IPRecord>> e= table.elements();e.hasMoreElements();){
			List<IPRecord> list=e.nextElement();
			for(int i=0;i<list.size()*Config.PERCENT/100&&i<Config.MAXIP;i++){
				IPRecord r=list.get(i);
				if(exportErrorData( r.eclass,  r.ekind, r.id, r.pathname,  r.variable,  r.beginline,  r.errorline,  r.description,  r.code,  r.preconditions,  r.traceinfo, r.emethod)){
					writecount++;
				}
			}
		}
		
	}
	/** 关闭数据库连接 */
	public void closeDataBase() {
		if (!Config.TESTING) {
			statistics();
		}
		
		if (dbcon != null) {
			try {
				dbcon.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbcon = null;
		
		if (statcon != null) {
			try {
				statcon.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		statcon = null;
	}
	
	class IPRecord{
		String eclass;
		String ekind;
		int id;
		String pathname;
		String variable;
		int beginline;
		int errorline;
		String description;
		String code;
		String preconditions;
		String traceinfo;
		String emethod;
		public IPRecord(String eclass, String ekind,int id,String pathname, String variable, int beginline, int errorline, String description, String code, String preconditions, String traceinfo) {
			this.eclass = eclass;
			this.ekind = ekind;
			this.id = id;
			this.pathname = pathname;
			this.variable = variable;
			this.beginline = beginline;
			this.errorline = errorline;
			this.description = description;
			this.code = code;
			this.preconditions = preconditions;
			this.traceinfo = traceinfo;
		}
		
		public IPRecord(String eclass, String ekind,int id,String pathname, String variable, int beginline, int errorline, String description, String code, String preconditions, String traceinfo, String emethod) {
			this(eclass, ekind,id,pathname, variable, beginline, errorline, description, code, preconditions, traceinfo);
			this.emethod = emethod;
		}
	}
	public int writecount=0;
	public ArrayList<IPRecord> list_ip=new ArrayList<IPRecord>();
	
	public void exportErrorDataBuff(String eclass, String ekind, int id,String pathname, String variable,
			int beginline, int errorline, String description,String code,String preconditions,String traceinfo, String emethod) {
		list_ip.add(new IPRecord( eclass,ekind,id,pathname,  variable,  beginline,  errorline,  description,  code,  preconditions,  traceinfo, emethod));
	}
	
	public void exportErrorDataBuff(String eclass, String ekind, int id,String pathname, String variable,
			int beginline, int errorline, String description,String code,String preconditions,String traceinfo) {
		list_ip.add(new IPRecord( eclass,  ekind,  id,  pathname,  variable,  beginline,  errorline,  description,  code,  preconditions,  traceinfo));
	}
	

	/**
	 * 向数据库输出一条错误记录
	 * @param eclass 缺陷
	 * @param ekind 类别
	 * @param id 结点ID
	 * @param pathname 文件
	 * @param variable 变量名
	 * @param beginline 起始行
	 * @param errorline 错误行
	 * @param description 错误描述
	 * @param code 错误行代码
	 * @param preconditions 前置条件
	 * @param traceinfo 跨函数跟踪信息
	 */
	public boolean exportErrorData(String eclass, String ekind, int id,String pathname, String variable, 
			int beginline, int errorline, String description, String code, String preconditions,String traceinfo, String emethod) {
		
		PreparedStatement select = null;
		PreparedStatement pstmt = null;
		if (dbcon == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			int maxid = getMaxIpId();
			
			String sql="select * from IP where  IPLine=? and Defect=? and Category=? and File=? and StartLine=? and IPLineCode=? and Num>? and Variable=? and Id=?";
			select = dbcon.prepareStatement(sql);
			// 设置值
			select.setString(2, eclass);
			select.setString(3, ekind);
			select.setString(4, pathname);
			select.setInt(5, beginline);
			select.setInt(1, errorline);
			select.setString(6, code);
			select.setInt(7, IP_ID);
			select.setString(8, variable);
			select.setInt(9, id);
			
			/*
			String sql = "select * from IP where IPLine="+errorline
							+" and Defect=\""+eclass+"\""
							+" and Category=\""+ekind+"\""
							+" and File=\""+pathname+"\""
							+" and StartLine="+beginline
							+" and IPLineCode=\""+code+"\"";*/
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			String strSQL="";
			// 预编译指令

			
			strSQL="insert into IP (Defect,Category,Id,File,Variable,StartLine,IPLine," +
					"Description,IPLineCode,PreConditions,TraceInfo)" +
					" values (?,?,?,?,?,?,?,?,?,?,?)";

			pstmt = dbcon.prepareStatement(strSQL);
			// 设置插入的值
			pstmt.setString(1, eclass);
			pstmt.setString(2, ekind);
			pstmt.setInt(3, id);
			pstmt.setString(4, pathname);
			pstmt.setString(5, variable);
			pstmt.setInt(6, beginline);
			pstmt.setInt(7, errorline);
			pstmt.setString(8, description);
			pstmt.setString(9, code);
			pstmt.setString(10, preconditions);
			pstmt.setString(11, traceinfo);
			// 插入
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (select != null) {
				try {
					select.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		return true;
	}
	
	public boolean exportErrorData(String eclass, String ekind,String pathname, String variable, 
			int beginline, int errorline, String description, String code, String preconditions,String traceinfo) {
		
		PreparedStatement select = null;
		PreparedStatement pstmt = null;
		if (dbcon == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			int maxid = getMaxIpId();
			
			String sql="select * from IP where IPLine=? and Defect=? and Category=? and File=? and StartLine=? and IPLineCode=? and Num>? and Variable=?";
			select = dbcon.prepareStatement(sql);
			// 设置值
			select.setString(2, eclass);
			select.setString(3, ekind);
			select.setString(4, pathname);
			select.setInt(5, beginline);
			select.setInt(1, errorline);
			select.setString(6, code);
			select.setInt(7, IP_ID);
			select.setString(8, variable);
			//select.setString(9, emethod);
			/*
			String sql = "select * from IP where IPLine="+errorline
							+" and Defect=\""+eclass+"\""
							+" and Category=\""+ekind+"\""
							+" and File=\""+pathname+"\""
							+" and StartLine="+beginline
							+" and IPLineCode=\""+code+"\"";*/
			ResultSet rs = select.executeQuery();
			if (rs.next()) {
				return false;
			}
			
			String strSQL="";
			// 预编译指令
			strSQL="insert into IP (Defect,Category,File,Variable,StartLine,IPLine,Description,IPLineCode,PreConditions,TraceInfo) values (?,?,?,?,?,?,?,?,?,?)";

			pstmt = dbcon.prepareStatement(strSQL);
			// 设置插入的值
			pstmt.setString(1, eclass);
			pstmt.setString(2, ekind);
			pstmt.setString(3, pathname);
			pstmt.setString(4, variable);
			pstmt.setInt(5, beginline);
			pstmt.setInt(6, errorline);
			pstmt.setString(7, description);
			pstmt.setString(8, code);
			pstmt.setString(9, preconditions);
			pstmt.setString(10, traceinfo);
			// 插入
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (select != null) {
				try {
					select.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					//ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		return true;
	}
	
	
	/**
	 * 向数据库输出分析结果
	 * add by cjie
	 */
	public boolean writeResult(int useTime, int fileCount, int lineCount) {
		PreparedStatement select = null;
		PreparedStatement pstmt = null;
		if (dbcon == null) {
			throw new RuntimeException("database connection closed.");
		}
		try {
			String sql="delete from total";
			select = dbcon.prepareStatement(sql);
			select.executeUpdate();
			
			String strSQL="";
			// 预编译指令
			strSQL="insert into total (usedTime,fileCount,lineCount) values (?,?,?)";

			pstmt = dbcon.prepareStatement(strSQL);
			// 设置插入的值
			pstmt.setInt(1, useTime);
			pstmt.setInt(2, fileCount);
			pstmt.setInt(3, lineCount);
			// 插入
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (select != null) {
				try {
					select.close();
				} catch (SQLException ex) {
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					throw new RuntimeException("Access database connect error",ex);
				}
			}
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
//		List<DBConfig> list=DBAccess.getScanTypes("softtest\\database\\java\\config_CH.mdb");
//		for(DBConfig s:list){
//			System.out.println(s.category);
//		}
		
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
				if (os.getLineNumber() == endline) {
					buff.append(line.substring(0, endcolumn));
				}else{
					buff.append(line);
				}
				
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
	private String getPassword() {
		return Config.DB_STAT_PASSWORD;
	}
}


