package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

public abstract class AbstractPostconditionListener {
	/**当前函数的前置条件集合*/
	protected PostconditionSet set=null;
	
	/**获得当前函数的前置条件集合*/
	public PostconditionSet getPostconditionSet() {
		return set;
	}
	
	/**设置当前函数的前置条件集合*/
	public void setPostconditionSet(PostconditionSet set) {
		this.set = set;
	}
	
	/**可以忽略存在的抽象类构造方法*/
	protected AbstractPostconditionListener(){}
	
	/**监听接口*/
	abstract public void listen(SimpleJavaNode node);
}
