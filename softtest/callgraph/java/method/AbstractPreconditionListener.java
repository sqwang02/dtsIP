package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**前置条件生成监听者抽象类*/
public abstract class AbstractPreconditionListener {
	/**当前函数的前置条件集合*/
	protected PreconditionSet set=null;
	
	/**获得当前函数的前置条件集合*/
	public PreconditionSet getPreconditionSet() {
		return set;
	}
	
	/**设置当前函数的前置条件集合*/
	public void setPreconditionSet(PreconditionSet set) {
		this.set = set;
	}
	
	/**可以忽略存在的抽象类构造方法*/
	protected AbstractPreconditionListener(){}
	
	/**监听接口*/
	abstract public void listen(SimpleJavaNode node);
}
