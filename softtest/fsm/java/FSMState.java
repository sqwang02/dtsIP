package softtest.fsm.java;

import java.util.*;
import softtest.cfg.java.*;
import org.w3c.dom.*;

/** ����ģʽ״̬��״̬ */
public class FSMState extends FSMElement implements Comparable<FSMState>{
	/** ���� */
	private String name;
	
	/** �Ƿ�Ϊ��ʼ״̬�ı�־ */
	private boolean isstart = false;
	
	/** �Ƿ�Ϊ�ս�״̬�ı�־ */
	private boolean isfinal = false;
	
	/** �Ƿ�Ϊ����״̬�ı�־ */
	private boolean iserror = false;
	
	/** ��߼��� */
	private Hashtable<String, FSMTransition> intrans = new Hashtable<String, FSMTransition>();

	/** ���߼��� */
	private Hashtable<String, FSMTransition> outtrans = new Hashtable<String, FSMTransition>();

	/** ���ʱ�־ */
	private boolean visited = false;
	
	/** ��ţ�Ҳ���ڱȽϵ����� */
	private int snumber = 0;
	
	/** ״̬�������ߵ�accept */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}
	
	/** �Ƚ������˳���������� */
	public int compareTo(FSMState e) {
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
	
	/** ��ָ�������ִ���״̬ */
	public FSMState(String name) {
		this.name = name;
	}
	
	/** ����״̬���ʱ�־ */
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/** ���״̬���ʱ�־ */
	public boolean getVisited() {
		return visited;
	}
	
	/** ��ýڵ�����*/
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	/** ����Ƿ�Ϊ��ʼ״̬��־ */
	public boolean isStart(){
		return isstart;
	}
	
	/** �����Ƿ�Ϊ��ʼ״̬��־ */
	public void setStart(boolean isstart){
		this.isstart=isstart;
	}
	
	/** ����Ƿ�Ϊ�ս�״̬��־ */
	public boolean isFinal(){
		return isfinal;
	}
	
	/** �����Ƿ�Ϊ�ս�״̬��־ */
	public void setFinal(boolean isfinal){
		this.isfinal=isfinal;
	}
	
	/** ����Ƿ�Ϊ����״̬��־ */
	public boolean isError(){
		return iserror;
	}
	
	/** �����Ƿ�Ϊ����״̬��־ */
	public void setError(boolean iserror){
		this.iserror=iserror;
	}
	
	/** ��ý���״̬����*/
	public Hashtable<String,FSMTransition> getInTransitions(){
		return intrans;
	}
	
	/** ���ý���״̬����*/
	public void setInTransitions(Hashtable<String,FSMTransition> intrans){
		this.intrans= intrans;
	}
	
	/** ��ó���״̬����*/
	public Hashtable<String,FSMTransition> getOutTransitions(){
		return outtrans;
	}
	
	/** ���ó���״̬����*/
	public void setOutTransitions(Hashtable<String,FSMTransition> outtrans){
		this.outtrans=outtrans;
	}
	
	@Override
	public void loadXML(Node n){
		if (n.getAttributes().getNamedItem("isStartState") != null && n.getAttributes().getNamedItem("isStartState").getNodeValue().equals("true")) {
			isstart=true;
		} else {
			isstart=false;
		}

		if (n.getAttributes().getNamedItem("isErrorState") != null && n.getAttributes().getNamedItem("isErrorState").getNodeValue().equals("true")) {
			iserror=true;
		} else {
			iserror=false;
		}

		if (n.getAttributes().getNamedItem("isFinalState") != null && n.getAttributes().getNamedItem("isFinalState").getNodeValue().equals("true")) {
			isfinal=true;
		} else {
			isfinal=false;
		}
		//����ÿ����������غ���
		if(fsm!=null&&fsm.getRelatedClass()!=null){
			loadAction(n,fsm.getRelatedClass());
		}
	}
	
	/** ��ӡ */
	@Override
	public String toString() {
		return name;
	}
	
	public boolean invokeRelatedMethod(VexNode n,FSMMachineInstance fsm){
		boolean r=true;
		if(getRelatedMethod()!=null){
			try {
				Object[] args = new Object[2];
				args[0] = n;
				args[1] = fsm;
				r=(Boolean) getRelatedMethod().invoke(null, args);
			} catch (Exception ex) {
				if(softtest.config.java.Config.DEBUG){
					ex.printStackTrace();
				}
				throw new RuntimeException("Can't invoke state method.",ex);
			}
		}
		return r;
	}
}