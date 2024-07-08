/**
 * 
 */
package softtest.callgraph.java.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.summary.lib.java.SensitiveInfo;
import softtest.summary.lib.java.TDLibParser;
import softtest.symboltable.java.NameDeclaration;

/**
 * @author ��ƽ��
 *
 * 
 */
public class SinkFeature extends AbstractFeature
{
	/**
	 * ���ڴ��JDK���еĻ�����
	 */
	public static Hashtable<String,SensitiveInfo> useTable = new Hashtable<String,SensitiveInfo>();
	
	/**
	 * ���ڴ�Ÿú�����������Ϣ
	 * ����key������Ⱦ��ţ�ע��0��ʾ����ֵ
	 */	
	private Hashtable<Integer,List<String> > mapInfo = new Hashtable<Integer,List<String> >();
	
	@Override
	/**
	 * ����ýڵ�ĺ���ժҪ
	 * 1 ���ȼ���JDK���еĻ�����
	 * 2 ����JDK��Ļ�����Ϊ�Զ��庯�����ɺ���ժҪ
	 */
	public void listen(SimpleJavaNode node, FeatureSet set)
	{
		//����JDK���еĻ�����
		if(SinkFeature.useTable.size() == 0)
		{
			TDLibParser tdlibParser = new TDLibParser(null,"./cfg/td/TD_use.xml",null);
			SinkFeature.useTable = tdlibParser.parseSinkReg();
		}
		
		SinkFeatureVisitor visitor = new SinkFeatureVisitor(this);
		node.jjtAccept(visitor, null);
		
		if(mapInfo.size() > 0)
		{
			set.addFeature(this);
		}
	}

	public static Hashtable<String, SensitiveInfo> getUseTable()
	{
		return useTable;
	}

	public static void setUseTable(Hashtable<String, SensitiveInfo> useTable)
	{
		SinkFeature.useTable = useTable;
	}

	public Hashtable<Integer, List<String>> getMapInfo()
	{
		return mapInfo;
	}

	public void setMapInfo(Hashtable<Integer, List<String>> mapInfo)
	{
		this.mapInfo = mapInfo;
	}
}

/**
 * ���ݸ������е��Ӻ�����Ϣ���ɻ������
 * @author ��ƽ��
 */
class SinkFeatureGeneratorFromCallInvok
{
	/**
	 * ����﷨�ڵ�
	 */
	private SimpleJavaNode treeNode = null;
	
	private SinkFeature sinkFeature = null;
	
	public SinkFeatureGeneratorFromCallInvok(SimpleJavaNode treeNode,SinkFeature sinkFeature)
	{
		this.treeNode = treeNode;
		
		this.sinkFeature = sinkFeature;
	}
	
	public Hashtable<NameDeclaration, Integer> getFormalParameters(SimpleJavaNode treeNode)
	{
		Hashtable<NameDeclaration, Integer> params = new Hashtable<NameDeclaration, Integer>();
		
		String xpath = ".//FormalParameters/FormalParameter/VariableDeclaratorId";
		
		try
		{
			List varIds = treeNode.findChildNodesWithXPath(xpath);
			
			int cnt = 0;
			for (Object o : varIds)
			{
				ASTVariableDeclaratorId vdi = (ASTVariableDeclaratorId) o;
				params.put(vdi.getNameDeclaration(), cnt);
				++cnt;
			}
		} 
		catch (JaxenException e)
		{
			throw new RuntimeException(
					"xpath error @ SinkFeatureGeneratorFromCallInvok.java : getFormalParameters(SimpleJavaNode treeNode)",
					e);
		}
		
		return params;
	}
	
	public void genSinkFeature()
	{
		SimpleJavaNode treeNode = this.getTreeNode();
		
		if(treeNode == null){ return ; }
		
		/*
		 * 1 ��ȡ���������β��б�
		 */
		Hashtable<NameDeclaration, Integer> params = getFormalParameters(treeNode);
		
		//������û���βΣ��򲻿���Ϊ��㺯��
		if(params == null) { return ; }
		if(params.size() <= 0){ return ; }
		
		/*
		 * 2 ��ȡ���е��Ӻ�������
		 */
		List initCalls = null;
		
		String case1 = ".//PrimaryExpression/PrimaryPrefix[@MethodName='true']";
		String case2 = ".//PrimaryExpression/PrimarySuffix[@MethodName='true']";
		String case3 = ".//AllocationExpression[./Arguments]";
		
		String subCallPath = case1.trim() + " | " + case2.trim() + " | " + case3.trim();
		
		try
		{
			initCalls = treeNode.findChildNodesWithXPath(subCallPath);
			
			/*
			 * 3 �����Ӻ�����ʵ���븸�������β�֮���ӳ���ϵ
			 */
			Iterator it = initCalls.iterator();
			
			while(it.hasNext())
			{
				Object limppa = it.next();
				List<Integer> argMap = mapRealToFormal((SimpleJavaNode)limppa, params);
				
				if(argMap == null) { continue ; }
				if(argMap.size() <= 1) { continue ; }

				/*
				 * 4 ��ȡ�Ӻ�����type
				 */
				Object type = null;
				Object pr = limppa;
				
				if ((pr instanceof ASTPrimaryPrefix)
						|| (pr instanceof ASTPrimarySuffix)
						|| (pr instanceof ASTAllocationExpression))
				{
					type = ((ExpressionBase) pr).getType();
				}
				
				if(type == null) { continue ; }
				
				/*
				 * 5 ���ɺ���ժҪ
				 */
				if((type instanceof Method) || (type instanceof Constructor)) 
				{
					MethodNode methodnode = MethodNode.findMethodNode(type);
					if (methodnode != null)
					{
						/*
						 * 5.1 �����Զ��庯���Ļ��������ɺ���ժҪ
						 */
						MethodSummary summary = methodnode.getMethodsummary();

						if (summary != null)
						{
							// �麯��ժҪ���Ƿ�Ϊ���뺯��
							for (AbstractFeature ff : summary.getFeatrues()
									.getTable().values())
							{
								if (!(ff instanceof SinkFeature))
								{
									continue;
								}
								SinkFeature rf = (SinkFeature) ff;
								for (Map.Entry<Integer, List<String>> e : rf
										.getMapInfo().entrySet())
								{
									Integer ide = e.getKey();
									List<String> dis = e.getValue();
									
									if(argMap.size() <= ide.intValue())
									{
										continue;
									}

									Integer i = (Integer) argMap.get(ide.intValue());
									if (!i.equals(-1))
									{
										List<String> newlist = new ArrayList<String>(
												dis);
										if (softtest.config.java.Config.LANGUAGE == 0)
										{
											newlist.add("�ļ�:"
													+ ProjectAnalysis.current_file
													+ " ��:" + ((SimpleJavaNode)limppa).getBeginLine()
													+ "\n");
										} else
										{
											newlist.add("file:"
													+ ProjectAnalysis.current_file
													+ " line:" + ((SimpleJavaNode)limppa).getBeginLine()
													+ "\n");
										}
										
										Hashtable<Integer,List<String> > mapInfo = this.sinkFeature.getMapInfo();
										mapInfo.put(i, newlist);
									}
								}
							}
						}
					} else
					/*
					 * 5.1 ������JDK�⺯���Ļ��������ɺ���ժҪ
					 */
					{
							String key = type.toString();
							SensitiveInfo sensitiveInfo = SinkFeature.useTable.get(key);
							
							if (sensitiveInfo != null)
							{
								List seqs = sensitiveInfo.getSensitiveSeqs();

								for (int i = 0; i < seqs.size(); i++)
								{
									int ki = Integer.parseInt((String) seqs.get(i));
									
									if(argMap.size() <= ki)
									{
										continue;
									}
									
									Integer k = argMap.get(ki);

									if (!k.equals(-1))
									{
										List<String> newlist = new ArrayList<String>();
										if (softtest.config.java.Config.LANGUAGE == 0)
										{
											newlist.add("�ļ�:"
													+ ProjectAnalysis.current_file
													+ " ��:" + ((SimpleJavaNode)limppa).getBeginLine()
													+ "\n");
										} else
										{
											newlist.add("file:"
													+ ProjectAnalysis.current_file
													+ " line:" + ((SimpleJavaNode)limppa).getBeginLine()
													+ "\n");
										}
										
										Hashtable<Integer,List<String> > mapInfo = this.sinkFeature.getMapInfo();
										mapInfo.put(k, newlist);
									}
								}
							}
							//					else
							//						//���JDK����û��¼��ú�����ժҪ��Ϣ���򲻴���
							//					{
							//					}
						}
					}
				}
			
		} 
		catch (JaxenException e)
		{
			throw new RuntimeException(
					"xpath error @ SinkFeatureGeneratorFromCallInvok.java : genSinkFeature()",
					e);
		}
	}
	
	public List<Integer> mapRealToFormal(SimpleJavaNode treeNode,Hashtable<NameDeclaration, Integer> params )
	{
		List<Integer> argMap = new ArrayList<Integer>();

		/*
		 * 1 ��ʼ��
		 */
		try
		{
			List realargs = null;
			String xpath = "./Arguments/ArgumentList/Expression";
			
			if((treeNode instanceof ASTPrimaryPrefix) || (treeNode instanceof ASTPrimarySuffix))
			{
				SimpleJavaNode node = (SimpleJavaNode) treeNode.getNextSibling();
				if(node == null) {return argMap;} 
				if( !(node instanceof ASTPrimarySuffix) ) {return argMap;}
				realargs = node.findChildNodesWithXPath(xpath);
			}
			else if(treeNode instanceof ASTAllocationExpression)
			{
				realargs = treeNode.findChildNodesWithXPath(xpath);
			}
			else
			{
				return argMap;
			}
				
			argMap.add(0, Integer.valueOf(-1));
			for (int count = 1; count <= realargs.size(); count++)
			{
				argMap.add(count, Integer.valueOf(-1));
			}
			
			if(argMap.size() == 1)
				//��ʾ�Ӻ���û��ʵ��
			{
				return argMap;
			}
			
			/*
			 * 2 ����ֵ��ӳ���ϵ void f(String s,int i) { g(i); }
			 * 
			 * void g(int i) { ... } ������ʾf�����ĵ�2������g�����ĵ�1�������Ӧ
			 */
			/*
			 * 2 ����ֵ��ӳ���ϵ
			 * 
			 */

				//realargs = pe.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression");
				
				for(int i=1; i<=realargs.size(); i++)
				{
					ASTExpression expr = (ASTExpression)realargs.get(i-1);
					
					List names = expr.findChildNodesWithXPath(".//Name");
					NameDeclaration nd = null;
					int flag = 0;
					
				for(int j=0; j<names.size(); j++)
				{
					ASTName name = (ASTName) names.get(j);
					nd = name.getNameDeclaration();
						
					if(nd != null && params.containsKey(nd))
					{
						argMap.add(i, params.get(nd)+1);
						flag = 1;
						break;
					}
				}
					
				if(flag == 0)
				{
					argMap.add(i,-1);
				}
					
			}
		}
		catch (JaxenException e) 
		{
			throw new RuntimeException(
						"xpath error @ SinkFeatureGeneratorFromCallInvok.java : mapRealToFormal(SimpleJavaNode treeNode,Hashtable<NameDeclaration, Integer> params )",
						e);
		}
			
//			int c = 1;
//			for (Object o : realargs)
//			{
//				ASTExpression expr = (ASTExpression) o;
//				
//				// f( x )
//				List name = expr
//						.findChildNodesWithXPath(".//PrimaryExpression[count(PrimarySuffix)=0]/PrimaryPrefix/Name");
//				// f( g() )
//				List suffix = expr
//						.findChildNodesWithXPath(".//PrimaryExpression[count(PrimarySuffix)=1 and count(PrimaryPrefix)=1 and ./PrimaryPrefix[count(*)=0]]/PrimarySuffix[count(*)=0]");
//				NameDeclaration nd = null;
//				
//				if (name.size() == 1)
//				{
//					ASTName nn = (ASTName) name.get(0);
//					
//					nd = nn.getNameDeclaration();
//				} else if (suffix.size() == 1)
//				{
//					ASTPrimarySuffix ps1 = (ASTPrimarySuffix) suffix.get(0);
//					
//					nd = ps1.getNameDeclaration();
//				}
//
//				// ����β��а����ñ����Ķ���
//				if (nd != null && params.containsKey(nd))
//				{
//					argMap.add(c, params.get(nd) + 1);
//				} else
//				{
//					argMap.add(c, -1);
//				}
//				c++;
//			}
//			
//		} 
//		catch (JaxenException e)
//		{
//			throw new RuntimeException(
//					"xpath error @ SinkFeatureGeneratorFromCallInvok.java : mapRealToFormal(SimpleJavaNode treeNode,Hashtable<NameDeclaration, Integer> params )",
//					e);
//		}
			
		return argMap;
	}

	//accessors
	public SimpleJavaNode getTreeNode()
	{
		return treeNode;
	}

	public void setTreeNode(SimpleJavaNode treeNode)
	{
		this.treeNode = treeNode;
	}

	public SinkFeature getSinkFeature()
	{
		return sinkFeature;
	}

	public void setSinkFeature(SinkFeature sinkFeature)
	{
		this.sinkFeature = sinkFeature;
	}
}

/**
 * ���ݸ������еķǺ����������͵�����ʹ�����ɻ������
 * ���磺while( i < count){...}
 * ���磺arrary[size];
 * ���磺i = i * j;
 * @author ��ƽ��
 *
 */
class SinkFeatureGeneratorNotFromCallInvok
{
	/**
	 * ����﷨�ڵ�
	 */
	/**
	 * ����﷨�ڵ�
	 */
	private SimpleJavaNode treeNode = null;
	
	private SinkFeature sinkFeature = null;
	
	public SinkFeatureGeneratorNotFromCallInvok(SimpleJavaNode treeNode,SinkFeature sinkFeature)
	{
		this.treeNode = treeNode;
		
		this.sinkFeature = sinkFeature;
	}
	
	/**
	 * ���ݷǺ������͵��������ɻ��ժҪ
	 * ���磺while( count < max ){...} //���maxΪ�ⲿ���룬����ܵ���DoS
	 * 		while( count < max ){...} //���countΪ�ⲿ���룬����ܵ���DoS
	 * 		while( f(input) )
	 * ���磺arrary[size];
	 * ���磺i = i * j;
	 */
	public void genSinkFeature()
	{
		SimpleJavaNode treeNode = this.getTreeNode();
		String xpath = null;
		List list = null;
		Iterator iter = null;
		ASTName name = null;
		NameDeclaration nd = null;
		/*
		 *  sensitiveSeqs ���ڴ�Ű�ȫ���еĲ��������к�
		 */
		ArrayList<Integer> sensitiveSeqs = new ArrayList<Integer>();
		
		if(treeNode == null){ return ; }
		
		/*
		 * 1 ��ȡ�������Ĳ����б�
		 */
		Hashtable<NameDeclaration, Integer> params = getFormalParameters(treeNode);
		
		/*
		 * 2 ��λ���ض���ģʽ�����Ҽ�¼���в��������к�
		 */
		try
		{
			/*
			 * 2.1 while��ν�ʱ��ʽ����������βα���������Ϊ�ñ����ǰ�ȫ���е�
			 * 
			 */
//			xpath = ".//WhileStatement/Expression//PrimaryExpression/PrimaryPrefix/Name";
//			list = treeNode.findChildNodesWithXPath(xpath);
//			
//			iter = list.iterator();
//			
//			while(iter.hasNext())
//			{
//				name = (ASTName) iter.next();
//				
//				nd = name.getNameDeclaration();
//				
//				//����β��а����ñ����Ķ���
//				if (nd != null && params.containsKey(nd))
//				{
//					sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
//				} 
//			}
			
			/*
			 * 2.2 
			 * 
			 * ����ĳ�ʼ����������������βα���������Ϊ�ñ����ǰ�ȫ���е�
			 * 
			 */
			xpath = ".//ArrayDimsAndInits/Expression//PrimaryExpression/PrimaryPrefix/Name";
			list = treeNode.findChildNodesWithXPath(xpath);
			
			iter = list.iterator();
			
			while(iter.hasNext())
			{
				name = (ASTName) iter.next();
				
				nd = name.getNameDeclaration();
				
				//����β��а����ñ����Ķ���
				if (nd != null && params.containsKey(nd))
				{
					sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
				} 
			}
			
			/*
			 * 2.3 
			 * 
			 * �������ʽ
			 * 
			 */
			xpath = ".//AdditiveExpression//Name | .//MultiplicativeExpression//Name | .//PreIncrementExpression//Name | .//PreDecrementExpression//Name | .//PostfixExpression//Name";
			list = treeNode.findChildNodesWithXPath(xpath);
			
			iter = list.iterator();
			
			while(iter.hasNext())
			{
				name = (ASTName) iter.next();
				
				nd = name.getNameDeclaration();
				
				//����β��а����ñ����Ķ���
				if (nd != null && params.containsKey(nd))
				{
					if(name.getTypeString().equalsIgnoreCase("int"))
					{
						sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
					}
				} 
			}
			
			/*
			 * 3 ���ɻ��ժҪ��Ϣ
			 */
			if(sensitiveSeqs.size() > 0)
			{
				List<String> newlist = new ArrayList<String>();
				if (softtest.config.java.Config.LANGUAGE == 0)
				{
					newlist.add("�ļ�:"
							+ ProjectAnalysis.current_file
							+ " ��:" + name.getBeginLine()
							+ "\n");
				} else
				{
					newlist.add("file:"
							+ ProjectAnalysis.current_file
							+ " line:" + name.getBeginLine()
							+ "\n");
				}
				
				Iterator it = sensitiveSeqs.iterator();
				while(it.hasNext())
				{
					Integer index = (Integer) it.next();
					
					Hashtable<Integer,List<String> > mapInfo = this.sinkFeature.getMapInfo();
					mapInfo.put(index, newlist);
				}
				
			}
		} 
		catch (JaxenException e)
		{
			throw new RuntimeException(
					"xpath error @ SinkFeatureGeneratorNotFromCallInvok.java : genSinkFeature()",
					e);
		}
		
		
		
	}
	
	public Hashtable<NameDeclaration, Integer> getFormalParameters(SimpleJavaNode treeNode)
	{
		Hashtable<NameDeclaration, Integer> params = new Hashtable<NameDeclaration, Integer>();
		
		String xpath = ".//FormalParameters/FormalParameter/VariableDeclaratorId";
		
		try
		{
			List varIds = treeNode.findChildNodesWithXPath(xpath);
			
			int cnt = 0;
			for (Object o : varIds)
			{
				ASTVariableDeclaratorId vdi = (ASTVariableDeclaratorId) o;
				params.put(vdi.getNameDeclaration(), cnt);
				++cnt;
			}
		} 
		catch (JaxenException e)
		{
			throw new RuntimeException(
					"xpath error @ SinkFeature.java : getFormalParameters(SimpleJavaNode treeNode)",
					e);
		}
		
		return params;
	}
	
	//accessors
	public SimpleJavaNode getTreeNode()
	{
		return treeNode;
	}

	public void setTreeNode(SimpleJavaNode treeNode)
	{
		this.treeNode = treeNode;
	}

	public SinkFeature getSinkFeature()
	{
		return sinkFeature;
	}

	public void setSinkFeature(SinkFeature sinkFeature)
	{
		this.sinkFeature = sinkFeature;
	}
}

class SinkFeatureVisitor extends JavaParserVisitorAdapter
{
	private SinkFeature sinkFeature;
	
	public SinkFeatureVisitor(SinkFeature sinkFeature)
	{
		this.sinkFeature = sinkFeature;
	}
	
	/**
	 * Ϊһ��ĺ����������ɻ������
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		if (node == null) {
			return null;
		}
		
		/*
		 * Ϊ�������͵Ļ������ժҪ
		 */
		SinkFeatureGeneratorFromCallInvok sfGenFromCall = new SinkFeatureGeneratorFromCallInvok(node,sinkFeature);
		sfGenFromCall.genSinkFeature();
		
		/*
		 * Ϊ�Ǻ������͵Ļ������ժҪ
		 */
		SinkFeatureGeneratorNotFromCallInvok sfGenNotFromCall = new SinkFeatureGeneratorNotFromCallInvok(node,sinkFeature);
		sfGenNotFromCall.genSinkFeature();
		
		return null;
	}
	
	/**
	 * Ϊ���캯�����ɻ������
	 */
	public Object visit(ASTConstructorDeclaration node, Object data)
	{
		if(node == null)
		{
			return null;
		}
		
		/*
		 * Ϊ�������͵Ļ������ժҪ
		 */
		SinkFeatureGeneratorFromCallInvok sfGenFromCall = new SinkFeatureGeneratorFromCallInvok(node,sinkFeature);
		sfGenFromCall.genSinkFeature();
		
		/*
		 * Ϊ�Ǻ������͵Ļ������ժҪ
		 */
		SinkFeatureGeneratorNotFromCallInvok sfGenNotFromCall = new SinkFeatureGeneratorNotFromCallInvok(node,sinkFeature);
		sfGenNotFromCall.genSinkFeature();
		
		return null;
	}

	public SinkFeature getSinkFeature()
	{
		return sinkFeature;
	}

	public void setSinkFeature(SinkFeature sinkFeature)
	{
		this.sinkFeature = sinkFeature;
	}
}
