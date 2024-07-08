package softtest.fsm.java;

import java.util.*;
import softtest.cfg.java.*;
import org.w3c.dom.Node;

/** 故障状态机种的状态转换 */
public class FSMTransition extends FSMElement implements Comparable<FSMTransition> {
	/** 名称 */
	private String name = null;

	/** 到达状态 */
	private FSMState tostate = null;

	/** 出发状态 */
	private FSMState fromstate = null;

	/** 编号，也用于比较的数字 */
	private int snumber = 0;

	/** 条件列表，条件与条件间为与的关系 */
	private List<FSMCondition> conditions = new LinkedList<FSMCondition>();

	/** 访问者模式的accept方法 */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 比较状态转换的顺序，用于排序 */
	public int compareTo(FSMTransition e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/** 设置序号 */
	public void setSnumber(int snumber){
		this.snumber=snumber;
	}
	
	/** 获得序号 */
	public int getSnumber(){
		return snumber;
	}

	/** 以该名字创建一条转换，此时出发和到达都为空，待设定 */
	public FSMTransition(String name) {
		this.name = name;
	}

	/** 以指定的名字，出发和到达创建转换 */
	public FSMTransition(String name, FSMState fromstate, FSMState tostate) {
		this.name = name;
		this.fromstate = fromstate;
		this.tostate = tostate;
		tostate.getInTransitions().put(name, this);
		fromstate.getOutTransitions().put(name, this);
	}

	/** 获得出发状态 */
	public FSMState getFromState() {
		return fromstate;
	}

	/** 设置出发状态 */
	public void setFromState(FSMState fromstate) {
		this.fromstate = fromstate;
	}

	/** 获得到达状态 */
	public FSMState getToState() {
		return tostate;
	}

	/** 设置到达状态 */
	public void setToState(FSMState tostate) {
		this.tostate = tostate;
	}

	/** 获得名称 */
	public String getName() {
		return name;
	}
	
	/** 设置名称 */
	public void setName(String name) {
		this.name=name;
	}

	/** 增加条件 */
	public void addCondition(FSMCondition condition) {
		conditions.add(condition);
		condition.fsm = fsm;
	}

	/** 获得条件列表 */
	public List<FSMCondition> getConditions() {
		return conditions;
	}
	
	/** 获得条件列表 */
	public void setConditions(List<FSMCondition> conditions) {
		this.conditions=conditions;
	}

	/** 解析xml */
	@Override
	public void loadXML(Node n) {
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}

	/** 对转换上的条件列表进行计算，返回是否满足所有条件 */
	public boolean evaluate(FSMMachineInstance fsm,FSMStateInstance state, VexNode vex) {
		boolean b=false;
		Iterator<FSMCondition> i = conditions.iterator();
		while (i.hasNext()) {
			FSMCondition condition = i.next();
			if (!condition.evaluate(fsm,state,vex)) {
				return false;
			}
		}
		//	调用关联动作
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
	
	/** 打印 */
	@Override
	public String toString() {
		return name;
	}
}
