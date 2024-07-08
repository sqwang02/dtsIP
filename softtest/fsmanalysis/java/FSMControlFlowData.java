package softtest.fsmanalysis.java;
import softtest.database.java.*;
/** 用于控制流状态迭代的数据传递 */
public class FSMControlFlowData {
	/** 是否报告错误 */
	boolean reporterror = false;
	
	/** 是否超时标志 **/
	boolean timeout=false;
	
	public void setTimeout(boolean timeout){
		this.timeout=timeout;
	}
	
	public boolean getTimeout(){
		return timeout;
	}
	
	/** 数据库访问接口 */
	DBAccess db = new DBAccess();
	
	String parsefilename="";
	
	public void setParseFileName(String parsefilename){
		this.parsefilename=parsefilename;
	}
	
	public DBAccess getDB(){
		return db;
	}
	
	public void setDB(DBAccess db){
		this.db=db;
	}
}
