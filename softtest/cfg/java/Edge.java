package softtest.cfg.java;

/** 代表控制流图中的边的类 */
public class Edge extends Element implements Comparable<Edge> {
	/** 名称 */
	String name = null;

	/** 头结点 */
	VexNode headnode = null;

	/** 尾结点点 */
	VexNode tailnode = null;
	
	/** 用于比较的数字 */
	int snumber = 0;
	
	/** 该分支是否矛盾标志，用于控制流图中的不可达路径 */
	boolean contradict=false;

	/** 以该名字创建一条边，此时头尾节点都为空，待设定 */
	public Edge(String name) {
		this.name = name;
	}

	/** 以指定的名字，尾节点和头节点创建边 */
	public Edge(String name, VexNode tailnode, VexNode headnode) {
		this.name = name;
		this.headnode = headnode;
		this.tailnode = tailnode;
		headnode.inedges.put(name, this);
		tailnode.outedges.put(name, this);
	}

	/** 访问者模式的accept方法 */
	@Override
	public void accept(GraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 获得尾节点 */
	public VexNode getTailNode(){
		return tailnode;
	}
	
	/** 获得头节点 */
	public VexNode getHeadNode(){
		return headnode;
	}
	
	/** 获得名称 */
	public String getName(){
		return name;
	}
	
	/** 获得分支是否矛盾标志 */
	public boolean getContradict(){
		return contradict;
	}
	
	/** 设置分支是否矛盾标志 */
	public void setContradict(boolean contradict){
		this.contradict=contradict;
	}
	
	/** 比较边的顺序，用于排序 */
	public int compareTo(Edge e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		return sb.toString();
	}
}
