package softtest.dts.java;

import java.sql.*;
import java.io.*;
import java.util.*;

import softtest.database.java.DBAccess;
class Record{
	String category;
	String judge;
	int num;
}
public class GenReport {
	private Connection dbcon;
	private String pathname;
	static int NPD_IP_ALL=0,NPD_RET_IP_ALL=0,NPD_PARAM_IP_ALL=0,	NPD_PRE_CHECK_IP_ALL=0,NPD_EQUAL_NULL_IP_ALL=0,
	NPD_NULL_CHECK_IP_ALL=0,RL_IP_ALL=0,RL_INIT_IP_ALL=0,RL_FLD_IP_ALL=0,IAO_IP_ALL=0,DC_IP_ALL=0,OOB_CON_IP_ALL=0,OOB_IP_ALL=0,
	NPD_D_ALL=0,NPD_RET_D_ALL=0,NPD_PARAM_D_ALL=0,NPD_PRE_CHECK_D_ALL=0,NPD_EQUAL_NULL_D_ALL=0,
	NPD_NULL_CHECK_D_ALL=0,RL_D_ALL=0,RL_INIT_D_ALL=0,RL_FLD_D_ALL=0,IAO_D_ALL=0,DC_D_ALL=0,OOB_CON_D_ALL=0,OOB_D_ALL=0;
	static int ip_ALL=0,defect_ALL=0;
	

	private static void fun(String args){

		GenReport check=new GenReport();
		check.pathname=args;

		check.openDatabase();
		
		//对比结果
		Statement stmt = null;
		try {
			ArrayList<Record> table=new ArrayList<Record>();
			
			//针对结果数据库，产生一个新的唯一编号
			String sql = "";
			ResultSet rs = null;

				
			//dts新数据库的记录放入临时哈希表
			stmt = check.dbcon.createStatement();
			sql = "select Num,Category,Judge,File,Variable,StartLine,IPLine from IP";
			rs = stmt.executeQuery(sql);	
			while (rs.next()) {

				Record record=new Record();
				record.num=rs.getInt(1);
				record.category=rs.getString(2);
				record.judge=rs.getString(3);
				if(record.judge==null){
					record.judge="";
				}
				table.add(record);
			}
			int NPD_IP=0,NPD_RET_IP=0,NPD_PARAM_IP=0,	NPD_PRE_CHECK_IP=0,NPD_EQUAL_NULL_IP=0,
			NPD_NULL_CHECK_IP=0,RL_IP=0,RL_INIT_IP=0,RL_FLD_IP=0,IAO_IP=0,DC_IP=0,OOB_CON_IP=0,OOB_IP=0,
			NPD_D=0,NPD_RET_D=0,NPD_PARAM_D=0,NPD_PRE_CHECK_D=0,NPD_EQUAL_NULL_D=0,
			NPD_NULL_CHECK_D=0,RL_D=0,RL_INIT_D=0,RL_FLD_D=0,IAO_D=0,DC_D=0,OOB_CON_D=0,OOB_D=0;
			int ip=0,defect=0;
			
			ip=table.size();
			ip_ALL+=table.size();
			
			for(Record r:table){
				if(r.judge.equals("Defect")){
					defect++;
					defect_ALL++;
				}
				if(r.category.equals("NPD")){
					NPD_IP++;
					NPD_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_D++;
						NPD_D_ALL++;
					}
				}else if(r.category.equals("NPD_RET")){
					NPD_RET_IP++;
					NPD_RET_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_RET_D++;
						NPD_RET_D_ALL++;
					}
					
				}else if(r.category.equals("NPD_PARAM")){
					NPD_PARAM_IP++;
					NPD_PARAM_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_PARAM_D++;
						NPD_PARAM_D_ALL++;
					}
				}else if(r.category.equals("NPD_PRE_CHECK")){
					NPD_PRE_CHECK_IP++;
					NPD_PRE_CHECK_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_PRE_CHECK_D++;
						NPD_PRE_CHECK_D_ALL++;
					}
				}else if(r.category.equals("NPD_EQUAL_NULL")){
					NPD_EQUAL_NULL_IP++;
					NPD_EQUAL_NULL_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_EQUAL_NULL_D++;
						NPD_EQUAL_NULL_D_ALL++;
					}					
				}else if(r.category.equals("NPD_NULL_CHECK")){
					NPD_NULL_CHECK_IP++;
					NPD_NULL_CHECK_IP_ALL++;
					if(r.judge.equals("Defect")){
						NPD_NULL_CHECK_D++;
						NPD_NULL_CHECK_D_ALL++;
					}		
				}else if(r.category.equals("RL")){
					RL_IP++;
					RL_IP_ALL++;
					if(r.judge.equals("Defect")){
						RL_D++;
						RL_D_ALL++;
					}					
				}else if(r.category.equals("RL_INIT")){
					RL_INIT_IP++;
					RL_INIT_IP_ALL++;
					if(r.judge.equals("Defect")){
						RL_INIT_D++;
						RL_INIT_D_ALL++;
					}	
				}else if(r.category.equals("RL_FLD")){
					RL_FLD_IP++;
					RL_FLD_IP_ALL++;
					if(r.judge.equals("Defect")){
						RL_FLD_D++;
						RL_FLD_D_ALL++;
					}						
				}else if(r.category.equals("IAO")){
					IAO_IP++;
					IAO_IP_ALL++;
					if(r.judge.equals("Defect")){
						IAO_D++;
						IAO_D_ALL++;
					}						
				}else if(r.category.equals("DC")){
					DC_IP++;
					DC_IP_ALL++;
					if(r.judge.equals("Defect")){
						DC_D++;
						DC_D_ALL++;
					}					
				}else if(r.category.equals("OOB_CON")){
					OOB_CON_IP++;
					OOB_CON_IP_ALL++;
					if(r.judge.equals("Defect")){
						OOB_CON_D++;
						OOB_CON_D_ALL++;
					}						
				}else if(r.category.equals("OOB")){
					OOB_IP++;
					OOB_IP_ALL++;
					if(r.judge.equals("Defect")){
						OOB_D++;
						OOB_D_ALL++;
					}						
				}else{
					throw new RuntimeException("ERROR!");
				}
		
			}
			
			
			System.out.println("NPD:\t"+NPD_D+"\\"+NPD_IP);
			System.out.println("NPD_RET:\t"+NPD_RET_D+"\\"+NPD_RET_IP);
			System.out.println("NPD_PARAM:\t"+NPD_PARAM_D+"\\"+NPD_PARAM_IP);
			System.out.println("NPD_PRE_CHECK:\t"+NPD_PRE_CHECK_D+"\\"+NPD_PRE_CHECK_IP);
			System.out.println("NPD_EQUAL_NULL:\t"+NPD_EQUAL_NULL_D+"\\"+NPD_EQUAL_NULL_IP);
			System.out.println("NPD_NULL_CHECK:\t"+NPD_NULL_CHECK_D+"\\"+NPD_NULL_CHECK_IP);
			System.out.println("RL:\t"+RL_D+"\\"+RL_IP);
			System.out.println("RL_INIT:\t"+RL_INIT_D+"\\"+RL_INIT_IP);
			System.out.println("RL_FLD:\t"+RL_FLD_D+"\\"+RL_FLD_IP);
			System.out.println("IAO:\t"+IAO_D+"\\"+IAO_IP);
			System.out.println("DC:\t"+DC_D+"\\"+DC_IP);
			System.out.println("OOB_CON:\t"+OOB_CON_D+"\\"+OOB_CON_IP);
			System.out.println("OOB:\t"+OOB_D+"\\"+OOB_IP);
			System.out.println("合计:\t"+defect+"\\"+ip);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Access database connect error",ex);
		} finally {

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					throw new RuntimeException("Access database connect error",ex);
				}
			}
		}
		
		check.closeDataBase();
	}
	public static void main(String args[]){
		/*fun("D:\\back\\path\\path_sensitive\\areca_DTS.mdb"); 
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\aTunes_DTS.mdb");
		System.out.println();
		
		fun("D:\\back\\path\\path_sensitive\\Azureus_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\cobra_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\freecol_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\freemind_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\jstock_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\megamek_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\robocode_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_sensitive\\SweetHome3D_DTS.mdb");
		System.out.println();*/
		
		/*fun("D:\\back\\path\\path_insensitive\\areca_DTS.mdb"); 
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\aTunes_DTS.mdb");
		System.out.println();
		
		fun("D:\\back\\path\\path_insensitive\\Azureus_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\cobra_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\freecol_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\freemind_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\jstock_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\megamek_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\robocode_DTS.mdb");
		System.out.println();

		fun("D:\\back\\path\\path_insensitive\\SweetHome3D_DTS.mdb");
		System.out.println();*/
		

		fun("D:\\back\\summary\\notusesummary\\areca_DTS.mdb"); 
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\aTunes_DTS.mdb");
		System.out.println();
		
		fun("D:\\back\\summary\\notusesummary\\Azureus_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\cobra_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\freecol_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\freemind_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\jstock_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\megamek_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\robocode_DTS.mdb");
		System.out.println();

		fun("D:\\back\\summary\\notusesummary\\SweetHome3D_DTS.mdb");
		System.out.println();
		
		System.out.println("NPD:\t"+NPD_D_ALL+"\\"+NPD_IP_ALL);
		System.out.println("NPD_RET:\t"+NPD_RET_D_ALL+"\\"+NPD_RET_IP_ALL);
		System.out.println("NPD_PARAM:\t"+NPD_PARAM_D_ALL+"\\"+NPD_PARAM_IP_ALL);
		System.out.println("NPD_PRE_CHECK:\t"+NPD_PRE_CHECK_D_ALL+"\\"+NPD_PRE_CHECK_IP_ALL);
		System.out.println("NPD_EQUAL_NULL:\t"+NPD_EQUAL_NULL_D_ALL+"\\"+NPD_EQUAL_NULL_IP_ALL);
		System.out.println("NPD_NULL_CHECK:\t"+NPD_NULL_CHECK_D_ALL+"\\"+NPD_NULL_CHECK_IP_ALL);
		System.out.println("RL:\t"+RL_D_ALL+"\\"+RL_IP_ALL);
		System.out.println("RL_INIT:\t"+RL_INIT_D_ALL+"\\"+RL_INIT_IP_ALL);
		System.out.println("RL_FLD:\t"+RL_FLD_D_ALL+"\\"+RL_FLD_IP_ALL);
		System.out.println("IAO:\t"+IAO_D_ALL+"\\"+IAO_IP_ALL);
		System.out.println("DC:\t"+DC_D_ALL+"\\"+DC_IP_ALL);
		System.out.println("OOB_CON:\t"+OOB_CON_D_ALL+"\\"+OOB_CON_IP_ALL);
		System.out.println("OOB:\t"+OOB_D_ALL+"\\"+OOB_IP_ALL);
		System.out.println("合计:\t"+defect_ALL+"\\"+ip_ALL);
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
		dbcon=openDatabase(pathname);
	}
	
	private void closeDataBase() {
		if (dbcon != null) {
			try {
				dbcon.close();
			} catch (SQLException ex) {
				//ex.printStackTrace();
				throw new RuntimeException("Access database connect error",ex);
			}
		}
		dbcon = null;
	}

}