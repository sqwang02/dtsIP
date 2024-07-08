package softtest.fsmanalysis.java;
import softtest.cfg.java.*;



public class ClearFSMControlFlowVisitor implements GraphVisitor {
	/** 对节点进行访问 */
	public void visit(VexNode n, Object data) {
		n.setFSMMachineInstanceSet(null);
		n.setVisited(true);
	}

	/** 对边进行访问 */
	public void visit(Edge e, Object data) {

	}

	/** 对图进行访问 */
	public void visit(Graph g, Object data) {

	}
}
