package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**RL分配资源特征监听者，单体*/
public class UFMResourceUseFeatureListener extends AbstractFeatureListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static UFMResourceUseFeatureListener onlyone=new UFMResourceUseFeatureListener();
	
	/**私有的构造函数，阻止new*/
	private UFMResourceUseFeatureListener(){}
	
	/**获得全局唯一的对象*/
	public static UFMResourceUseFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new UFMResourceUseFeature().listen(node, set);
	}
}
