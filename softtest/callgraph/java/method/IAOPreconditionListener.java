package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**IAOǰ�����������ߣ�����*/
public class IAOPreconditionListener extends AbstractPreconditionListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static IAOPreconditionListener onlyone=new IAOPreconditionListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private IAOPreconditionListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static IAOPreconditionListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new IAOPrecondition().listen(node, set);
	}
}
