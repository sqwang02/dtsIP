package softtest.callgraph.java;

import java.util.*;

import softtest.symboltable.java.MethodNameDeclaration;

import softtest.symboltable.java.*;

/** 代表调用关系图的类 */
public class CGraph extends CElement {
	/** 不是实际的计数，用于产生名字 */
	private int nodecount = 0;

	/** 不是实际的计数，用于产生名字 */
	private int edgecount = 0;

	/** 结点集合 */
	public Hashtable<String, CVexNode> nodes = new Hashtable<String, CVexNode>();

	/** 边集合 */
	public Hashtable<String, CEdge> edges = new Hashtable<String, CEdge>();

	/** 缺省参数构造函数 */
	public CGraph() {

	}

	/** 增加一个节点，如果该节点已经存在，则抛出异常 */
	public CVexNode addVex(CVexNode vex) {
		if (nodes.get(vex.name) != null) {
			throw new RuntimeException("The vexnode has already existed.");
		}
		nodes.put(vex.name, vex);
		vex.snumber = nodecount++;
		return vex;
	}

	/** 增加一个指定名称的节点，并设定该节点关联的函数声明，最终的名称将为name+nodecount */
	public CVexNode addVex(String name, MethodNameDeclaration mnd) {
		CVexNode vex = new CVexNode(name + nodecount, mnd);
		return addVex(vex);
	}

	/** 增加一个节点 并设定该节点关联的函数声明 */
	public CVexNode addVex(MethodNameDeclaration mnd) {
		String name = "" + nodecount;
		return addVex(name, mnd);
	}

	/** 增加一个指定尾、头节点的边，并设定名称 */
	public CEdge addEdge(CVexNode tailnode, CVexNode headnode, String name) {
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

		CEdge e = new CEdge(name, tailnode, headnode);
		edges.put(name, e);
		e.snumber = edgecount++;

		return e;
	}

	/** 增加一个指定尾、头节点的边，并设定名称 */
	public CEdge addEdge(String tail, String head, String name) {
		CVexNode tailnode = nodes.get(tail);
		CVexNode headnode = nodes.get(head);
		return addEdge(tailnode, headnode, name);
	}

	/** 增加一个指定尾、头节点的边 */
	public CEdge addEdge(String tail, String head) {
		String name = "" + edgecount;
		return addEdge(tail, head, name);
	}

	/** 增加一个指定尾、头节点的边 */
	public CEdge addEdge(CVexNode tailnode, CVexNode headnode) {
		String name = "" + edgecount;
		return addEdge(tailnode, headnode, name);
	}

	/** 增加一个指定尾、头节点的边，并设定名称为name+edgecount */
	public CEdge addEdge(String name, CVexNode tailnode, CVexNode headnode) {
		name = name + edgecount;
		return addEdge(tailnode, headnode, name);
	}

	/** 删除指定的边，如果找不到该边或者该边不是图中的边则抛出异常 */
	public void removeEdge(CEdge e) {
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
	public void removeEdge(String name) {
		CEdge e = edges.get(name);
		removeEdge(e);
	}

	/** 删除指定节点的所有入边 */
	public void removeInedges(CVexNode vex) {
		LinkedList<CEdge> temp = new LinkedList<CEdge>();
		temp.clear();
		for (Enumeration<CEdge> e = vex.inedges.elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<CEdge> i = temp.listIterator();
		while (i.hasNext()) {
			CEdge edge = i.next();
			removeEdge(edge);
		}
	}

	/** 删除指定节点的所有出边 */
	public void removeOutedges(CVexNode vex) {
		LinkedList<CEdge> temp = new LinkedList<CEdge>();
		temp.clear();
		for (Enumeration<CEdge> e = vex.outedges.elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<CEdge> i = temp.listIterator();
		while (i.hasNext()) {
			CEdge edge = i.next();
			removeEdge(edge);
		}
	}

	/** 删除指定节点及其关联的边 */
	public void removeVex(CVexNode vex) {
		if (nodes.get(vex.name) != vex || vex == null) {
			throw new RuntimeException("Cannot find the vexnode.");
		}
		removeInedges(vex);
		removeOutedges(vex);
		nodes.remove(vex.name);
	}

	/** 删除指定节点的所有入边 */
	public void removeVex(String name) {
		CVexNode vex = nodes.get(name);
		removeVex(vex);
	}

	/** 控制流图访问者的accept方法 */
	@Override
	public void accept(CGraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 获得指定节点的一个没有访问的相邻节点，可能返回null,节点顺序由自然顺序决定 */
	private CVexNode getAdjUnvisitedVertex(CVexNode v) {
		if (v.outedges.size() <= 0) {
			return null;
		}
		List<CVexNode> list = new ArrayList<CVexNode>();
		for (Enumeration<CEdge> e = v.outedges.elements(); e.hasMoreElements();) {
			CEdge edge = e.nextElement();
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
	public void dfs(CGraphVisitor visitor, Object data) {
		// 找到图入口开始
		CVexNode first = null;
		Stack<CVexNode> stack = new Stack<CVexNode>();
		for (Enumeration e = nodes.elements(); e.hasMoreElements();) {
			CVexNode n = (CVexNode) e.nextElement();
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
			CVexNode next = getAdjUnvisitedVertex(stack.peek());
			if (next == null) {
				stack.pop();
			} else {
				next.accept(visitor, data);
				next.setVisited(true);
				stack.push(next);
			}
		}
		// 处理那些图入口到达不了的节点，不掉用访问者
		for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
			CVexNode n = e.nextElement();
			if (!n.getVisited()) {
				// first.accept(visitor, data);
				first.setVisited(true);
				stack.push(first);
				while (!stack.isEmpty()) {
					CVexNode next = getAdjUnvisitedVertex(stack.peek());
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
		for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
			CVexNode n = e.nextElement();
			n.setVisited(false);
		}
	}

	/** 节点顺序遍历 */
	public void numberOrderVisit(CGraphVisitor visitor, Object data) {
		List<CVexNode> list = new ArrayList<CVexNode>();
		for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
			CVexNode vex = e.nextElement();
			list.add(vex);
		}
		Collections.sort(list);

		Iterator<CVexNode> i = list.iterator();
		while (i.hasNext()) {
			i.next().accept(visitor, data);
		}
	}

	/** 清除所有节点的访问标志 */
	public void clearVisited() {
		for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
			CVexNode n = e.nextElement();
			n.setVisited(false);
		}
	}

	/** 得到所有节点的拓扑排序列表，对于存在环路的情况，选取入度最小的节点，删除入边破坏环路 */
	public List<CVexNode> getTopologicalOrderList() {
		Stack<CVexNode> stack = new Stack<CVexNode>();
		ArrayList<CVexNode> list = new ArrayList<CVexNode>();
		// 初始化入度
		for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
			CVexNode n = e.nextElement();
			n.indegree = 0;
			for (Enumeration<CEdge> e1 = n.getInedges().elements(); e1.hasMoreElements();) {
				CEdge edge = e1.nextElement();
				if (edge.getTailNode() != n) {
					// 自环不算入度
					n.indegree++;
				}
			}
			if (n.indegree == 0) {
				// 入度为0的节点入栈
				stack.push(n);
			}
		}
		while (list.size() < nodes.size()) {
			while (!stack.empty()) {
				CVexNode n = stack.pop();
				list.add(n);

				for (Enumeration<CEdge> e = n.getOutedges().elements(); e.hasMoreElements();) {
					CEdge edge = e.nextElement();
					CVexNode headnode = edge.getHeadNode();
					// 非自环
					if (headnode != n) {
						// 头节点入度-1
						if (headnode.indegree > 0) {
							headnode.indegree--;
							if (headnode.indegree == 0) {
								// 入度为0则入栈
								stack.push(headnode);
							}
						}
					}
				}
			}

			if (list.size() < nodes.size()) {
				// 存在环路
				CVexNode mindegreenode = null;
				// 选取入度>0，且最小的节点
				for (Enumeration<CVexNode> e = nodes.elements(); e.hasMoreElements();) {
					CVexNode n = e.nextElement();
					if (n.indegree > 0) {
						if (mindegreenode == null || mindegreenode.indegree > n.indegree) {
							mindegreenode = n;
						}
					}
				}
				// 破坏环路
				mindegreenode.indegree = 0;
				stack.push(mindegreenode);
			}
		}
		return list;
	}
}