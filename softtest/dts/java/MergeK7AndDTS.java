package softtest.dts.java;
import java.io.File;
import java.sql.*;
import java.util.*;

import softtest.database.java.*;


class IpRecord{
	String category;
	String file;
	int ipline;
	int startline;
	String variable;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(category);
		sb.append(",");
		sb.append(ipline);
		sb.append(",");
		sb.append(startline);
		sb.append(",");
		sb.append(variable);
		sb.append(",");
		sb.append(file);
		return sb.toString();
	}
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((category == null) ? 0 : category.toLowerCase().hashCode());
		result = PRIME * result + ((file == null) ? 0 : file.toLowerCase().hashCode());
		result = PRIME * result + ipline;
		result = PRIME * result + startline;
		result = PRIME * result + ((variable == null) ? 0 : variable.toLowerCase().hashCode());
		/*System.out.println("category : "+category+"\t"+category.toLowerCase().hashCode());
		System.out.println("ipline : "+ipline);
		System.out.println("startline : "+startline);
		System.out.println("variable : "+variable+"\t"+variable.toLowerCase().hashCode());
		System.out.println("file : "+file+"\t"+file.toLowerCase().hashCode());*/
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
		} else if (!category.equalsIgnoreCase(other.category))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equalsIgnoreCase(other.file))
			return false;
		if (ipline != other.ipline)
			return false;
		if (startline != other.startline)
			return false;
		
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equalsIgnoreCase(other.variable))
			return false;
		return true;
	}
	
};
public class MergeK7AndDTS {

	
	private Connection dbconk7,dbcondts,dbconresult;
	private String pathnamek7,pathnamedts,pathnameresult;
	
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
		dbconk7=openDatabase(pathnamek7);
		dbcondts=openDatabase(pathnamedts);
		dbconresult=openDatabase(pathnameresult);
	}
	
	private void closeDataBase() {
		if (dbconk7 != null) {
			try {
				dbconk7.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbconk7 = null;
		
		if (dbcondts != null) {
			try {
				dbcondts.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbcondts = null;

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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 3) {
			System.out.println("Usage：MergeK7AndDTS \"K7dbfile\" \"DTSdbfile\" \"resultdbfile\"");
			return;
		}
		MergeK7AndDTS merge=new MergeK7AndDTS();
		merge.pathnamek7=args[0];
		merge.pathnamedts=args[1];
		merge.pathnameresult=args[2];
		merge.openDatabase();
		
		//合并结果
		Statement stmtk7 = null,stmtdts=null,select=null;
		PreparedStatement pstmt = null;
		int count=0;
		try {
			Hashtable<IpRecord,IpRecord> table=new Hashtable<IpRecord,IpRecord>();
			String sql = "";
			ResultSet rs=null;
				
			//dts记录放入临时哈希表
			stmtdts = merge.dbcondts.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP ";//where Judge = \'Defect\'";
			rs = stmtdts.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				/*if(record.category.contains("NPD")||record.category.contains("NPE")){
					record.category="NPD";
				}else if(record.category.contains("RL")){
					record.category="RL";
				}else{
					continue;
				}*/
				record.file=rs.getString(2);
				record.variable=rs.getString(3);
				record.startline=rs.getInt(4);
				record.ipline=rs.getInt(5);
				table.put(record, record);
			}
			
			//k7记录放入临时哈希表
			stmtk7= merge.dbconk7.createStatement();
			sql = "select Category,File,Variable,StartLine,IPLine from IP ";//where Judge = \'Defect\'";
			rs = stmtk7.executeQuery(sql);	
			while (rs.next()) {
				IpRecord record=new IpRecord();
				record.category=rs.getString(1);
				/*if(record.category.contains("NPD")||record.category.contains("NPE")){
					record.category="NPD";
				}else if(record.category.contains("RL")){
					record.category="RL";
				}else{
					continue;
				}*/
				record.file=rs.getString(2);
				record.variable=rs.getString(3);
				record.startline=rs.getInt(4);
				record.ipline=rs.getInt(5);
				table.put(record, record);
			}
			
			for(IpRecord record:table.values()){
				sql="insert into IP (Category,File,Variable,StartLine,IPLine) values (?,?,?,?,?)";
				//sql="insert into IP (Category,File,IPLine,Judge) values (?,?,?,?)";
				pstmt = merge.dbconresult.prepareStatement(sql);
				pstmt.setString(1, record.category);
				pstmt.setString(2, record.file);
				pstmt.setString(3, record.variable);
				pstmt.setInt(4, record.startline);
				pstmt.setInt(5, record.ipline);
				//插入
				pstmt.executeUpdate();
				count++;
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
			if (stmtk7 != null) {
				try {
					stmtk7.close();
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
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		System.out.println("merge "+count+" record.");
		merge.closeDataBase();
	}

}
