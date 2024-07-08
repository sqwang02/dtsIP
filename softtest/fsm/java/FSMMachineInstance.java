package softtest.fsm.java;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

/** 状态机实例，用于跟踪具体状态机，包含多个当前可能状态 */
public class FSMMachineInstance {
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
	private String description = "";
	
	private String precontions="";
	
	private String traceinfo="";
	
	private String switchLine="";

	/** 构造函数 */
	public FSMMachineInstance() {
		states = new FSMStateInstanceSet();
	}

	/** 拷贝构造函数 */
	public FSMMachineInstance(FSMMachineInstance instance) {
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
	
	/** 设置故障描述 */
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
	}
	
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
		if ((o == null) || !(o instanceof FSMMachineInstance)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		boolean b = true;
		FSMMachineInstance x = (FSMMachineInstance) o;
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

	public String getPrecontions() {
		return precontions;
	}

	public void setPrecontions(String precontions) {
		this.precontions = precontions;
	}

	public String getTraceinfo() {
		return traceinfo;
	}

	public void setTraceinfo(String traceinfo) {
		this.traceinfo = traceinfo;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
}
