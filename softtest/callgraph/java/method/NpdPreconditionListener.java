package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**NPDǰ�����������ߣ�����*/
public class NpdPreconditionListener extends AbstractPreconditionListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static NpdPreconditionListener onlyone=new NpdPreconditionListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private NpdPreconditionListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static NpdPreconditionListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new NpdPrecondition().listen(node, set);
	}
}
