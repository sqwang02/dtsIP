package softtest.callgraph.java;

import java.util.*;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.symboltable.java.*;
import softtest.ast.java.*;
import softtest.symboltable.java.MethodNameDeclaration;

/** 调用关系图的顶点类 */
public class CVexNode extends CElement implements Comparable<CVexNode> {
	/** 名称 */
	String name;

	/** 入边集合 */
	Hashtable<String, CEdge> inedges = new Hashtable<String, CEdge>();

	/** 出边集合 */
	Hashtable<String, CEdge> outedges = new Hashtable<String, CEdge>();

	/** 访问标志 */
	boolean visited = false;

	/** 用于比较的数字 */
	int snumber = 0;
	
	/** 用于拓扑排序中用到的入度计算*/
	int indegree = 0;
	
	/** 对应的函数声明 */
	MethodNameDeclaration mnd = null;

	/** 以指定的名字创建调用关系图节点 */
	public CVexNode(String name,MethodNameDeclaration mnd) {
		this.name = name;
		this.mnd=mnd;
		mnd.setCallGraphVex(this);
	}
	
	/** 设置函数声明 */
	public void setMethodNameDeclaration(MethodNameDeclaration mnd){
		this.mnd=mnd;
	}
	
	/** 获得函数声明 */
	public MethodNameDeclaration getMethodNameDeclaration(){
		return this.mnd;
	}
	
	/**获得函数语法树节点*/
	public ASTMethodDeclaration getMethodDeclaration(){
		return (ASTMethodDeclaration)mnd.getMethodNameDeclaratorNode().jjtGetParent();
	}

	/** 控制流图访问者的accept */
	@Override
	public void accept(CGraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}
	
	/** 设置节点访问标志 */
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/** 获得节点访问标志 */
	public boolean getVisited() {
		return visited;
	}

	/** 获得节点名称 */
	public String getName() {
		return name;
	}

	/** 获得入边集合 */
	public Hashtable<String, CEdge> getInedges() {
		return inedges;
	}

	/** 获得出边集合 */
	public Hashtable<String, CEdge> getOutedges() {
		return outedges;
	}

	/** 比较区间的顺序，用于排序 */
	public int compareTo(CVexNode e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/** 检查一个节点是否是前驱 */
	public boolean isPreNode(CVexNode p){
		for(Enumeration e=inedges.elements();e.hasMoreElements();){
			CEdge edge=(CEdge)e.nextElement();
			if(p==edge.getTailNode()){
				return true;
			}
		}
		return false;
	}
}