package softtest.repair.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.SimpleJavaNode;

public class readFileACCESS {
	public static  ReadDefect readDefectInformation(String filepath){
		resultSets rst =new resultSets();
		ResultSet rs=null;
		rs=rst.getResultSet(filepath);
		ReadDefect mdb = new ReadDefect();
		try {
			while(rs.next()){
					String ss=null;
					int i=1;
					//Map map = new HashMap();
					
					//for(int i = 1;i<data.getColumnCount();i++){
					//String columnName = data.getColumnName(i);
					String columnValue = rs.getString(i);
					//System.out.println(columnName+":"+columnValue);
					mdb.setDefect(columnValue);
					mdb.setCategory(rs.getString(i+1));
					mdb.setId(rs.getInt(i+2));
					mdb.setFileName(rs.getString(i+3));
					mdb.setVariable(rs.getString(i+4));
					mdb.setStartLine(Integer.parseInt(rs.getString(i+5)));
					mdb.setIPLine(Integer.parseInt(rs.getString(i+6)));
					mdb.setIPLineCode(rs.getString(i+7));					
					//通过检测到的抽象语法树的节点Id找到对应节点，通过节点找相应的使用-定义
					//寻找使用-定义
					//DefectRepair.RepairStr(mdb.getDefect(),mdb.getCategory(),mdb.getId(),
					//		mdb.getFileName(),mdb.getVariable(),mdb.getStartLine(),
					//		mdb.getIPLine(),mdb.getIPLineCode(),mdb.getDescription());
					}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return mdb;
	}
}

