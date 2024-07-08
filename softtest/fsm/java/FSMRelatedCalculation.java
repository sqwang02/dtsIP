package softtest.fsm.java;

import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.ast.java.*;
//״̬������ؼ���
public class FSMRelatedCalculation {
	/** ��ص��﷨����ǽڵ� */
	private SimpleJavaNode tagtreenode;
	
	/** ���ָ����﷨����ǽڵ���ȣ�����Ϊ��� */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof FSMRelatedCalculation)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		FSMRelatedCalculation t=(FSMRelatedCalculation)o;
		if(tagtreenode == t.tagtreenode){
			return true;
		}
		return false;
	}

	/** ����hash���key����Ҫ��ֵ֤�����hashcode��� */
	@Override
	public int hashCode() {
		return (tagtreenode!=null)?tagtreenode.hashCode():0;
	}
	
	/** ���������������е�IN */
	public void calculateIN(FSMMachineInstance fsmin,VexNode n,Object data){	
	}
	
	/** ���������������е�OUT */
	public void calculateOUT(FSMMachineInstance fsmin,VexNode n,Object data){
	}
	
	/** �������캯�� */
	public FSMRelatedCalculation(FSMRelatedCalculation o){
		tagtreenode = o.tagtreenode;
	}
	
	/** ���� */
	public FSMRelatedCalculation copy(){
		FSMRelatedCalculation r = new FSMRelatedCalculation(this);
		return r;
	}
	
	/** ���캯�� */
	public FSMRelatedCalculation(SimpleJavaNode tagtreenode){
		this.tagtreenode=tagtreenode;
	}
	
	/** ���캯�� */
	public FSMRelatedCalculation(){
		this.tagtreenode=null;
	}
	
	/** ���ñ�ǽڵ� */
	public void setTagTreeNode(SimpleJavaNode tagtreenode){
		this.tagtreenode=tagtreenode;
	}
	
	/** ��üǽڵ� */
	public SimpleJavaNode getTagTreeNode(){
		return tagtreenode;
	}
}
