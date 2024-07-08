package softtest.repair.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class resultSets {
	public static ResultSet getResultSet(String filepath){
	Properties prop =new Properties();
	prop.put("charSet", "gb2312");
	//prop.put("user", "test0011");
	//prop.put("password","test0011");
	//32位的jdk1.6数据库连接//String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + filepath;
	String url ="jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb, *.accdb);DBQ="+filepath;
	//PreparedStatement ps = null;
	Statement stmt =null;
	ResultSet rs = null;
	//int count = 0;
	//ReadMdb rm = new ReadMdb();
	//ResultSet rm;
	
	
	try{
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		Connection conn = DriverManager.getConnection(url,prop);
		stmt = (Statement)conn.createStatement();
		//
		//ResultSet count;
		//rs = stmt.executeQuery("select *  from IP where Defect='fault'");
		rs = stmt.executeQuery("select Defect,Category,Id,File,Variable,StartLine,IPLine,IPLineCode,Description,PreConditions  from IP where Defect='fault' order by File,IPLine desc");
		


	}catch(Exception e){
		e.printStackTrace();
	}
	return rs;
	}
}
