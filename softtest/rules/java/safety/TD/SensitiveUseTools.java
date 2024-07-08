/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.lang.reflect.Method;

import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.MethodNode;

/**
 * @author ��ƽ��
 *
 * 1 ��⺯�������� ��  �Զ��庯���� JDK����
 */
public class SensitiveUseTools
{
	
	/**
	 * �Ժ��������ͽ����ж�
	 * 0ΪJDK�⺯��
	 * 1Ϊ�Զ��庯��
	 * -1��ʾ����
	 * 
	 * @param treenode
	 * @return
	 */
	public static int typeOfFunction(SimpleJavaNode treenode)
	{
		int result = -1;
		Object type = null;
		
		/*
		 * �ýڵ��ȻΪ���ʽ�ڵ�
		 */
		ExpressionBase eb = (ExpressionBase)treenode;
		
		/*
		 * ��ȡ�ڵ��������Ϣ
		 */
		type = eb.getType();
		
		if (type != null && (type instanceof Method))
		{
			/*
			 * ������Ӧ�ĺ����ڵ�
			 */
			MethodNode methodnode=MethodNode.findMethodNode(type);
			
			if(methodnode != null)
				//���������Ӧ�ĺ����ڵ㣬��ô��ʾ�ú���Ϊ�Զ��庯��
			{
				//1�����Զ��庯��
				return 1;
			}
			else
			{
				//0����JDK�⺯��
				return 0;
			}
		}
		
		return result;
	}
}
