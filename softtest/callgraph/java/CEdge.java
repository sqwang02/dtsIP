package softtest.callgraph.java;

/** 代表调用关系图中的边的类 */
public class CEdge extends CElement implements Comparable<CEdge> {
	/** 名称 */
	String name = null;

	/** 头结点 */
	CVexNode headnode = null;

	/** 尾结点点 */
	CVexNode tailnode = null;
	
	/** 用于比较的数字 */
	int snumber = 0;
	
	/** 以该名字创建一条边，此时头尾节点都为空，待设定 */
	public CEdge(String name) {
		this.name = name;
	}

	/** 以指定的名字，尾节点和头节点创建边 */
	public CEdge(String name, CVexNode tailnode, CVexNode headnode) {
		this.name = name;
		this.headnode = headnode;
		this.tailnode = tailnode;
		headnode.inedges.put(name, this);
		tailnode.outedges.put(name, this);
	}

	/** 访问者模式的accept方法 */
	@Override
	public void accept(CGraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 获得尾节点 */
	public CVexNode getTailNode(){
		return tailnode;
	}
	
	/** 获得头节点 */
	public CVexNode getHeadNode(){
		return headnode;
	}
	
	/** 获得名称 */
	public String getName(){
		return name;
	}
	
	/** 比较边的顺序，用于排序 */
	public int compareTo(CEdge e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
}