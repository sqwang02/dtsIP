package softtest.fsmanalysis.java;

import softtest.DefUseAnalysis.java.DUAnaysisVistor;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTSwitchLabel;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.CGraph;
import softtest.callgraph.java.CVexNode;
import softtest.callgraph.java.method.DomainPostconditionListener;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.cfg.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import softtest.symboltable.java.*;

class Stream {
	private static String filename = "stat"+File.separator+"pathcount_test";
	private static PrintStream ps = null;
	static {
		try {
			ps = new PrintStream(filename);
		} catch (Exception e) {
		}
	}
	public static void print(String str) {
		ps.print(str);
	}
}

public class PathCountVisitor extends JavaParserVisitorAdapter implements GraphVisitor {
	/** ������������ */
	public static int LOOP_NUM = 1;
	
	private HashMap<VexNode,Integer> countMap = new HashMap<VexNode, Integer>();
	
	public PathCountVisitor(boolean b) {
		CONTRADICT = b;
	}

	public Integer getCount(VexNode v) {
		return countMap.get(v);
	}
	
	public boolean CONTRADICT = false;
	
	public void calculateIN(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);

		// ����ǰ���ڵ��U(in)
		for (Edge edge : list) {
			VexNode pre = edge.getTailNode();
			int visits = countMap.get(n);		
			// �жϷ�֧�Ƿ�ì��
			if (CONTRADICT && edge.getContradict()) {
				continue;
			}
			
			if (edge.getName().startsWith("F")) {
				// ����һ��ѭ��
				if (n.getName().startsWith("while_out") || n.getName().startsWith("for_out")) {
					visit(pre, data);
				}
			}
			
			int preCount = 0;
			if (countMap.containsKey(pre)) {
				preCount = countMap.get(pre);
			}
			countMap.put(n, visits+preCount);
		}
	}

	public void calculateOUT(VexNode n, Object data) {
	}

	/** �Խڵ���з��� */
	public void visit(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}

		n.setVisited(true);
		if (n.getGraph().getEntryNode() == n) {
			countMap.put(n, 1);  
		}
		else {
			countMap.put(n, 0);
		}

		if (list.isEmpty() /*&& n.getSnumber() != 0*/) {
			return;
		}

		calculateIN(n, data);
		calculateOUT(n, data);
	}

	/** �Ա߽��з��� */
	public void visit(Edge e, Object data) {

	}

	/** ��ͼ���з��� */
	public void visit(Graph g, Object data) {

	}
	
	@Override
	public Object visit(ASTCompilationUnit treenode, Object data) {
		return super.visit(treenode, data);
	}

	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		
		if (g == null) {
			return null;
		}

		//		����������
		PathCountVisitor pcv = null;
		for (int j = 0; j < LOOP_NUM; j++) {
			pcv = new PathCountVisitor(CONTRADICT); 
			g.numberOrderVisit(pcv, data);
			g.clearVisited();
			g.setPathcount(pcv.getCount(g.getExitNode()));
		}
		
		return pcv.getCount(g.getExitNode());
	}

	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		
		if (g == null) {
			return null;
		}

		//	����������
		PathCountVisitor pcv = null;
		for (int j = 0; j < LOOP_NUM; j++) {
			pcv = new PathCountVisitor(CONTRADICT); 
			g.numberOrderVisit(pcv, data);
			g.clearVisited();
			g.setPathcount(pcv.getCount(g.getExitNode()));
		}
		System.out.println("·����ĿΪ��"+g.getPathcount());
		return pcv.getCount(g.getExitNode());
	}
	
	class Node {
		int id;
		List<Integer> nextNodes;

		Node(int id) {
			this.id = id;
			nextNodes = new ArrayList<>();
		}
	}

	public class ControlFlowGraph {
		List<Node> nodes;

		ControlFlowGraph() {
			nodes = new ArrayList<>();
		}

		public List<List<Integer>> getAllPaths(int startNode, int endNode) {
			List<List<Integer>> paths = new ArrayList<>();
			List<Integer> currentPath = new ArrayList<>();

			getAllPathsUtil(startNode, endNode, currentPath, paths);

			return paths;
		}

		private void getAllPathsUtil(int currentNode, int endNode, List<Integer> currentPath, List<List<Integer>> paths) {
			currentPath.add(currentNode);
			if (currentNode == endNode) {
				paths.add(new ArrayList<>(currentPath));
			} else {
				Node node = nodes.get(currentNode);
				for (int nextNode : node.nextNodes) {
					getAllPathsUtil(nextNode, endNode, currentPath, paths);
				}
			}
			currentPath.remove(currentPath.size() - 1);
		}

		public void main(String[] args) {
			// ����������ͼ
			ControlFlowGraph graph = new ControlFlowGraph();
			Node node1 = new Node(1);
			Node node2 = new Node(2);
			Node node3 = new Node(3);
			Node node4 = new Node(4);
			Node node5 = new Node(5);
			Node node6 = new Node(6);

			node1.nextNodes.add(2);
			node1.nextNodes.add(3);
			node2.nextNodes.add(4);
			node2.nextNodes.add(5);
			node3.nextNodes.add(6);
			node4.nextNodes.add(6);
			node5.nextNodes.add(6);

			graph.nodes.add(node1);
			graph.nodes.add(node2);
			graph.nodes.add(node3);
			graph.nodes.add(node4);
			graph.nodes.add(node5);
			graph.nodes.add(node6);

			// ��ȡ����·��
			List<List<Integer>> paths = graph.getAllPaths(1, 6);

			// ��ӡ����·��
			for (List<Integer> path : paths) {
				System.out.println("��ӡ����·����"+path);
			}
		}
	}


	/*
	public static int fileAnalysis(Object loopdata,String filename) throws FileNotFoundException{
		//�������ժҪ������������Ϣ�Ĺ���
		MapOfVariable.clear();
		TypeSet.clear();
		
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(filename)));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		ProjectAnalysis.current_file=filename;
		ProjectAnalysis.current_astroot=astroot;
		
		// ���ű����
		// 1. ���������������
		// 2. ���ʽ���ͷ���
		// 3. ��Ƿ����ַ���
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astroot.jjtAccept(sc, null);	
		astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet.getCurrentTypeSet());
		astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
		astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet.getCurrentTypeSet());
		OccurrenceFinder of = new OccurrenceFinder();
		astroot.jjtAccept(of, null);

		// ����������ͼ
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		
		//������ʹ��
		astroot.jjtAccept(new DUAnaysisVistor() , null);
	
		//��ʼ������
		astroot.getScope().initDomains();
		
		//�����ļ��ڲ����ù�ϵ
		ProjectAnalysis.current_call_graph = new CGraph();
		astroot.getScope().resolveCallRelation(ProjectAnalysis.current_call_graph);
		
		List<CVexNode> list=ProjectAnalysis.current_call_graph .getTopologicalOrderList();
		Collections.reverse(list);
		
		ProjectAnalysis.getPostconditionListener().addListener(DomainPostconditionListener.getInstance());
		
		//���㺯��ժҪ
		ControFlowDomainVisitor tempvisitor=new ControFlowDomainVisitor();
		ArrayList<SimpleJavaNode> methodlist=new ArrayList<SimpleJavaNode>();
		HashSet<SimpleJavaNode> methodset=new HashSet<SimpleJavaNode>();
		for(CVexNode n:list){
			ASTMethodDeclaration method=(ASTMethodDeclaration)n.getMethodNameDeclaration().getMethodNameDeclaratorNode().jjtGetParent();
			method.jjtAccept(tempvisitor, null);
			methodset.add(method);	
			ProjectAnalysis.genMethodSummary(method);
		}
		astroot.jjtAccept(new MethodAndConstructorFinder(), methodlist);
		
		for(SimpleJavaNode method:methodlist){
			if(!methodset.contains(method)){
				method.jjtAccept(tempvisitor, null);
				ProjectAnalysis.genMethodSummary(method);
			}
		}
		
		for(SimpleJavaNode method:methodlist){
			Integer count1 = (Integer) method.jjtAccept(new PathCountVisitor(false), loopdata);
			Integer count2 = (Integer) method.jjtAccept(new PathCountVisitor(true), loopdata);
			if (count1 == null) {
				continue;
			}
			SimpleJavaNode simplejavanode = (SimpleJavaNode) method.jjtGetChild(1);
			String name = "ABC";
			if (method instanceof ASTMethodDeclaration && ((ASTMethodDeclaration)method).getType()!=null) {
				name = ((ASTMethodDeclaration)method).getType().toString();
			}
			else if (method instanceof ASTConstructorDeclaration && ((ASTConstructorDeclaration)method).getType()!=null) {
				name = ((ASTConstructorDeclaration)method).getType().toString();
			}
			else {
				//name = simplejavanode.getImage();
			}
			name = name.replace(' ', '-');
			Stream.print(count1+" "+count2+" "+name+" "+ProjectAnalysis.current_file+"\n");
			//System.out.print(count1+" "+count2+"\n");
		}
		
		//		 experiment
		//ProjectAnalysis.functions += methodlist.size();
		ProjectAnalysis.current_file=null;
		ProjectAnalysis.current_astroot=null;
		ProjectAnalysis.current_call_graph = null;
		return astroot.getEndLine();
	}*/
	
	public static void main(String args[]) {
		try {
			new TypeSet(null);
			throw new FileNotFoundException();
			//fileAnalysis(null, "C:\\DTS\\experiment\\aTunes\\src\\src\\net\\sourceforge\\atunes\\kernel\\handlers\\HotkeyHandler.java");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
class MethodAndConstructorFinder1 extends JavaParserVisitorAdapter{
	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		ArrayList<SimpleJavaNode> methodlist=(ArrayList<SimpleJavaNode>)data;
		methodlist.add(treenode);
		return null;
	}
	
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		ArrayList<SimpleJavaNode> methodlist=(ArrayList<SimpleJavaNode>)data;
		methodlist.add(treenode);
		return null;
	}
}