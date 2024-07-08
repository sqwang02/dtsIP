package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**RL分配资源特征监听者，单体*/
public class AllocateFeatureListener extends AbstractFeatureListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static AllocateFeatureListener onlyone=new AllocateFeatureListener();
	
	/**私有的构造函数，阻止new*/
	private AllocateFeatureListener(){}
	
	/**获得全局唯一的对象*/
	public static AllocateFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new AllocateFeature().listen(node, set);
	}
}
