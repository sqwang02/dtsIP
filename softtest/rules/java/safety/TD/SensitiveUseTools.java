/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.lang.reflect.Method;

import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.MethodNode;

/**
 * @author 彭平雷
 *
 * 1 检测函数的类型 ：  自定义函数， JDK函数
 */
public class SensitiveUseTools
{
	
	/**
	 * 对函数的类型进行判断
	 * 0为JDK库函数
	 * 1为自定义函数
	 * -1表示错误
	 * 
	 * @param treenode
	 * @return
	 */
	public static int typeOfFunction(SimpleJavaNode treenode)
	{
		int result = -1;
		Object type = null;
		
		/*
		 * 该节点必然为表达式节点
		 */
		ExpressionBase eb = (ExpressionBase)treenode;
		
		/*
		 * 获取节点的类型信息
		 */
		type = eb.getType();
		
		if (type != null && (type instanceof Method))
		{
			/*
			 * 查找相应的函数节点
			 */
			MethodNode methodnode=MethodNode.findMethodNode(type);
			
			if(methodnode != null)
				//如果存在相应的函数节点，那么表示该函数为自定义函数
			{
				//1代表自定义函数
				return 1;
			}
			else
			{
				//0代表JDK库函数
				return 0;
			}
		}
		
		return result;
	}
}
