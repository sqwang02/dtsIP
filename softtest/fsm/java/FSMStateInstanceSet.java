package softtest.fsm.java;

import java.util.*;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;

/** 状态实例集合 */
public class FSMStateInstanceSet {
	/** 状态实例hash表 */
	private Hashtable<FSMStateInstance, FSMStateInstance> table = new Hashtable<FSMStateInstance, FSMStateInstance>();

	/** 构造函数 */
	public FSMStateInstanceSet(){
		
	}
	
	/** 拷贝构造函数 */
	public FSMStateInstanceSet(FSMStateInstanceSet set){
		for(Enumeration<FSMStateInstance> e = set.table.elements();e.hasMoreElements();){
			FSMStateInstance newinstance = new FSMStateInstance(e.nextElement());
			table.put(newinstance, newinstance);
		}
	}
	
	/** 加入状态实例 */
	public void addStateInstance(FSMStateInstance stateinstance) {
		if (stateinstance.getState().isFinal()) {
			// 如果是终结状态则不加入
			return;
		}
		FSMStateInstance si = table.get(stateinstance);
		if (si != null) {
			// 该状态实例已经存在了,应该考虑条件的合并
			DomainSet ds=DomainSet.union(si.getDomainSet(), stateinstance.getDomainSet());
			//ds.removeRedundantDomain();
			si.setDomainSet(ds);
			if (si.getSwitchPoints() == null) {
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
			else if (stateinstance.getSwitchPoints() != null && si.getSwitchPoints().size() > stateinstance.getSwitchPoints().size()){
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
		} else {
			// 直接加入
			si= new FSMStateInstance(stateinstance);
			table.put(si, si);
		}

	}
	
	public void addStateInstanceWithoutConditon(FSMStateInstance stateinstance) {
		if (stateinstance.getState().isFinal()) {
			// 如果是终结状态则不加入
			return;
		}
		FSMStateInstance si = table.get(stateinstance);
		if (si != null) {
			// 该状态实例已经存在了,不做任何操作
			if (si.getSwitchPoints() == null) {
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
			else if (stateinstance.getSwitchPoints() != null && si.getSwitchPoints().size() > stateinstance.getSwitchPoints().size()){
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
		} else {
			// 直接加入
			si= new FSMStateInstance(stateinstance);
			table.put(si, si);
		}

	}

	
	/** 移除状态实例 */
	public void removeStateInstance(FSMStateInstance stateinstance) {
		table.remove(stateinstance);
	}

	/** 获得状态实例哈希表 */
	public Hashtable<FSMStateInstance, FSMStateInstance> getTable() {
		return table;
	}

	/** 增加状态约束条件 */
	public void addDomainSet(DomainSet set) {
		List<FSMStateInstance> todelete = new ArrayList<FSMStateInstance>();
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.addDomainSet(set);
			if (s.getDomainSet().isContradict()) {
				// 不可能的状态
				if(softtest.config.java.Config.DEBUG){
					System.out.println("impossible state" + s);
				}
				todelete.add(s);
			}
		}

		Iterator<FSMStateInstance> i = todelete.iterator();
		while (i.hasNext()) {
			table.remove(i.next());
		}
	}

	/** 打印 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean b = false;
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			sb.append(s + ",");
			b = true;
		}
		if (b) {
			sb.setCharAt(sb.length()-1 , '}');
		} else {
			sb.append("}");
		}
		return sb.toString();
	}
	
	/** 根据caselabel节点和switch节点，计算状态集合的所有状态条件 */
	public void calSwitch(VexNode n,VexNode pre){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calSwitch(n,pre);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// 删除那些矛盾状态		
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}	
	}
	
	/** 根据前趋控制流判断节点和是否为真分支标志，计算状态集合的所有状态条件 */
	public void calCondition(VexNode pre,boolean istruebranch){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calCondition(pre,istruebranch);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// 删除那些矛盾状态
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}
	}
	
	/** 根据当前控制流节点，计算状态集合的所有状态条件 */
	public void calDomainSet(VexNode vex){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calDomainSet(vex);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// 删除那些矛盾状态
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}
	}
	
	/** 判断状态集合是否为空集 */
	public boolean isEmpty(){
		return table.isEmpty();
	}
}
