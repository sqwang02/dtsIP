package softtest.callgraph.java;

/** CVexNode CEdge CGraph的抽象基类 */
public abstract class CElement {
	/** 访问者模式的accept方法 */
	public abstract void accept(CGraphVisitor visitor, Object data);
}
