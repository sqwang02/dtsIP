package softtest.callgraph.java;

import java.io.*;
import java.util.*;

/** 用于产生.dot文件的调用关系图访问者 */
public class DumpCGraphVisitor implements CGraphVisitor{
	/** 访问控制流图的节点，打印节点名字，当前的变量域集，条件限定域集 */
	public void visit(CVexNode n, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = null;
			s = ""+"\"";
			out.write(n.name + "[label=\"" + n.name + "\\n" + s + "];\n");
		} catch (IOException ex) {
		}
	}

	/** 访问控制流图的边，打印名字 */
	public void visit(CEdge e, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = null;
			s = e.tailnode.name + " -> " + e.headnode.name + "[label=\"" + e.name + "\"";
			s = s + "];\n";
			out.write(s);
		} catch (IOException ex) {
		}
	}

	/** 访问控制流图，遍历访问其节点集合和边集合 */
	public void visit(CGraph g, Object data) {
		try {
			FileWriter out = new FileWriter((String) data);
			out.write("digraph G {\n");

			for (Enumeration<CVexNode> e = g.nodes.elements(); e.hasMoreElements();) {
				CVexNode n = e.nextElement();
				visit(n, out);
			}

			for (Enumeration<CEdge> e = g.edges.elements(); e.hasMoreElements();) {
				CEdge edge = e.nextElement();
				visit(edge, out);
			}
			out.write(" }");
			out.close();
		} catch (IOException ex) {
		}
	}
}
