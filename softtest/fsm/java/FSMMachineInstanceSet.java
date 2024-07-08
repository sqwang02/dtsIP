package softtest.fsm.java;

import java.util.*;

import softtest.cfg.java.*;
import softtest.IntervalAnalysis.java.*;

/** ״̬��ʵ������ */
public class FSMMachineInstanceSet {
	/** �洢״̬��ʵ����hash�� */
	private Hashtable<FSMMachineInstance, FSMMachineInstance> table = new Hashtable<FSMMachineInstance, FSMMachineInstance>();

	/** ���캯�� */
	public FSMMachineInstanceSet(){
		
	}
	
	/** �������캯�� */
	public FSMMachineInstanceSet(FSMMachineInstanceSet set){
		for(Enumeration<FSMMachineInstance> e = set.table.elements();e.hasMoreElements();){
			FSMMachineInstance newinstance = new FSMMachineInstance(e.nextElement());
			table.put(newinstance, newinstance);
		}		
	}
	
	/** ���״̬��ʵ�� */
	public void clear() {
		table.clear();
	}

	/** ���״̬��ʵ��hash�� */
	public Hashtable<FSMMachineInstance, FSMMachineInstance> getTable() {
		return table;
	}

	/** ����״̬��ʵ��hash�� */
	public void setTable(Hashtable<FSMMachineInstance, FSMMachineInstance> table) {
		this.table = table;
	}

	/** ��״̬������set�ϲ����� */
	public void mergeFSMMachineInstances(FSMMachineInstanceSet set) {
		Hashtable<FSMMachineInstance, FSMMachineInstance> addtable = set.table;
		for (Enumeration<FSMMachineInstance> e = addtable.elements(); e.hasMoreElements();) {
			FSMMachineInstance addfsmin = e.nextElement();
			FSMMachineInstance fsmin = table.get(addfsmin);
			if (fsmin != null) {
				// ��״̬���ڼ������Ѿ����ڣ��ϲ�״̬
				for (Enumeration<FSMStateInstance> f = addfsmin.getStates().getTable().elements(); f.hasMoreElements();) {
					fsmin.addStateInstance(f.nextElement());
				}
			} else {
				// ��״̬���ڼ����в����ڣ�����
				fsmin= new FSMMachineInstance(addfsmin);
				table.put(fsmin, fsmin);
			}
		}
	}
	
	public void mergFSMMachineInstancesWithoutConditon(FSMMachineInstanceSet set) {
		Hashtable<FSMMachineInstance, FSMMachineInstance> addtable = set.table;
		for (Enumeration<FSMMachineInstance> e = addtable.elements(); e.hasMoreElements();) {
			FSMMachineInstance addfsmin = e.nextElement();
			FSMMachineInstance fsmin = table.get(addfsmin);
			if (fsmin != null) {
				// ��״̬���ڼ������Ѿ����ڣ��ϲ�״̬
				for (Enumeration<FSMStateInstance> f = addfsmin.getStates().getTable().elements(); f.hasMoreElements();) {
					fsmin.addStateInstanceWithoutConditon(f.nextElement());
				}
			} else {
				// ��״̬���ڼ����в����ڣ�����
				fsmin= new FSMMachineInstance(addfsmin);
				table.put(fsmin, fsmin);
			}
		}
	}
	

	/** ��״̬��ʵ������list������� */
	public void addFSMMachineInstances(List list) {
		Iterator i = list.iterator();
		while (i.hasNext()) {
			FSMMachineInstance in = (FSMMachineInstance) i.next();
			if (!table.contains(in)) {
				table.put(in, in);
			}
		}
	}

	/** ����״̬����Լ�� */
	public void addDomainSet(DomainSet set) {
		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			FSMMachineInstance f = e.nextElement();
			f.addDomainSet(set);
		}
	}

	/** ��ӡ */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean b = false;
		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			FSMMachineInstance f = e.nextElement();
			sb.append(f + "\\n");
			b = true;
		}
		if (b) {
			return sb.substring(0, sb.length() - 2);
		} else {
			return sb.toString();
		}
	}
	
	/** ���ݵ�ǰ�������ڵ㣬����״̬�����ϵ�����״̬���� */
	public void calSwitch(VexNode n,VexNode pre){
		List<FSMMachineInstance> todelete=new ArrayList<FSMMachineInstance>(); 
		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			FSMMachineInstance f = e.nextElement();
			f.calSwitch(n,pre);	
			if(f.getStates().isEmpty()){
				todelete.add(f);
			}
		}
		// ɾ����Щ�յ�״̬��
		Iterator<FSMMachineInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}			
	}
	
	/** ����ǰ���������жϽڵ���Ƿ�Ϊ���֧��־������״̬�����ϵ�����״̬���� */
	public void calCondition(VexNode pre,boolean istruebranch){
		List<FSMMachineInstance> todelete=new ArrayList<FSMMachineInstance>(); 
		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			FSMMachineInstance f = e.nextElement();
			f.calCondition(pre,istruebranch);	
			if(f.getStates().isEmpty()){
				todelete.add(f);
			}
		}
		// ɾ����Щ�յ�״̬��		
		Iterator<FSMMachineInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}		
	}
	
	/** ���ݵ�ǰ�������ڵ㣬����״̬�������е�����״̬���� */
	public void calDomainSet(VexNode vex){
		List<FSMMachineInstance> todelete=new ArrayList<FSMMachineInstance>(); 
		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			FSMMachineInstance f = e.nextElement();
			f.calDomainSet(vex);	
			if(f.getStates().isEmpty()){
				todelete.add(f);
			}
		}
		// ɾ����Щ�յ�״̬��		
		Iterator<FSMMachineInstance> i = todelete.iterator();
		while(i.hasNext()){
			table.remove(i.next());
		}
	}
}
