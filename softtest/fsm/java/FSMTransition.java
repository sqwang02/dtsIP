package softtest.fsm.java;

import java.util.*;
import softtest.cfg.java.*;
import org.w3c.dom.Node;

/** ����״̬���ֵ�״̬ת�� */
public class FSMTransition extends FSMElement implements Comparable<FSMTransition> {
	/** ���� */
	private String name = null;

	/** ����״̬ */
	private FSMState tostate = null;

	/** ����״̬ */
	private FSMState fromstate = null;

	/** ��ţ�Ҳ���ڱȽϵ����� */
	private int snumber = 0;

	/** �����б�������������Ϊ��Ĺ�ϵ */
	private List<FSMCondition> conditions = new LinkedList<FSMCondition>();

	/** ������ģʽ��accept���� */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** �Ƚ�״̬ת����˳���������� */
	public int compareTo(FSMTransition e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/** ������� */
	public void setSnumber(int snumber){
		this.snumber=snumber;
	}
	
	/** ������ */
	public int getSnumber(){
		return snumber;
	}

	/** �Ը����ִ���һ��ת������ʱ�����͵��ﶼΪ�գ����趨 */
	public FSMTransition(String name) {
		this.name = name;
	}

	/** ��ָ�������֣������͵��ﴴ��ת�� */
	public FSMTransition(String name, FSMState fromstate, FSMState tostate) {
		this.name = name;
		this.fromstate = fromstate;
		this.tostate = tostate;
		tostate.getInTransitions().put(name, this);
		fromstate.getOutTransitions().put(name, this);
	}

	/** ��ó���״̬ */
	public FSMState getFromState() {
		return fromstate;
	}

	/** ���ó���״̬ */
	public void setFromState(FSMState fromstate) {
		this.fromstate = fromstate;
	}

	/** ��õ���״̬ */
	public FSMState getToState() {
		return tostate;
	}

	/** ���õ���״̬ */
	public void setToState(FSMState tostate) {
		this.tostate = tostate;
	}

	/** ������� */
	public String getName() {
		return name;
	}
	
	/** �������� */
	public void setName(String name) {
		this.name=name;
	}

	/** �������� */
	public void addCondition(FSMCondition condition) {
		conditions.add(condition);
		condition.fsm = fsm;
	}

	/** ��������б� */
	public List<FSMCondition> getConditions() {
		return conditions;
	}
	
	/** ��������б� */
	public void setConditions(List<FSMCondition> conditions) {
		this.conditions=conditions;
	}

	/** ����xml */
	@Override
	public void loadXML(Node n) {
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}

	/** ��ת���ϵ������б���м��㣬�����Ƿ������������� */
	public boolean evaluate(FSMMachineInstance fsm,FSMStateInstance state, VexNode vex) {
		boolean b=false;
		Iterator<FSMCondition> i = conditions.iterator();
		while (i.hasNext()) {
			FSMCondition condition = i.next();
			if (!condition.evaluate(fsm,state,vex)) {
				return false;
			}
		}
		//	���ù�������
		if (relatedmethod == null) {
			b = true;
		} else {
			Object[] args = new Object[2];
			args[0] = vex;
			args[1] = fsm;
			try {
				Boolean r = (Boolean) relatedmethod.invoke(null, args);
				b = r;
			} catch (Exception e) {
				if(softtest.config.java.Config.DEBUG){
					e.printStackTrace();
				}
				throw new RuntimeException("action error",e);
			}
		}
		return b;
	}
	
	/** ��ӡ */
	@Override
	public String toString() {
		return name;
	}
}
