package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**OOB前置条件监听者，单体*/
public class OOBPreconditionListener extends AbstractPreconditionListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static OOBPreconditionListener onlyone=new OOBPreconditionListener();
	
	/**私有的构造函数，阻止new*/
	private OOBPreconditionListener(){}
	
	/**获得全局唯一的对象*/
	public static OOBPreconditionListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new OOBPrecondition().listen(node, set);
	}
}
