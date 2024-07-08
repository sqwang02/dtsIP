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
 * @author ��ƽ��
 *
 * ���磺 
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
		 * 1 �ҵ���Ӧ���﷨�ڵ�
		 */
		SimpleNode treenode=n.getTreeNode().getConcreteNode();
		if(treenode==null){
			return false;
		}
		
		try
		{
			/*
			 * 2 ͨ��Xpathƥ������е��﷨�ڵ� 
			 */	
			List list = treenode.findChildNodesWithXPath(xpath);
			
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * 3 �жϺ��������� 
				 */
				int typeOfFunction = -1;
				Object type = null;
				MethodNode methodnode = null;
				
					//�ýڵ��ȻΪ���ʽ�ڵ�
				ASTPrimarySuffix aps = (ASTPrimarySuffix)list.get(i);
				
					//��ȡ�ڵ��������Ϣ
				type = aps.getType();
				
				if (type != null && (type instanceof Method))
				{
					//������Ӧ�ĺ����ڵ�
					methodnode=MethodNode.findMethodNode(type);
					
					if(methodnode != null)
						//���������Ӧ�ĺ����ڵ㣬��ô��ʾ�ú���Ϊ�Զ��庯��
					{
						typeOfFunction = 1;//1�����Զ��庯��
					}
					else
					{
						typeOfFunction = 0;//0����JDK�⺯��
					}
				}
				/*
				 * 4 �����������Ϊ�Զ��庯��
				 */
				if(typeOfFunction == 1)
				{
					//��ȡ�ú����ĺ���ժҪ
					MethodSummary summary = methodnode.getMethodsummary();
					
					if(summary != null)
						//������ں���ժҪ
					{
						for (AbstractFeature ff : summary.getFeatrues().getTable().values())
							//�������еĺ�������
						{
							if (!(ff instanceof SinkFeature))
								//�����������ʹ����صĺ������������������
							{
								continue;
							}
							
							//��ȡ����ʹ����صĺ�������
							SinkFeature rf = (SinkFeature) ff;
							
							for (Map.Entry<Integer, List<String>> e : rf.getMapInfo().entrySet())
								//�����ú�����������������
							{
								//��ȡ��ʹ�����еĲ������
								Integer ide = e.getKey();
								int index = ide.intValue();
								
								//��ȡ�ú����Ĳ����б�
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
								
								//������Ӧ�ı���
								List vars = aexpr.findChildNodesWithXPath(".//PrimaryExpression/PrimaryPrefix/Name");
								Iterator it = vars.iterator();
								while (it.hasNext())
								{
									//��ȡ����
									ASTName name = (ASTName) it.next();
									
									//��ȡ��Ⱦ����
									Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = ((TaintedSet)fsmin.getRelatedObject()).getTable();
							
									for (Enumeration<VariableNameDeclaration> ee = table.elements(); ee.hasMoreElements();)
										//������Ⱦ�����е����б���
									{
										VariableNameDeclaration v = ee.nextElement();
										if (v == name.getNameDeclaration()) 
											//�����Ⱦ�����е�ĳһԪ��������ʹ��Ԫ����ͬ���򱨴�
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
						//��������ں���ժҪ
					{
						return false;
					}
				}
				/*
				 * 5 �����������ΪJDK�⺯��
				 */
				else if(typeOfFunction == 0)
				{
					//��ȡ������typeString
					String key = type.toString();
					
					//��������ʹ�ñ�
					SensitiveInfo sensitiveInfo = SinkFeature.useTable.get(key);
					
					if(sensitiveInfo != null)
						//���������Ӧ�ı�������
					{
						//��ȡ�ñ�������
						List seqs = sensitiveInfo.getSensitiveSeqs();
						
						for(int j=0; j<seqs.size(); j++)
							//�����ñ�������
						{
							//��ȡ��ʹ�����еĲ������
							int index = Integer.parseInt((String) seqs.get(j));
							
							//��ȡ�ú����Ĳ����б�
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
							
							//������Ӧ�ı���
							List vars = aexpr.findChildNodesWithXPath(".//PrimaryExpression/PrimaryPrefix/Name");
							Iterator it = vars.iterator();
							while (it.hasNext())
							{
								//��ȡ����
								ASTName name = (ASTName) it.next();
								
								//��ȡ��Ⱦ����
								Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = ((TaintedSet)fsmin.getRelatedObject()).getTable();
						
								for (Enumeration<VariableNameDeclaration> ee = table.elements(); ee.hasMoreElements();)
									//������Ⱦ�����е����б���
								{
									VariableNameDeclaration v = ee.nextElement();
									if (v == name.getNameDeclaration()) 
										//�����Ⱦ�����е�ĳһԪ��������ʹ��Ԫ����ͬ���򱨴�
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
				 * 6 �������
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
