/**
 * 
 */
package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**
 * @author 彭平雷
 *
 */
public class SinkFeatureListener extends AbstractFeatureListener
{
	/**单子对象，保证全局唯一*/
	private static SinkFeatureListener onlyone=new SinkFeatureListener();
	
	/**私有构造函数，以防创建新的实例，导致对象的非唯一性*/
	private SinkFeatureListener(){}
	
	/**获得全局唯一的对象*/
	public static SinkFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new SinkFeature().listen(node, set);
	}
}
