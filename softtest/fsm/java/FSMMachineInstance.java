package softtest.fsm.java;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

/** ״̬��ʵ�������ڸ��پ���״̬�������������ǰ����״̬ */
public class FSMMachineInstance {
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
	private String description = "";
	
	private String precontions="";
	
	private String traceinfo="";
	
	private String switchLine="";

	/** ���캯�� */
	public FSMMachineInstance() {
		states = new FSMStateInstanceSet();
	}

	/** �������캯�� */
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
	
	/** ���ù������� */
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
