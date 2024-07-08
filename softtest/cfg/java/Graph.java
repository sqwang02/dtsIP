package softtest.cfg.java;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;

import softtest.ast.java.*;

/** 代表控制流图的类 */
public class Graph extends Element {
	/** 路径总数(删除矛盾边后的) */
	private int pathcount = 0;
	
	/** 不是实际的计数，用于产生名字 */
	private int nodecount = 0;

	/** 不是实际的计数，用于产生名字 */
	private int edgecount = 0;

	/** 结点集合 */
	public Hashtable<String, VexNode> nodes = new Hashtable<String, VexNode>();

	/** 边集合 */
	public Hashtable<String, Edge> edges = new Hashtable<String, Edge>();

	/** 缺省参数构造函数 */
	public Graph() {

	}

	/** 增加一个节点，如果该节点已经存在，则抛出异常 */
	VexNode addVex(VexNode vex) {
		if (nodes.get(vex.name) != null) {
			throw new RuntimeException("The vexnode has already existed.");
		}
		nodes.put(vex.name, vex);
		vex.snumber = nodecount++;
		return vex;
	}

	/** 增加一个指定名称的节点，并设定该节点关联的抽象语法节点，最终的名称将为name+nodecount */
	VexNode addVex(String name, SimpleJavaNode treenode) {
		VexNode vex = new VexNode(name + nodecount, treenode);
		vex.setGraph(this);
		return addVex(vex);
	}

	/** 增加一个节点，并设定该节点关联的抽象语法节点 */
	VexNode addVex(SimpleJavaNode treenode) {
		String name = "" + nodecount;
		return addVex(name, treenode);
	}

	/** 增加一个指定尾、头节点的边，并设定名称 */
	Edge addEdge(VexNode tailnode, VexNode headnode, String name) {
		if (headnode == null || tailnode == null) {
			throw new RuntimeException("An edge's head or tail cannot be null.");
		}
		if (edges.get(name) != null) {
			throw new RuntimeException("The edge has already existed.");
		}
		if (nodes.get(headnode.name) != headnode || nodes.get(tailnode.name) != tailnode) {
			throw new RuntimeException("There is a contradiction.");
		}

		if (headnode.inedges.get(name) != null) {
			throw new RuntimeException("There is a contradiction.");
		}

		if (tailnode.outedges.get(name) != null) {
			throw new RuntimeException("There is a contradiction.");
		}

		Edge e = new Edge(name, tailnode, headnode);
		edges.put(name, e);
		e.snumber = edgecount++;

		return e;
	}

	/** 增加一个指定尾、头节点的边，并设定名称 */
	Edge addEdge(String tail, String head, String name) {
		VexNode tailnode = nodes.get(tail);
		VexNode headnode = nodes.get(head);
		return addEdge(tailnode, headnode, name);
	}

	/** 增加一个指定尾、头节点的边 */
	Edge addEdge(String tail, String head) {
		String name = "" + edgecount;
		return addEdge(tail, head, name);
	}

	/** 增加一个指定尾、头节点的边 */
	Edge addEdge(VexNode tailnode, VexNode headnode) {
		String name = "" + edgecount;
		return addEdge(tailnode, headnode, name);
	}

	/** 增加一个指定尾、头节点的边，并设定名称为name+edgecount */
	Edge addEdge(String name, VexNode tailnode, VexNode headnode) {
		name = name + edgecount;
		return addEdge(tailnode, headnode, name);
	}

	/** 增加一个指定尾、头节点的边，并设定其可能的真假分支情况，真分支名称以T_开头，假分支以F_开头,eflag为异常边标记 */
	Edge addEdgeWithFlag(VexNode tailnode, VexNode headnode,boolean eflag) {
		String name = "";
		Edge edge = null;
		if((tailnode.truetag||tailnode.falsetag)&&eflag){
			//catch语句的分支可能存在冲突
			//throw new RuntimeException("illeagal arguement");
		}
		if (tailnode.truetag) {
			name = "T_";
			tailnode.truetag = false;
		} else if (tailnode.falsetag) {
			name = "F_";
			tailnode.falsetag = false;
		}else if(eflag){
			name = "E_";
		}
		edge = addEdge(name, tailnode, headnode);
		return edge;
	}	

	/** 删除指定的边，如果找不到该边或者该边不是图中的边则抛出异常 */
	void removeEdge(Edge e) {
		if (edges.get(e.name) != e || e == null) {
			throw new RuntimeException("Cannot find the edge.");
		}
		if (e.headnode == null || e.tailnode == null) {
			throw new RuntimeException("There is a contradiction.");
		}
		if (e.headnode.inedges.get(e.name) != e || e.tailnode.outedges.get(e.name) != e) {
			throw new RuntimeException("There is a contradiction.");
		}

		e.headnode.inedges.remove(e.name);
		e.tailnode.outedges.remove(e.name);
		edges.remove(e.name);

	}

	/** 删除指定的边 */
	void removeEdge(String name) {
		Edge e = edges.get(name);
		removeEdge(e);
	}

	/** 删除指定节点的所有入边 */
	void removeInedges(VexNode vex) {
		LinkedList<Edge> temp = new LinkedList<Edge>();
		temp.clear();
		for (Enumeration<Edge> e = vex.inedges.elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<Edge> i = temp.listIterator();
		while (i.hasNext()) {
			Edge edge = i.next();
			removeEdge(edge);
		}
	}

	/** 删除指定节点的所有出边 */
	void removeOutedges(VexNode vex) {
		LinkedList<Edge> temp = new LinkedList<Edge>();
		temp.clear();
		for (Enumeration<Edge> e = vex.outedges.elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<Edge> i = temp.listIterator();
		while (i.hasNext()) {
			Edge edge = i.next();
			removeEdge(edge);
		}
	}

	/** 删除指定节点及其关联的边 */
	void removeVex(VexNode vex) {
		if (nodes.get(vex.name) != vex || vex == null) {
			throw new RuntimeException("Cannot find the vexnode.");
		}
		removeInedges(vex);
		removeOutedges(vex);
		nodes.remove(vex.name);
	}

	/** 删除指定节点的所有入边 */
	void removeVex(String name) {
		VexNode vex = nodes.get(name);
		removeVex(vex);
	}

	/** 控制流图访问者的accept方法 */
	@Override
	public void accept(GraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 获得指定节点的一个没有访问的相邻节点，可能返回null,节点顺序由自然顺序决定 */
	public VexNode getAdjUnvisitedVertex(VexNode v) {
		if (v.outedges.size() <= 0) {
			return null;
		}
		List<VexNode> list = new ArrayList<VexNode>();
		for (Enumeration<Edge> e = v.outedges.elements(); e.hasMoreElements();) {
			Edge edge = e.nextElement();
			if (!edge.headnode.getVisited()) {
				list.add(edge.headnode);
			}
		}
		Collections.sort(list);
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/** 深度优先遍历，visitor为对节点的访问者，data为数据 */
	public void dfs(GraphVisitor visitor, Object data) {
		// 找到控制流图入口开始
		VexNode first = null;
		Stack<VexNode> stack = new Stack<VexNode>();
		for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
			VexNode n = (VexNode) e.nextElement();
			if (!n.getVisited() && n.inedges.size() == 0) {
				first = n;
				break;
			}
		}
		if (first == null) {
			throw new RuntimeException("控制流图入口错误");
		}
		first.accept(visitor, data);
		first.setVisited(true);
		stack.push(first);
		while (!stack.isEmpty()) {
			VexNode next = getAdjUnvisitedVertex(stack.peek());
			if (next == null) {
				stack.pop();
			} else {
				next.accept(visitor, data);
				next.setVisited(true);
				stack.push(next);
			}
		}
		// 处理那些控制流图入口到达不了的节点，不掉用访问者
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode n = e.nextElement();
			if (!n.getVisited()) {
				// first.accept(visitor, data);
				first.setVisited(true);
				stack.push(first);
				while (!stack.isEmpty()) {
					VexNode next = getAdjUnvisitedVertex(stack.peek());
					if (next == null) {
						stack.pop();
					} else {
						// next.accept(visitor, data);
						next.setVisited(true);
						stack.push(next);
					}
				}
			}
		}

		// 将访问标志重新设置回false
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode n = e.nextElement();
			n.setVisited(false);
		}
	}

	/** 节点顺序遍历 */
	public void numberOrderVisit(GraphVisitor visitor, Object data) {
		List<VexNode> list = new ArrayList<VexNode>();
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode vex = e.nextElement();
			list.add(vex);
		}
		Collections.sort(list);

		Iterator<VexNode> i = list.iterator();
		while (i.hasNext()) {
			i.next().accept(visitor, data);
		}
	}
	
	/** 清除所有节点的访问标志 */
	public void clearVisited(){
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode n = e.nextElement();
			n.setVisited(false);
		}
	}

	/** 清除所有边上的矛盾标志 */
	public void clearEdgeContradict() {
		for (Enumeration<Edge> e = edges.elements(); e.hasMoreElements();) {
			Edge n = e.nextElement();
			n.setContradict(false);
		}
	}

	/** 清除所有节点上的矛盾标志 */
	public void clearVexNodeContradict() {
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode n = e.nextElement();
			n.setContradict(false);
		}
	}
	
	/**
	 * @param fromvex 出发节点
	 * @param tovex	目的节点
	 * @param tempe 不能经过的边
	 * @return 如果可达则返回true，否则返回false
	 */
	public static boolean checkVexReachable(VexNode fromvex,VexNode tovex,Edge tempe){
		if(fromvex==null||tovex==null||tempe==null){
			return false;
		}
		if(fromvex==tovex){
			return true;
		}
		Stack<VexNode> stack = new Stack<VexNode>();
		HashSet<VexNode> table=new HashSet<VexNode>();
		stack.push(fromvex);
		table.add(fromvex);
		while (!stack.isEmpty()) {
			VexNode v=stack.pop();
			for (Enumeration<Edge> e = v.outedges.elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				if(edge==tempe){
					continue;
				}
				VexNode tempnode=edge.headnode;
				if (!table.contains(tempnode)) {
					if(tempnode==tovex){
						return true;
					}
					table.add(tempnode);
					stack.push(tempnode);
				}
			}
		}
		return false;
	}
	
	/**
	 * @param fromvex 出发节点
	 * @param tovex	目的节点
	 * @return 一条路径
	 */
	public static List<VexNode> findAPath(VexNode fromvex,VexNode tovex){
		List<VexNode> ret=new ArrayList<VexNode>();
		if(fromvex==null||tovex==null){
			return ret;
		}
		if(fromvex==tovex){
			ret.add(fromvex);
			return ret;
		}
		Stack<VexNode> stack = new Stack<VexNode>();
		HashSet<VexNode> table=new HashSet<VexNode>();
		stack.push(fromvex);
		table.add(fromvex);
		
		while (!stack.isEmpty()) {
			VexNode current=stack.peek();
			VexNode next=null;
			for (Enumeration<Edge> e = current.outedges.elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				VexNode tempnode=edge.headnode;
				if(!table.contains(tempnode)){
					next=tempnode;
				}
			}
			if (next == null) {
				stack.pop();
			} else {
				if(next==tovex){
					for(VexNode v:stack){
						ret.add(v);
					}
					ret.add(tovex);
					return ret;
				}else{
					table.add(next);
					stack.push(next);
				}
			}
		}		
		
		
		return ret;
	}
	
	/**
	 * @return 返回入口节点
	 */
	public VexNode getEntryNode(){
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode vex = e.nextElement();
			if(vex.getName().startsWith("func_head_")){
				return vex;
			}
		}
		throw new RuntimeException("Cannot find entry node.");
	}
	
	/**
	 * @return 返回出口节点
	 */
	public VexNode getExitNode(){
		for (Enumeration<VexNode> e = nodes.elements(); e.hasMoreElements();) {
			VexNode vex = e.nextElement();
			if(vex.getName().startsWith("func_out_")){
				return vex;
			}
		}
		throw new RuntimeException("Cannot find exit node.");
	}

	/**
	 * @return the pathcount
	 */
	public int getPathcount() {
		return pathcount;
	}

	/**
	 * @param pathcount the pathcount to set
	 */
	public void setPathcount(int pathcount) {
		this.pathcount = pathcount;
	}
}