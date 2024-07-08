package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

public class DomainPostconditionListener extends AbstractPostconditionListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static DomainPostconditionListener onlyone=new DomainPostconditionListener();
	
	/**私有的构造函数，阻止new*/
	private DomainPostconditionListener(){}
	
	/**获得全局唯一的对象*/
	public static DomainPostconditionListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new DomainPostcondition().listen(node, set);
	}

}