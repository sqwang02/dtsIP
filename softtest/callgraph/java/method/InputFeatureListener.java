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

	/**���Ӷ��󣬱�֤ȫ��Ψһ*/
	private static InputFeatureListener onlyone=new InputFeatureListener();
	
	/**˽�й��캯�����Է������µ�ʵ�������¶���ķ�Ψһ��*/
	private InputFeatureListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static InputFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new InputFeature().listen(node, set);
	}

}
