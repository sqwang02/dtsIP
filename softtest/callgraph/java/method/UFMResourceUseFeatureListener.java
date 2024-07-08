package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**RL������Դ���������ߣ�����*/
public class UFMResourceUseFeatureListener extends AbstractFeatureListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static UFMResourceUseFeatureListener onlyone=new UFMResourceUseFeatureListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private UFMResourceUseFeatureListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static UFMResourceUseFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new UFMResourceUseFeature().listen(node, set);
	}
}
