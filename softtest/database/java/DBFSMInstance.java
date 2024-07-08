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
	/** 状态机实例，用于跟踪具体状态机，包含多个当前可能状态 */
		/** 状态机 */
		private FSMMachine fsm = null;
                       
		/** 对应的变量 */
		private VariableNameDeclaration v = null;

		/** 当前可能状态集合 */
		private FSMStateInstanceSet states = null;

		/** 状态机关联的对象 */
		private FSMRelatedCalculation relatedobject = null;
		
		/** 显示错误时用到的字符串 */
		private String resultstring="unknown";
		
		/** 描述故障的字符串 */
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

		/** 构造函数 
		 * @return */
		public void DBFSMInstance() {
			states = new FSMStateInstanceSet();
		}

		/** 拷贝构造函数 */
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

		/** 增加新的状态，考虑条件的融合 */
		public void addStateInstance(FSMStateInstance state) {
			states.addStateInstance(state);
		}
		
		/** 增加新的状态，不考虑条件的融合 */
		public void addStateInstanceWithoutConditon(FSMStateInstance state) {
			states.addStateInstanceWithoutConditon(state);
		}

		/** 设置状态集合 */
		public void setStates(FSMStateInstanceSet states) {
			this.states = states;
		}

		/** 获得状态集合 */
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

		/** 设置结果字符串*/
		public void setResultString(String resultstring){
			this.resultstring=resultstring;
		}
		
		/**
		 * 判断a是否为b的子类
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
		
		/** 设置故障描述 
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
		
		/** 获得结果字符串*/
		public String getResultString(){
			if(v!=null){
				return v.getImage();
			}else{
				return resultstring;
			}
		}
		
		/** 创建一个开始状态实例 */
		public FSMStateInstance createStartStateInstance() {
			FSMState start = fsm.getStartState();
			if (start == null) {
				throw new RuntimeException("The fsm does not have a start state.");
			}
			return new FSMStateInstance(start);
		}

		/** 设置状态机 */
		public void setFSMMachine(FSMMachine fsm) {
			this.fsm = fsm;
		}

		/** 获得状态机 */
		public FSMMachine getFSMMachine() {
			return fsm;
		}

		/** 设置相关变量 */
		public void setRelatedVariable(VariableNameDeclaration v) {
			if(fsm.isVariableRelated()){
				this.v = v;
			}
			else{
				throw new RuntimeException("Try to assign a variable to a nonvariableRelated statemachine.");
			}
		}

		/** 获得相关变量 */
		public VariableNameDeclaration getRelatedVariable() {
			return v;
		}

		/** 设置相关对象 */
		public void setRelatedObject(FSMRelatedCalculation relatedobject) {
			this.relatedobject = relatedobject;
		}

		/** 获得相关对象 */
		public FSMRelatedCalculation getRelatedObject() {
			return relatedobject;
		}

		/** 如果相关对象和变量和状态机相等，则被认为相等 */
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

		/** 因为状态机实例会被用于作为hashtable的key,为了保证值相等的状态机实例有相同的hashcode，所以重写了该方法 */
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

		/** 增加状态条件约束 */
		public void addDomainSet(DomainSet set) {
			states.addDomainSet(set);
		}

		/** 打印 */
		@Override
		public String toString() {
			String s = "";
			s = s + fsm + " on " + getResultString() + ":" + states;
			return s;
		}

		/** 根据当前控制流节点，计算状态机的所有状态条件 */
		public void calDomainSet(VexNode vex) {
			states.calDomainSet(vex);
		}

		/** 根据前趋控制流判断节点和是否为真分支标志，计算状态机的所有状态条件 */
		public void calCondition(VexNode pre, boolean istruebranch) {
			states.calCondition(pre, istruebranch);
		}

		/** 根据caselabel节点和switch节点，计算状态机的所有状态条件 */
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
