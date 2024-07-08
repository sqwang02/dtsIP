package softtest.database.java;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

import softtest.config.java.Config;
import softtest.database.java.DBAccess.IPRecord;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.fsm.java.FSMState;
import softtest.fsm.java.FSMStateInstance;
import softtest.fsm.java.FSMStateInstanceSet;

public class DBFSMInstance extends DBAccess{
	DBAccess Db =new DBAccess();
	/** ״̬��ʵ�������ڸ��پ���״̬�������������ǰ����״̬ */
		/** ״̬�� */
		private FSMMachine fsm = null;
                       
		/** ��Ӧ�ı��� */
		private VariableNameDeclaration v = null;

		/** ��ǰ����״̬���� */
		private FSMStateInstanceSet states = null;

		/** ״̬�������Ķ��� */
		private FSMRelatedCalculation relatedobject = null;
		
		/** ��ʾ����ʱ�õ����ַ��� */
		private String resultstring="unknown";
		
		/** �������ϵ��ַ��� */
		private static String Defect;
		private String filename;
		private int startLine;
		private static int IPLine;
		private String IPLineCode;
		private Object traceinfo;
		private Object precontions;
		private String switchLine;
		private String variable;

		public String getVariable() {
			return variable;
		}

		public void setVariable(String variable) {
			this.variable = variable;
		}

		/** ���캯�� 
		 * @return */
		public void DBFSMInstance() {
			states = new FSMStateInstanceSet();
		}

		/** �������캯�� */
		public DBFSMInstance(DBFSMInstance instance) {
			fsm = instance.fsm;
			v = instance.v;
			if(instance.relatedobject!=null){
				relatedobject=instance.relatedobject.copy();
			}
			resultstring=instance.resultstring;
			traceinfo=instance.traceinfo;
			precontions=instance.precontions;
			states = new FSMStateInstanceSet(instance.states);
		}

		/** �����µ�״̬�������������ں� */
		public void addStateInstance(FSMStateInstance state) {
			states.addStateInstance(state);
		}
		
		/** �����µ�״̬���������������ں� */
		public void addStateInstanceWithoutConditon(FSMStateInstance state) {
			states.addStateInstanceWithoutConditon(state);
		}

		/** ����״̬���� */
		public void setStates(FSMStateInstanceSet states) {
			this.states = states;
		}

		/** ���״̬���� */
		public FSMStateInstanceSet getStates() {
			return states;
		}
		
		/**
		 * @return the switchLine
		 */
		public String getSwitchLine() {
			return switchLine;
		}

		/**
		 * @param switchLine the switchLine to set
		 */
		public void setSwitchLine(String switchLine) {
			this.switchLine = switchLine;
		}

		/** ���ý���ַ���*/
		public void setResultString(String resultstring){
			this.resultstring=resultstring;
		}
		
		/**
		 * �ж�a�Ƿ�Ϊb������
		 * @param a
		 * @param b
		 * @return
		 */
		private boolean isSelfOrSuperClass(Class a, Class b) {
			do {
				if (a.equals(b)) {
					return true;
				}
				a = a.getSuperclass();
			}
			while (a != null) ;
			return false;
		}
		
		/** ���ù������� 
		public void fillDescription(int beginline, int errorline) {
			FSMMachine fsmm = this.getFSMMachine();
			if (isSelfOrSuperClass(fsmm.getRelatedClass(),AbstractStateMachine.class)) {
				try {
					AbstractStateMachine sm = (AbstractStateMachine) fsmm.getRelatedClass().newInstance();
					sm.fillDescription(this,beginline,errorline);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}		
		}*/
		
		/** ��ý���ַ���*/
		public String getResultString(){
			if(v!=null){
				return v.getImage();
			}else{
				return resultstring;
			}
		}
		
		/** ����һ����ʼ״̬ʵ�� */
		public FSMStateInstance createStartStateInstance() {
			FSMState start = fsm.getStartState();
			if (start == null) {
				throw new RuntimeException("The fsm does not have a start state.");
			}
			return new FSMStateInstance(start);
		}

		/** ����״̬�� */
		public void setFSMMachine(FSMMachine fsm) {
			this.fsm = fsm;
		}

		/** ���״̬�� */
		public FSMMachine getFSMMachine() {
			return fsm;
		}

		/** ������ر��� */
		public void setRelatedVariable(VariableNameDeclaration v) {
			if(fsm.isVariableRelated()){
				this.v = v;
			}
			else{
				throw new RuntimeException("Try to assign a variable to a nonvariableRelated statemachine.");
			}
		}

		/** �����ر��� */
		public VariableNameDeclaration getRelatedVariable() {
			return v;
		}

		/** ������ض��� */
		public void setRelatedObject(FSMRelatedCalculation relatedobject) {
			this.relatedobject = relatedobject;
		}

		/** �����ض��� */
		public FSMRelatedCalculation getRelatedObject() {
			return relatedobject;
		}

		/** �����ض���ͱ�����״̬����ȣ�����Ϊ��� */
		@Override
		public boolean equals(Object o) {
			if ((o == null) || !(o instanceof DBFSMInstance)) {
				return false;
			}
			if (this == o) {
				return true;
			}
			boolean b = true;
			DBFSMInstance x = (DBFSMInstance) o;
			if (fsm != x.fsm || v != x.v || (relatedobject!=null&&!relatedobject.equals( x.relatedobject))) {
				b = false;
			}
			return b;
		}

		/** ��Ϊ״̬��ʵ���ᱻ������Ϊhashtable��key,Ϊ�˱�ֵ֤��ȵ�״̬��ʵ������ͬ��hashcode��������д�˸÷��� */
		@Override
		public int hashCode() {
			int i = 0;
			if (v != null) {
				i = i + v.hashCode();
			}

			if (relatedobject != null) {
				i = i + relatedobject.hashCode();
			}
			i = i + fsm.hashCode();
			return i;
		}

		/** ����״̬����Լ�� */
		public void addDomainSet(DomainSet set) {
			states.addDomainSet(set);
		}

		/** ��ӡ */
		@Override
		public String toString() {
			String s = "";
			s = s + fsm + " on " + getResultString() + ":" + states;
			return s;
		}

		/** ���ݵ�ǰ�������ڵ㣬����״̬��������״̬���� */
		public void calDomainSet(VexNode vex) {
			states.calDomainSet(vex);
		}

		/** ����ǰ���������жϽڵ���Ƿ�Ϊ���֧��־������״̬��������״̬���� */
		public void calCondition(VexNode pre, boolean istruebranch) {
			states.calCondition(pre, istruebranch);
		}

		/** ����caselabel�ڵ��switch�ڵ㣬����״̬��������״̬���� */
		public void calSwitch(VexNode n, VexNode pre) {
			states.calSwitch(n, pre);
		}
		public static String getDefect() {
			return Defect;
		}
		public void setDefect(String defect,int i) {
			Defect = defect;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public int getStartLine() {
			return startLine;
		}
		public void setStartLine(int startLine) {
			this.startLine = startLine;
		}
		public static int getIPLine() {
			return IPLine;
		}
		public void setIPLine(int iPLine) {
			IPLine = iPLine;
		}
		public String getIPLineCode() {
			return IPLineCode;
		}
		public void setIPLineCode(String iPLineCode) {
			IPLineCode = iPLineCode;
		}
		

	public void writeIP(){
		Hashtable<String, List<IPRecord>> table=new Hashtable<String, List<IPRecord>>();
		for(IPRecord r:list_ip){
			List<IPRecord> list=table.get(r.ekind);
			if(list==null){
				list=new ArrayList<IPRecord>();
				table.put(r.ekind, list);
			}
			list.add(r);
		}
		
		for(Enumeration<List<IPRecord>> e= table.elements();e.hasMoreElements();){
			List<IPRecord> list=e.nextElement();
			for(int i=0;i<list.size()*Config.PERCENT/100&&i<Config.MAXIP;i++){
				IPRecord r=list.get(i);
				if(exportErrorData( r.eclass,  r.ekind, r.id, r.pathname,  r.variable,  r.beginline,  r.errorline,  r.description,  r.code,  r.preconditions,  r.traceinfo, r.emethod)){
					writecount++;
				}
			}
		}
		
	}
	
	
	

}
