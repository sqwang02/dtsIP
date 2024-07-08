package softtest.fsm.java;

import java.io.*;
import java.util.*;

/** 用于产生.dot文件的状态机访问者 */
public class DumpFSMVisitor implements FSMVisitor {
	/** 访问状态机的状态，打印状态名字 */
	public void visit(FSMState n, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = "";
			if (n.isStart()) {
				s = s + ",style=bold";
			}
			if (n.isFinal()) {
				s = s + ",peripheries=2";
			}
			if (n.isError()) {
				s = s + ",color=red";
			}
			//s=s+",orientation=90";
			out.write(n.getName() + "[label=\"" + n.getName() + "\"" + s + "];\n");
		} catch (IOException ex) {
		}
	}

	/** 访问状态机的转换，打印名字 */
	public void visit(FSMTransition e, Object data) {
		FileWriter out = (FileWriter) data;
		try {
			String s = "";
			s = e.getFromState().getName() + " -> " + e.getToState().getName() + "[label=\"" + e.getName() + "\"";
			s = s + "];\n";
			out.write(s);
		} catch (IOException ex) {
		}
		// 条件当前没有打印
		Iterator<FSMCondition> i=e.getConditions().iterator();
		while(i.hasNext()){
			visit(i.next(),out);
		}
	}

	/** 访问状态机，遍历访问其状态集合和转换集合 */
	public void visit(FSMMachine g, Object data) {
		try {
			FileWriter out = new FileWriter((String) data);
			out.write("digraph G {\n");
			out.write("label=\"" + g.getName() + "\";\n");
			//out.write("rotate=45;\n");

			for (Enumeration<FSMState> e = g.getStates().elements(); e.hasMoreElements();) {
				FSMState n = e.nextElement();
				visit(n, out);
			}

			for (Enumeration<FSMTransition> e = g.getTransitions().elements(); e.hasMoreElements();) {
				FSMTransition edge = e.nextElement();
				visit(edge, out);
			}
			out.write(" }");
			out.close();
		} catch (IOException ex) {
		}
	}
	
	/** 条件访问者 */
	public void visit(FSMCondition c, Object data){
		//data为FileWriter
	}
}
