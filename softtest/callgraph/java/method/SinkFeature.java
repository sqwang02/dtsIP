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
 * @author 彭平雷
 *
 * 
 */
public class SinkFeature extends AbstractFeature
{
	/**
	 * 用于存放JDK库中的汇点规则
	 */
	public static Hashtable<String,SensitiveInfo> useTable = new Hashtable<String,SensitiveInfo>();
	
	/**
	 * 用于存放该函数的特征信息
	 * 其中key代表污染序号，注：0表示返回值
	 */	
	private Hashtable<Integer,List<String> > mapInfo = new Hashtable<Integer,List<String> >();
	
	@Override
	/**
	 * 计算该节点的函数摘要
	 * 1 首先计算JDK库中的汇点规则
	 * 2 根据JDK库的汇点规则，为自定义函数生成函数摘要
	 */
	public void listen(SimpleJavaNode node, FeatureSet set)
	{
		//计算JDK库中的汇点规则
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
 * 根据父函数中的子函数信息生成汇点特征
 * @author 彭平雷
 */
class SinkFeatureGeneratorFromCallInvok
{
	/**
	 * 入口语法节点
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
		 * 1 获取父函数的形参列表
		 */
		Hashtable<NameDeclaration, Integer> params = getFormalParameters(treeNode);
		
		//父函数没有形参，则不可能为汇点函数
		if(params == null) { return ; }
		if(params.size() <= 0){ return ; }
		
		/*
		 * 2 获取所有的子函数调用
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
			 * 3 建立子函数的实参与父函数的形参之间的映射关系
			 */
			Iterator it = initCalls.iterator();
			
			while(it.hasNext())
			{
				Object limppa = it.next();
				List<Integer> argMap = mapRealToFormal((SimpleJavaNode)limppa, params);
				
				if(argMap == null) { continue ; }
				if(argMap.size() <= 1) { continue ; }

				/*
				 * 4 获取子函数的type
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
				 * 5 生成函数摘要
				 */
				if((type instanceof Method) || (type instanceof Constructor)) 
				{
					MethodNode methodnode = MethodNode.findMethodNode(type);
					if (methodnode != null)
					{
						/*
						 * 5.1 根据自定义函数的汇点规则生成函数摘要
						 */
						MethodSummary summary = methodnode.getMethodsummary();

						if (summary != null)
						{
							// 查函数摘要，是否为输入函数
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
											newlist.add("文件:"
													+ ProjectAnalysis.current_file
													+ " 行:" + ((SimpleJavaNode)limppa).getBeginLine()
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
					 * 5.1 根据自JDK库函数的汇点规则生成函数摘要
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
											newlist.add("文件:"
													+ ProjectAnalysis.current_file
													+ " 行:" + ((SimpleJavaNode)limppa).getBeginLine()
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
							//						//如果JDK库中没有录入该函数的摘要信息，则不处理
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
		 * 1 初始化
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
				//表示子函数没有实参
			{
				return argMap;
			}
			
			/*
			 * 2 参数值的映射关系 void f(String s,int i) { g(i); }
			 * 
			 * void g(int i) { ... } 上例表示f函数的第2参数与g函数的第1参数相对应
			 */
			/*
			 * 2 参数值的映射关系
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
//				// 如果形参中包含该变量的定义
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
 * 根据父函数中的非函数调用类型的敏感使用生成汇点特征
 * 比如：while( i < count){...}
 * 比如：arrary[size];
 * 比如：i = i * j;
 * @author 彭平雷
 *
 */
class SinkFeatureGeneratorNotFromCallInvok
{
	/**
	 * 入口语法节点
	 */
	/**
	 * 入口语法节点
	 */
	private SimpleJavaNode treeNode = null;
	
	private SinkFeature sinkFeature = null;
	
	public SinkFeatureGeneratorNotFromCallInvok(SimpleJavaNode treeNode,SinkFeature sinkFeature)
	{
		this.treeNode = treeNode;
		
		this.sinkFeature = sinkFeature;
	}
	
	/**
	 * 根据非函数类型的特征生成汇点摘要
	 * 比如：while( count < max ){...} //如果max为外部输入，则可能导致DoS
	 * 		while( count < max ){...} //如果count为外部输入，则可能导致DoS
	 * 		while( f(input) )
	 * 比如：arrary[size];
	 * 比如：i = i * j;
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
		 *  sensitiveSeqs 用于存放安全敏感的参数的序列号
		 */
		ArrayList<Integer> sensitiveSeqs = new ArrayList<Integer>();
		
		if(treeNode == null){ return ; }
		
		/*
		 * 1 获取父函数的参数列表
		 */
		Hashtable<NameDeclaration, Integer> params = getFormalParameters(treeNode);
		
		/*
		 * 2 定位到特定的模式，并且记录敏感参数的序列号
		 */
		try
		{
			/*
			 * 2.1 while的谓词表达式中如果存在形参变量，则认为该变量是安全敏感的
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
//				//如果形参中包含该变量的定义
//				if (nd != null && params.containsKey(nd))
//				{
//					sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
//				} 
//			}
			
			/*
			 * 2.2 
			 * 
			 * 数组的初始化变量中如果存在形参变量，则认为该变量是安全敏感的
			 * 
			 */
			xpath = ".//ArrayDimsAndInits/Expression//PrimaryExpression/PrimaryPrefix/Name";
			list = treeNode.findChildNodesWithXPath(xpath);
			
			iter = list.iterator();
			
			while(iter.hasNext())
			{
				name = (ASTName) iter.next();
				
				nd = name.getNameDeclaration();
				
				//如果形参中包含该变量的定义
				if (nd != null && params.containsKey(nd))
				{
					sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
				} 
			}
			
			/*
			 * 2.3 
			 * 
			 * 算术表达式
			 * 
			 */
			xpath = ".//AdditiveExpression//Name | .//MultiplicativeExpression//Name | .//PreIncrementExpression//Name | .//PreDecrementExpression//Name | .//PostfixExpression//Name";
			list = treeNode.findChildNodesWithXPath(xpath);
			
			iter = list.iterator();
			
			while(iter.hasNext())
			{
				name = (ASTName) iter.next();
				
				nd = name.getNameDeclaration();
				
				//如果形参中包含该变量的定义
				if (nd != null && params.containsKey(nd))
				{
					if(name.getTypeString().equalsIgnoreCase("int"))
					{
						sensitiveSeqs.add(Integer.valueOf(params.get(nd) + 1));
					}
				} 
			}
			
			/*
			 * 3 生成汇点摘要信息
			 */
			if(sensitiveSeqs.size() > 0)
			{
				List<String> newlist = new ArrayList<String>();
				if (softtest.config.java.Config.LANGUAGE == 0)
				{
					newlist.add("文件:"
							+ ProjectAnalysis.current_file
							+ " 行:" + name.getBeginLine()
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
	 * 为一般的函数调用生成汇点特征
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		if (node == null) {
			return null;
		}
		
		/*
		 * 为函数类型的汇点生成摘要
		 */
		SinkFeatureGeneratorFromCallInvok sfGenFromCall = new SinkFeatureGeneratorFromCallInvok(node,sinkFeature);
		sfGenFromCall.genSinkFeature();
		
		/*
		 * 为非函数类型的汇点生成摘要
		 */
		SinkFeatureGeneratorNotFromCallInvok sfGenNotFromCall = new SinkFeatureGeneratorNotFromCallInvok(node,sinkFeature);
		sfGenNotFromCall.genSinkFeature();
		
		return null;
	}
	
	/**
	 * 为构造函数生成汇点特征
	 */
	public Object visit(ASTConstructorDeclaration node, Object data)
	{
		if(node == null)
		{
			return null;
		}
		
		/*
		 * 为函数类型的汇点生成摘要
		 */
		SinkFeatureGeneratorFromCallInvok sfGenFromCall = new SinkFeatureGeneratorFromCallInvok(node,sinkFeature);
		sfGenFromCall.genSinkFeature();
		
		/*
		 * 为非函数类型的汇点生成摘要
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
