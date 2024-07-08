package softtest.DefUseAnalysis.java;

import java.util.*;
import softtest.symboltable.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

public class DUControlFlowVisitor implements GraphVisitor {
	/** 对节点进行访问 */
	public void visit(VexNode n, Object data) {
		calculateIN(n,data);
		calculateOUT(n,data);
	}

	/** 对边进行访问 */
	public void visit(Edge e, Object data) {

	}

	/** 对图进行访问 */
	public void visit(Graph g, Object data) {

	}

	/** 计算In */
	public void calculateIN(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);

		// 计算前驱节点的U(in)
		VexNode pre = null;
		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			pre = edge.getTailNode();
			
			if (edge.getName().startsWith("F")) {
				//处理一次循环
				if (n.getName().startsWith("while_out") || n.getName().startsWith("for_out")) {
					visit(pre, data);
				}
			}
			
			//汇合控制流，求并集
			n.getLiveDefs().mergeLiveDefs(pre.getLiveDefs());
		}
		//System.out.println(n.getName()+"    "+n.getLiveDefs());
	}

	/** 计算Out */
	public void calculateOUT(VexNode n, Object data) {
		for(NameOccurrence occ:n.getOccurrences()){
			if(!(occ.getDeclaration() instanceof VariableNameDeclaration)){
				continue;
			}
			LiveDefsSet  livedefs=n.getLiveDefs();
			VariableNameDeclaration v=(VariableNameDeclaration)occ.getDeclaration();
			if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.USE){
				for(NameOccurrence o: livedefs.getVariableLiveDefs(v)){
					o.addDefUse(occ);
					occ.addUseDef(o);
				}
			}else{
				livedefs.setNewDef(occ);
			}
		}
	}
}
