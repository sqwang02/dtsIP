package softtest.fsmanalysis.java;
import softtest.database.java.*;
/** ���ڿ�����״̬���������ݴ��� */
public class FSMControlFlowData {
	/** �Ƿ񱨸���� */
	boolean reporterror = false;
	
	/** �Ƿ�ʱ��־ **/
	boolean timeout=false;
	
	public void setTimeout(boolean timeout){
		this.timeout=timeout;
	}
	
	public boolean getTimeout(){
		return timeout;
	}
	
	/** ���ݿ���ʽӿ� */
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
