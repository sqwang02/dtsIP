package softtest.fsmanalysis.java;
import softtest.cfg.java.*;



public class ClearFSMControlFlowVisitor implements GraphVisitor {
	/** �Խڵ���з��� */
	public void visit(VexNode n, Object data) {
		n.setFSMMachineInstanceSet(null);
		n.setVisited(true);
	}

	/** �Ա߽��з��� */
	public void visit(Edge e, Object data) {

	}

	/** ��ͼ���з��� */
	public void visit(Graph g, Object data) {

	}
}
