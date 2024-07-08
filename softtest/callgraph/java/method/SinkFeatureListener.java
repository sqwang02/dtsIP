/**
 * 
 */
package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**
 * @author ��ƽ��
 *
 */
public class SinkFeatureListener extends AbstractFeatureListener
{
	/**���Ӷ��󣬱�֤ȫ��Ψһ*/
	private static SinkFeatureListener onlyone=new SinkFeatureListener();
	
	/**˽�й��캯�����Է������µ�ʵ�������¶���ķ�Ψһ��*/
	private SinkFeatureListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static SinkFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new SinkFeature().listen(node, set);
	}
}
