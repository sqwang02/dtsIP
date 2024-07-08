package softtest.callgraph.java;

/** 图的访问者接口 */
public interface CGraphVisitor {
	/** 节点访问者 */
	public void visit(CVexNode n, Object data);

	/** 边访问者 */
	public void visit(CEdge e, Object data);

	/** 图访问者 */
	public void visit(CGraph g, Object data);
}
