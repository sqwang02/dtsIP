package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**RL释放资源特征监听者，单体*/
public class ReleaseFeatureListener extends AbstractFeatureListener{

	/**全局只有一个唯一的对象，通过getInstance()获得*/
	private static ReleaseFeatureListener onlyone=new ReleaseFeatureListener();
	
	/**私有的构造函数，阻止new*/
	private ReleaseFeatureListener(){}
	
	/**获得全局唯一的对象*/
	public static ReleaseFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new ReleaseFeature().listen(node, set);
	}
}
