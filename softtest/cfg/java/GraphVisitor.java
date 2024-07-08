package softtest.cfg.java;

/** 图的访问者接口 */
public interface GraphVisitor {
	/** 节点访问者 */
	public void visit(VexNode n, Object data);

	/** 边访问者 */
	public void visit(Edge e, Object data);

	/** 图访问者 */
	public void visit(Graph g, Object data);
}
