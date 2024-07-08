package softtest.fsm.java;

import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.ast.java.*;
//状态机的相关计算
public class FSMRelatedCalculation {
	/** 相关的语法树标记节点 */
	private SimpleJavaNode tagtreenode;
	
	/** 如果指向的语法树标记节点相等，则被认为相等 */
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

	/** 用于hash表的key，需要保证值相等则hashcode相等 */
	@Override
	public int hashCode() {
		return (tagtreenode!=null)?tagtreenode.hashCode():0;
	}
	
	/** 计算数据流方程中的IN */
	public void calculateIN(FSMMachineInstance fsmin,VexNode n,Object data){	
	}
	
	/** 计算数据流方程中的OUT */
	public void calculateOUT(FSMMachineInstance fsmin,VexNode n,Object data){
	}
	
	/** 拷贝构造函数 */
	public FSMRelatedCalculation(FSMRelatedCalculation o){
		tagtreenode = o.tagtreenode;
	}
	
	/** 拷贝 */
	public FSMRelatedCalculation copy(){
		FSMRelatedCalculation r = new FSMRelatedCalculation(this);
		return r;
	}
	
	/** 构造函数 */
	public FSMRelatedCalculation(SimpleJavaNode tagtreenode){
		this.tagtreenode=tagtreenode;
	}
	
	/** 构造函数 */
	public FSMRelatedCalculation(){
		this.tagtreenode=null;
	}
	
	/** 设置标记节点 */
	public void setTagTreeNode(SimpleJavaNode tagtreenode){
		this.tagtreenode=tagtreenode;
	}
	
	/** 获得记节点 */
	public SimpleJavaNode getTagTreeNode(){
		return tagtreenode;
	}
}
