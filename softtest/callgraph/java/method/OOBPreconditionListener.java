package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

/**OOBǰ�����������ߣ�����*/
public class OOBPreconditionListener extends AbstractPreconditionListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static OOBPreconditionListener onlyone=new OOBPreconditionListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private OOBPreconditionListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static OOBPreconditionListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new OOBPrecondition().listen(node, set);
	}
}
