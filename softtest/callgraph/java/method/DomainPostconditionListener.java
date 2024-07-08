package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

public class DomainPostconditionListener extends AbstractPostconditionListener{

	/**ȫ��ֻ��һ��Ψһ�Ķ���ͨ��getInstance()���*/
	private static DomainPostconditionListener onlyone=new DomainPostconditionListener();
	
	/**˽�еĹ��캯������ֹnew*/
	private DomainPostconditionListener(){}
	
	/**���ȫ��Ψһ�Ķ���*/
	public static DomainPostconditionListener getInstance(){
		return onlyone; 
	}
	
	/**ʵ�ּ����ӿ�*/
	@Override
	public void listen(SimpleJavaNode node) {
		new DomainPostcondition().listen(node, set);
	}

}