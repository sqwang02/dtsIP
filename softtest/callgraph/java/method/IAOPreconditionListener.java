package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**IAO前置条件监听者，单体*/
public class IAOPreconditionListener extends AbstractPreconditionListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static IAOPreconditionListener onlyone=new IAOPreconditionListener();
	
	/**私有的构造函数，阻止new*/
	private IAOPreconditionListener(){}
	
	/**获得全局唯一的对象*/
	public static IAOPreconditionListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new IAOPrecondition().listen(node, set);
	}
}
