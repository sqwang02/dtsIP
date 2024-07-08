package softtest.dts.java;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import softtest.database.java.DBAccess;


public class SimplifyDatabase {
	private Connection dbconold,dbconresult;
	private String pathnameold,pathnameresult;
		
	public static void main(String args[]){
		int diff=0;
		if (args.length != 2) {
			System.out.println("Usage：SimplifyDatabase \"olddbfile\" \"resultdbfile\"");
			return;
		}
		SimplifyDatabase simp=new SimplifyDatabase();
		simp.pathnameold=args[0];
		simp.pathnameresult=args[1];;
		simp.openDatabase();
		
		//对比结果
		Statement stmtdts = null,stmtmerge=null,select=null;
		PreparedStatement pstmt = null;
		try {
			Hashtable<IpRecord,IpRecord> table=new Hashtable<IpRecord,IpRecord>();
			
			//针对结果数据库，产生一个新的唯一编号
			String sql = "";
			ResultSet rs = null;

				
			//数据库的记录放入临时哈希表
			stmtdts = simp.dbconold.createStatement();
			sql = "select Category,File,IPLine from IP where Judge = \'Defect\'";
			rs = stmtdts.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				record.file=rs.getString(2);
				record.ipline=rs.getInt(3);
				if(record.category.contains("NPD")||record.category.contains("NPE")){
					record.category="NPD";
				}else if(record.category.contains("RL")){
					record.category="RL";
				}else{
					continue;
				}
				table.put(record, record);
			}
			
			for(IpRecord record:table.values()){
				sql="insert into IP (Category,File,IPLine,Judge) values (?,?,?,?)";
				pstmt = simp.dbconresult.prepareStatement(sql);
				pstmt.setString(1, record.category);
				pstmt.setString(2, record.file);
				pstmt.setInt(3, record.ipline);
				pstmt.setString(4, "Defect");
				diff++;
				//插入
				pstmt.executeUpdate();
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
		System.out.println("Found "+diff+" defects in the database.");
		
		simp.closeDataBase();
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
