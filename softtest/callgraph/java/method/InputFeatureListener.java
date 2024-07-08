/**
 * 
 */
package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**
 * @author limppa
 *
 */
public class InputFeatureListener extends AbstractFeatureListener
{

	/**单子对象，保证全局唯一*/
	private static InputFeatureListener onlyone=new InputFeatureListener();
	
	/**私有构造函数，以防创建新的实例，导致对象的非唯一性*/
	private InputFeatureListener(){}
	
	/**获得全局唯一的对象*/
	public static InputFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**实现监听接口*/
	@Override
	public void listen(SimpleJavaNode node) {
		new InputFeature().listen(node, set);
	}

}
