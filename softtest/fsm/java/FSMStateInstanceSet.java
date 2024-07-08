package softtest.fsm.java;

import java.util.*;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;

/** ״̬ʵ������ */
public class FSMStateInstanceSet {
	/** ״̬ʵ��hash�� */
	private Hashtable<FSMStateInstance, FSMStateInstance> table = new Hashtable<FSMStateInstance, FSMStateInstance>();

	/** ���캯�� */
	public FSMStateInstanceSet(){
		
	}
	
	/** �������캯�� */
	public FSMStateInstanceSet(FSMStateInstanceSet set){
		for(Enumeration<FSMStateInstance> e = set.table.elements();e.hasMoreElements();){
			FSMStateInstance newinstance = new FSMStateInstance(e.nextElement());
			table.put(newinstance, newinstance);
		}
	}
	
	/** ����״̬ʵ�� */
	public void addStateInstance(FSMStateInstance stateinstance) {
		if (stateinstance.getState().isFinal()) {
			// ������ս�״̬�򲻼���
			return;
		}
		FSMStateInstance si = table.get(stateinstance);
		if (si != null) {
			// ��״̬ʵ���Ѿ�������,Ӧ�ÿ��������ĺϲ�
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
			// ֱ�Ӽ���
			si= new FSMStateInstance(stateinstance);
			table.put(si, si);
		}

	}
	
	public void addStateInstanceWithoutConditon(FSMStateInstance stateinstance) {
		if (stateinstance.getState().isFinal()) {
			// ������ս�״̬�򲻼���
			return;
		}
		FSMStateInstance si = table.get(stateinstance);
		if (si != null) {
			// ��״̬ʵ���Ѿ�������,�����κβ���
			if (si.getSwitchPoints() == null) {
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
			else if (stateinstance.getSwitchPoints() != null && si.getSwitchPoints().size() > stateinstance.getSwitchPoints().size()){
				si.setSwitchPoints(stateinstance.getSwitchPoints());
			}
		} else {
			// ֱ�Ӽ���
			si= new FSMStateInstance(stateinstance);
			table.put(si, si);
		}

	}

	
	/** �Ƴ�״̬ʵ�� */
	public void removeStateInstance(FSMStateInstance stateinstance) {
		table.remove(stateinstance);
	}

	/** ���״̬ʵ����ϣ�� */
	public Hashtable<FSMStateInstance, FSMStateInstance> getTable() {
		return table;
	}

	/** ����״̬Լ������ */
	public void addDomainSet(DomainSet set) {
		List<FSMStateInstance> todelete = new ArrayList<FSMStateInstance>();
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.addDomainSet(set);
			if (s.getDomainSet().isContradict()) {
				// �����ܵ�״̬
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

	/** ��ӡ */
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
	
	/** ����caselabel�ڵ��switch�ڵ㣬����״̬���ϵ�����״̬���� */
	public void calSwitch(VexNode n,VexNode pre){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calSwitch(n,pre);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// ɾ����Щì��״̬		
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}	
	}
	
	/** ����ǰ���������жϽڵ���Ƿ�Ϊ���֧��־������״̬���ϵ�����״̬���� */
	public void calCondition(VexNode pre,boolean istruebranch){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calCondition(pre,istruebranch);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// ɾ����Щì��״̬
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}
	}
	
	/** ���ݵ�ǰ�������ڵ㣬����״̬���ϵ�����״̬���� */
	public void calDomainSet(VexNode vex){
		List<FSMStateInstance> todelete=new ArrayList<FSMStateInstance>(); 
		for (Enumeration<FSMStateInstance> e = table.elements(); e.hasMoreElements();) {
			FSMStateInstance s = e.nextElement();
			s.calDomainSet(vex);
			if(s.getDomainSet().isContradict()){
				todelete.add(s);
			}
		}
		// ɾ����Щì��״̬
		Iterator<FSMStateInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}
	}
	
	/** �ж�״̬�����Ƿ�Ϊ�ռ� */
	public boolean isEmpty(){
		return table.isEmpty();
	}
}
