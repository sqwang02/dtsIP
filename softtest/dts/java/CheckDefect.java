package softtest.dts.java;

import java.sql.*;
import java.io.*;
import java.util.*;

import softtest.database.java.DBAccess;

public class CheckDefect {
	private Connection dbconold,dbconnew,dbconresult;
	private String pathnameold,pathnamenew,pathnameresult;
		
	public static void main(String args[]){
		int diff=0;
		if (args.length != 3) {
			System.out.println("Usage：CompareDTSAndMerge \"dtsolddbfile\" \"dtsnewdbfile\" \"dtsresultdbfile\"");
			return;
		}
		CheckDefect check=new CheckDefect();
		check.pathnameold=args[0];
		check.pathnamenew=args[1];
		check.pathnameresult=args[2];
		check.openDatabase();
		
		//对比结果
		Statement stmtdts = null,stmtmerge=null,select=null;
		PreparedStatement pstmt = null;
		try {
			Hashtable<IpRecord,IpRecord> table=new Hashtable<IpRecord,IpRecord>();
			
			//针对结果数据库，产生一个新的唯一编号
			String sql = "";
			ResultSet rs = null;

				
			//dts新数据库的记录放入临时哈希表
			stmtdts = check.dbconnew.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP";
			rs = stmtdts.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				record.file=rs.getString(2);
				record.variable=rs.getString(3);
				record.startline=rs.getInt(4);
				record.ipline=rs.getInt(5);
				/*if(record.category.contains("NPD")||record.category.contains("NPE")){
					// BUG!!!! 下面查找时没有做相同的字符串转换
					//record.category="NPD";
				}else if(record.category.contains("RL")){
					//record.category="RL";
				}else{
					continue;
				}*/
				table.put(record, record);
			}
			//对比数据库中的defect记录是否包含在新数据库中，如果没有则将其添加到结果数据库中
			stmtmerge= check.dbconold.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP where Judge = \'Defect\'";
			rs = stmtmerge.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				record.file=rs.getString(2);
				record.variable=rs.getString(3);
				record.startline=rs.getInt(4);
				record.ipline=rs.getInt(5);
				if (table.containsKey(record)) {
					sql="update IP set Judge = ? where Category = ? and File =? and Variable = ? and StartLine = ? and IPLine = ?";
					pstmt = check.dbconnew.prepareStatement(sql);
					pstmt.setString(1, "Defect");
					pstmt.setString(2, record.category);
					pstmt.setString(3, record.file);
					pstmt.setString(4, record.variable);
					pstmt.setInt(5, record.startline);
					pstmt.setInt(6, record.ipline);
					//更新
					pstmt.executeUpdate();
				}else{
					sql="insert into IP (Category,File,Variable,StartLine,IPLine,Judge) values (?,?,?,?,?,?)";
					pstmt = check.dbconresult.prepareStatement(sql);
					pstmt.setString(1, record.category);
					pstmt.setString(2, record.file);
					pstmt.setString(3, record.variable);
					pstmt.setInt(4, record.startline);
					pstmt.setInt(5, record.ipline);
					pstmt.setString(6, "Defect");
					
					//插入
					pstmt.executeUpdate();
					diff++;
				}
			}
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {
			if (select != null) {
				try {
					select.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (stmtdts != null) {
				try {
					stmtdts.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (stmtmerge != null) {
				try {
					stmtmerge.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		System.out.println("Found "+diff+" records is not in the dts database.");
		
		check.closeDataBase();
	}
	
	private Connection openDatabase(String pathname){
		Connection dbcon =null;
		File file = new File(pathname);
		if (!file.exists()) {
			DBAccess.createMdbFile(pathname);
		}
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		String url = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=" + pathname;
		try {
			Class.forName(driver);
			dbcon = DriverManager.getConnection(url, "", "");
		} catch (ClassNotFoundException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database driver error",ex);
		} catch (SQLException ex) {
			//ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		}
		return dbcon;
	}
	
	private void openDatabase(){
		dbconold=openDatabase(pathnameold);
		dbconnew=openDatabase(pathnamenew);
		dbconresult=openDatabase(pathnameresult);
	}
	
	private void closeDataBase() {
		if (dbconold != null) {
			try {
				dbconold.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbconold = null;
		
		if (dbconnew != null) {
			try {
				dbconnew.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbconnew = null;
		
		if (dbconresult != null) {
			try {
				dbconresult.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbconresult = null;
	}

}