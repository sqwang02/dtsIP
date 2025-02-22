/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.method.AbstractFeature;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.SinkFeature;
import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachineInstance;
import softtest.summary.lib.java.SensitiveInfo;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author 彭平雷
 *
 * 比如： 
 * 1 p().f(), p().g(String), p().h(byte[])
 * 2 o.p().f(), o.p().g(String), o.p().h(byte[])
 * 
 * xpath = .//PrimaryExpression//PrimarySuffix[@MethodName='true']
 *
 */
public class ChainFunctionInvokationSensitiveChecker implements ICommonFunctionSensitiveChecker
{

	public boolean checkUsed(VexNode n, FSMMachineInstance fsmin)
	{
		String xpath = ".//PrimaryExpression//PrimarySuffix[@MethodName='true']";
		/*
		 * 1 找到对应的语法节点
		 */
		SimpleNode treenode=n.getTreeNode().getConcreteNode();
		if(treenode==null){
			return false;
		}
		
		try
		{
			/*
			 * 2 通过Xpath匹配出所有的语法节点 
			 */	
			List list = treenode.findChildNodesWithXPath(xpath);
			
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * 3 判断函数的类型 
				 */
				int typeOfFunction = -1;
				Object type = null;
				MethodNode methodnode = null;
				
					//该节点必然为表达式节点
				ASTPrimarySuffix aps = (ASTPrimarySuffix)list.get(i);
				
					//获取节点的类型信息
				type = aps.getType();
				
				if (type != null && (type instanceof Method))
				{
					//查找相应的函数节点
					methodnode=MethodNode.findMethodNode(type);
					
					if(methodnode != null)
						//如果存在相应的函数节点，那么表示该函数为自定义函数
					{
						typeOfFunction = 1;//1代表自定义函数
					}
					else
					{
						typeOfFunction = 0;//0代表JDK库函数
					}
				}
				/*
				 * 4 如果函数类型为自定义函数
				 */
				if(typeOfFunction == 1)
				{
					//获取该函数的函数摘要
					MethodSummary summary = methodnode.getMethodsummary();
					
					if(summary != null)
						//如果存在函数摘要
					{
						for (AbstractFeature ff : summary.getFeatrues().getTable().values())
							//遍历所有的函数特征
						{
							if (!(ff instanceof SinkFeature))
								//如果不是敏感使用相关的函数特征，则继续查找
							{
								continue;
							}
							
							//获取敏感使用相关的函数特征
							SinkFeature rf = (SinkFeature) ff;
							
							for (Map.Entry<Integer, List<String>> e : rf.getMapInfo().entrySet())
								//遍历该函数特征的所有内容
							{
								//获取对使用敏感的参数序号
								Integer ide = e.getKey();
								int index = ide.intValue();
								
								//获取该函数的参数列表
								ASTPrimaryExpression pe = (ASTPrimaryExpression) aps.getFirstParentOfType(ASTPrimaryExpression.class);
								ASTPrimarySuffix ps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
								ASTArguments args = (ASTArguments) ps.jjtGetChild(0);
								
								if( args.jjtGetNumChildren() == 0 )
								{
									continue;
								}
								
								if( args.jjtGetChild(0).jjtGetNumChildren() < index )
								{
									continue;
								}
								
								ASTExpression aexpr = (ASTExpression) args.jjtGetChild(0).jjtGetChild(index - 1);
								
								//查找相应的变量
								List vars = aexpr.findChildNodesWithXPath(".//PrimaryExpression/PrimaryPrefix/Name");
								Iterator it = vars.iterator();
								while (it.hasNext())
								{
									//获取变量
									ASTName name = (ASTName) it.next();
									
									//获取污染集合
									Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = ((TaintedSet)fsmin.getRelatedObject()).getTable();
							
									for (Enumeration<VariableNameDeclaration> ee = table.elements(); ee.hasMoreElements();)
										//遍历污染集合中的所有变量
									{
										VariableNameDeclaration v = ee.nextElement();
										if (v == name.getNameDeclaration()) 
											//如果污染集合中的某一元素与敏感使用元素相同，则报错
										{
											fsmin.setResultString(v.getImage());
											return true;
										}
									}
								}
								
							}
						}
					}
					else
						//如果不存在函数摘要
					{
						return false;
					}
				}
				/*
				 * 5 如果函数类型为JDK库函数
				 */
				else if(typeOfFunction == 0)
				{
					//获取函数的typeString
					String key = type.toString();
					
					//查找敏感使用表
					SensitiveInfo sensitiveInfo = SinkFeature.useTable.get(key);
					
					if(sensitiveInfo != null)
						//如果存在相应的表项内容
					{
						//获取该表项内容
						List seqs = sensitiveInfo.getSensitiveSeqs();
						
						for(int j=0; j<seqs.size(); j++)
							//遍历该表项内容
						{
							//获取对使用敏感的参数序号
							int index = Integer.parseInt((String) seqs.get(j));
							
							//获取该函数的参数列表
							ASTPrimaryExpression pe = (ASTPrimaryExpression) aps.getFirstParentOfType(ASTPrimaryExpression.class);
							ASTPrimarySuffix ps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
							ASTArguments args = (ASTArguments) ps.jjtGetChild(0);
							
							if( args.jjtGetNumChildren() == 0 )
							{
								continue;
							}
							
							if( args.jjtGetChild(0).jjtGetNumChildren() < index )
							{
								continue;
							}
							
							ASTExpression aexpr = (ASTExpression) args.jjtGetChild(0).jjtGetChild(index - 1);
							
							//查找相应的变量
							List vars = aexpr.findChildNodesWithXPath(".//PrimaryExpression/PrimaryPrefix/Name");
							Iterator it = vars.iterator();
							while (it.hasNext())
							{
								//获取变量
								ASTName name = (ASTName) it.next();
								
								//获取污染集合
								Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = ((TaintedSet)fsmin.getRelatedObject()).getTable();
						
								for (Enumeration<VariableNameDeclaration> ee = table.elements(); ee.hasMoreElements();)
									//遍历污染集合中的所有变量
								{
									VariableNameDeclaration v = ee.nextElement();
									if (v == name.getNameDeclaration()) 
										//如果污染集合中的某一元素与敏感使用元素相同，则报错
									{
										fsmin.setResultString(v.getImage());
										return true;
									}
								}
							}
						}
					}
				}
				/*
				 * 6 如果出错
				 */
				else
				{
					return false;
				}
				
			}
			
			
		} 
		catch (JaxenException e)
		{
			e.printStackTrace();
		}
		
		
		return false;
	}

}
