package softtest.cfg.java;

import java.io.*;
import java.util.*;

/** 用于产生.dot文件的控制流图访问者 */
public class DumpEdgesVisitor implements GraphVisitor { // 对图遍历的访问者接口
	/** 访问控制流图的节点，打印节点名字，当前的变量域集，条件限定域集 */
	public void visit(VexNode n, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = null;
			s = "" + n.getDomainSet();
			if (n.getConditionData() != null) {
				s = s + "\\n" + n.getConditionData();
			}
			if (n.getFSMMachineInstanceSet() != null) {
				s = s + "\\n" + n.getFSMMachineInstanceSet();
			}
			if (n.getReturnDomain() != null) {
				s = s + "\\nreturn: " + n.getReturnDomain();
			}
			s = s + "\"";
			if (n.getContradict()) {
				s = s + ",color=red";
			}
			out.write(n.name + "[label=\"" + n.name + "\\n" + s + "];\n");
		} catch (IOException ex) {
		}
	}

	/** 访问控制流图的边，打印名字 */
	public void visit(Edge e, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = null;
			s = e.tailnode.name + " -> " + e.headnode.name + "[label=\"" + e.name + "\"";
			if (e.getContradict()) {
				s = s + ",color=red";
			}
			s = s + "];\n";
			out.write(s);
			// out.write(e.tailnode.name + " -> " + e.headnode.name +
			// "[label=\"" + e.name + "\"];\n");
		} catch (IOException ex) {
		}
	}

	/** 访问控制流图，遍历访问其节点集合和边集合 */
	public void visit(Graph g, Object data) {
		try {
			FileWriter out = new FileWriter((String) data);
			out.write("digraph G {\n");

			for (Enumeration<VexNode> e = g.nodes.elements(); e.hasMoreElements();) {
				VexNode n = e.nextElement();
				visit(n, out);
			}

			for (Enumeration<Edge> e = g.edges.elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				visit(edge, out);
			}
			out.write(" }");
			out.close();
		} catch (IOException ex) {
		}
	}
}
