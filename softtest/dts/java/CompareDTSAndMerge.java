package softtest.dts.java;
import java.sql.*;
import java.io.*;
import java.util.*;

import softtest.database.java.DBAccess;

public class CompareDTSAndMerge {
	private Connection dbcondts,dbconmerge,dbconresult;
	private String pathnamedts,pathnamemerge,pathnameresult;
		
	public static void main(String args[]){
		int diff=0;
		if (args.length != 3) {
			System.out.println("Usage：CompareDTSAndMerge \"dtsdbfile\" \"mergedbfile\" \"resultdbfile\"");
			return;
		}
		CompareDTSAndMerge comp=new CompareDTSAndMerge();
		comp.pathnamedts=args[0];
		comp.pathnamemerge=args[1];
		comp.pathnameresult=args[2];
		comp.openDatabase();
		
		//对比结果
		Statement stmtdts = null,stmtmerge=null,select=null;
		PreparedStatement pstmt = null;
		try {
			Hashtable<IpRecord,IpRecord> table=new Hashtable<IpRecord,IpRecord>();
			
			//针对结果数据库，产生一个新的唯一编号
			String sql = "";
			ResultSet rs = null;

				
			//dts数据库的记录放入临时哈希表
			stmtdts = comp.dbcondts.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP ";//where Juge = \'Defect\'";
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
			//对比总数据库中的defect记录是否包含在dts数据库中，如果没有则将其添加到结果数据库中
			stmtmerge= comp.dbconmerge.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP ";//where Judge = \'Defect\'";
			rs = stmtmerge.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				record.file=rs.getString(2);
				record.variable=rs.getString(3);
				record.startline=rs.getInt(4);
				record.ipline=rs.getInt(5);
				if (table.containsKey(record)) {
				// if(table.contains(record)){
				}else{
					sql="insert into IP (Category,File,Variable,StartLine,IPLine) values (?,?,?,?,?)";
					pstmt = comp.dbconresult.prepareStatement(sql);
					pstmt.setString(1, record.category);
					pstmt.setString(2, record.file);
					pstmt.setString(3, record.variable);
					pstmt.setInt(4, record.startline);
					pstmt.setInt(5, record.ipline);
					
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
		
		comp.closeDataBase();
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
		dbcondts=openDatabase(pathnamedts);
		dbconmerge=openDatabase(pathnamemerge);
		dbconresult=openDatabase(pathnameresult);
	}
	
	private void closeDataBase() {
		if (dbcondts != null) {
			try {
				dbcondts.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbcondts = null;
		
		if (dbconmerge != null) {
			try {
				dbconmerge.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbconmerge = null;

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
