package softtest.database.java;
import java.sql.*;
import java.io.*;
import java.util.*;

class IpRecord{
	String defect;
	String category;
	String file;
	String variable;
	int startline;
	int ipline;
	String iplinecode;
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((category == null) ? 0 : category.hashCode());
		result = PRIME * result + ((defect == null) ? 0 : defect.hashCode());
		result = PRIME * result + ((file == null) ? 0 : file.hashCode());
		result = PRIME * result + ipline;
		result = PRIME * result + ((iplinecode == null) ? 0 : iplinecode.hashCode());
		result = PRIME * result + startline;
		result = PRIME * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IpRecord other = (IpRecord) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (defect == null) {
			if (other.defect != null)
				return false;
		} else if (!defect.equals(other.defect))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (ipline != other.ipline)
			return false;
		if (iplinecode == null) {
			if (other.iplinecode != null)
				return false;
		} else if (!iplinecode.equals(other.iplinecode))
			return false;
		if (startline != other.startline)
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
};

public class DBRegressCompare {
	private Connection dbconold,dbconnew,dbconresult;
	private String pathnameold,pathnamenew,pathnameresult;
		
	public static void main(String args[]){
		int diff=0;
		if (args.length != 3) {
			System.out.println("Usage：DBRegressCompare \"olddbfile\" \"newdbfile\" \"resultdbfile\"");
			return;
		}
		DBRegressCompare comp=new DBRegressCompare();
		comp.pathnameold=args[0];
		comp.pathnamenew=args[1];
		comp.pathnameresult=args[2];
		comp.openDatabase();
		
		//对比结果
		Statement stmtold = null,stmtnew=null,select=null;
		PreparedStatement pstmt = null;
		try {
			Hashtable<IpRecord,IpRecord> table=new Hashtable<IpRecord,IpRecord>();
			
			//针对结果数据库，产生一个新的唯一编号
			select = comp.dbconresult.createStatement();
			String sql = "";
			sql="SELECT MAX(Num) FROM IP";
			ResultSet rs = select.executeQuery(sql);
			int key = 1;
			if (rs.next()) {
				String str = rs.getString(1);
				if (str != null) {
					key = Integer.parseInt(str) + 1;
				}
			}
				
			//新数据库的记录放入临时哈希表
			stmtnew = comp.dbconnew.createStatement();
			sql = "select Defect,Category,File,Variable,StartLine,IPLine,IPLineCode from IP ";//where Juge = \'Defect\'";
			rs = stmtnew.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.defect=rs.getString(1);
				record.category=rs.getString(2);
				record.file=rs.getString(3);
				record.variable=rs.getString(4);
				record.startline=rs.getInt(5);
				record.ipline=rs.getInt(6);
				record.iplinecode=rs.getString(7);
				table.put(record, record);
			}
			
			//对比旧数据库中的defect记录是否包含在新数据库中，如果没有则将其添加到结果数据库中
			stmtold= comp.dbconold.createStatement();
			sql = "select Defect,Category,File,Variable,StartLine,IPLine,IPLineCode from IP where Judge = \'Defect\'";
			rs = stmtold.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.defect=rs.getString(1);
				record.category=rs.getString(2);
				record.file=rs.getString(3);
				record.variable=rs.getString(4);
				record.startline=rs.getInt(5);
				record.ipline=rs.getInt(6);
				record.iplinecode=rs.getString(7);
				
				if(table.containsKey(record)){
					
				}else{
					sql="insert into IP (Num,Defect,Category,File,Variable,StartLine,IPLine,IPLineCode,Judge) values (?,?,?,?,?,?,?,?,?)";
					pstmt = comp.dbconresult.prepareStatement(sql);
					pstmt.setString(1, "" + key++);
					pstmt.setString(2, record.defect);
					pstmt.setString(3, record.category);
					pstmt.setString(4, record.file);
					pstmt.setString(5, record.variable);
					pstmt.setInt(6, record.startline);
					pstmt.setInt(7, record.ipline);
					pstmt.setString(8, record.iplinecode);
					pstmt.setString(9, "Defect");
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
			if (stmtold != null) {
				try {
					stmtold.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
			if (stmtnew != null) {
				try {
					stmtnew.close();
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
		System.out.println("Found "+diff+" records is not in the new database.");
		
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
