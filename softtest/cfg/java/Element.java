package softtest.cfg.java;

/** VexNode Edge Graph的抽象基类 */
public abstract class Element {
	/** 访问者模式的accept方法 */
	public abstract void accept(GraphVisitor visitor, Object data);
}
