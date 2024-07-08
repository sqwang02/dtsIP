package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**RL�ͷ���Դ���������ߣ�����*/
public class ReleaseFeatureListener extends AbstractFeatureListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static ReleaseFeatureListener onlyone=new ReleaseFeatureListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private ReleaseFeatureListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static ReleaseFeatureListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new ReleaseFeature().listen(node, set);
	}
}
