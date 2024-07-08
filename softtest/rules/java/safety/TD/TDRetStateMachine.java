/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.AbstractFeature;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.InputFeature;
import softtest.callgraph.java.method.InputFeatureListener;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.callgraph.java.method.SinkFeature;
import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.rules.java.AbstractStateMachine;
import softtest.summary.lib.java.SensitiveInfo;
import softtest.summary.lib.java.TaintedInfo;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author 彭平雷
 * 
 */
public class TDRetStateMachine extends AbstractStateMachine
{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline)
	{
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0)
		{
			f.format("未验证的输入: 变量 \'%s\' 的数据来自 %d 行的外部输入，在 %d 行上进行使用前并没有进行数据合法性检查，可能造成一个漏洞",
					fsmmi.getResultString(), beginline, errorline);
		} else
		{
			f.format("Tainted Data: the data in variable \'%s\' comes from an outer input function on line %d may cause a vulnerability on line %d.",
					fsmmi.getResultString(), beginline, errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}

	@Override
	public void registerPrecondition(PreconditionListenerSet listeners)
	{
	}

	@Override
	public void registerFeature(FeatureListenerSet listeners)
	{
		//listeners.addListener(InputFeatureListener.getInstance());
		//listeners.addListener(SensitiveUseFeatureListener.getInstance());
	}

	/**
	 * 创建状态集实例函数，为每个输入函数创建一个状态机实例，跟踪其感染的变量集合
	 * 
	 * 1 定位到特定的语法模式： f( g() ); o.f( g() ); this.f( g() ); h().f( g() ); o.h().f(
	 * g() ); if( f( g() ) ){...} 2 对每个函数调用进行判断； 3 如果该函数不为输入函数，continue; 4
	 * 如果该函数污染的变量不是返回值，continue; 5 如果该函数外不存在1阶父函数调用，continue; 6
	 * 如果1阶父函数不是汇点函数，continue; 7 如果1阶父函数的敏感变量序号不是该函数所列序号，continue; 8 return
	 * true;
	 * 
	 * 如何定位到函数？ PrimarySuffix[@Arguments='true']
	 * 
	 * 如何定位到函数名？ .//PrimaryPrefix[@MethodName='true'] e.g : f()
	 * .//PrimarySuffix[@MethodName='true'] e.g : this.f()
	 * 
	 * 如何定位到1阶父函数？ o = getFirstParentOf()。 而后对语法元素进行函数身份验证。
	 * 
	 * 如何记录子函数在参数列表中的序号？ 不好做。算法改进如下：
	 * 
	 * 1 定位到特定的语法模式： f(),o.f() ; this.f(); h().f(), o.h().f(); if( f()
	 * ){......}; 2 对每个函数调用进行判断； 3 如果该函数不是汇点函数，continue; 4 遍历该汇点函数的所有敏感序列； 5
	 * 如果参数列表i处不是函数调用，continue; 6 如果参数列表i处不是输入函数，continue; 7
	 * 如果参数列表i处的输入函数未对返回值污染，continue;
	 */
	public static List<FSMMachineInstance> createTDRetStateMachines(
			SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		/*
		 * 1 定位到特定的语法模式 f() || o.f() :
		 * .//PrimaryExpression/PrimaryPrefix[@MethodName='true'] this.f()
		 * this.h().g() h().f() o.h().f() :
		 * .//PrimaryExpression/PrimarySuffix[@MethodName='true'] if(f()){...} :
		 * 同上 new A(); .//AllocationExpression[./Arguments]
		 */

		String case1 = ".//PrimaryExpression[./PrimaryPrefix[@MethodName='true']]";
		String case2 = ".//PrimaryExpression[./PrimarySuffix[@MethodName='true']]";
		String case3 = ".//AllocationExpression[./Arguments]";

		String cond = case1.trim() + " | " + case2.trim() + " | "
				+ case3.trim();
		try
		{
			List initFuns = node.findChildNodesWithXPath(cond);
			

			for (int i = 0; i < initFuns.size(); i++)
			{
				//ASTPrimaryExpression pe = (ASTPrimaryExpression) initFuns.get(i);
				
				Object limppa = initFuns.get(i);
				Object pr = null;
				Object type = null;
				
				if(limppa instanceof ASTPrimaryExpression)
				{
					ASTPrimaryExpression pe = (ASTPrimaryExpression) limppa;
					
					/*
					 * 获取type
					 */
					pr = pe.jjtGetChild(pe.jjtGetNumChildren() - 2);
				}
				else if(limppa instanceof ASTAllocationExpression)
				{
					pr = limppa;
				}
				
				if ((pr instanceof ASTPrimaryPrefix)
						|| (pr instanceof ASTPrimarySuffix)
						|| (pr instanceof ASTAllocationExpression))
				{
					type = ((ExpressionBase) pr).getType();
				}

				if (!(type instanceof Method) && !(type instanceof Constructor))
				{
					continue;
				}
				
//				/*
//				 * 获取type
//				 */
//				Object pr = pe.jjtGetChild(pe.jjtGetNumChildren() - 2);
//				Object type = null;
//				if ((pr instanceof ASTPrimaryPrefix)
//						|| (pr instanceof ASTPrimarySuffix)
//						|| (pr instanceof ASTAllocationExpression))
//				{
//					type = ((ExpressionBase) pr).getType();
//				}
//
//				if ((type == null) || !(type instanceof Method)
//						|| !(type instanceof Constructor))
//				{
//					continue;
//				}

				/*
				 * 2 如果该函数不是汇点函数，continue;
				 */
				MethodNode methodnode = MethodNode.findMethodNode(type);
				if (methodnode != null)
				// 自定义函数
				{
					MethodSummary summary = methodnode.getMethodsummary();

					if (summary != null)
					{
						for (AbstractFeature ff : summary.getFeatrues().getTable().values())
						{
							// 表示该函数不是汇点函数
							if (!(ff instanceof SinkFeature))
							{
								continue;
							}

							SinkFeature sf = (SinkFeature) ff;

							// 获取汇点函数的参数列表
							//Object obj = pe.jjtGetChild(pe.jjtGetNumChildren() - 1);
							Object obj = null;
							
							if(limppa instanceof ASTPrimaryExpression)
							{
								obj = ((ASTPrimaryExpression)limppa).jjtGetChild(((ASTPrimaryExpression)limppa).jjtGetNumChildren() - 1);
							}
							else if(limppa instanceof ASTAllocationExpression)
							{
								obj = ((ASTAllocationExpression)limppa).jjtGetChild(((ASTAllocationExpression)limppa).jjtGetNumChildren() - 1);
							}
							ASTArguments args = null;

							if (obj == null)
							{
								continue;
							} else if (obj instanceof ASTArguments)
							{
								args = (ASTArguments) obj;
							} else if (obj instanceof ASTPrimarySuffix)
							{
								args = (ASTArguments) ((ASTPrimarySuffix) obj)
										.jjtGetChild(0);
							} else if (obj instanceof ASTPrimaryPrefix)
							{
								args = (ASTArguments) ((ASTPrimaryPrefix) obj)
										.jjtGetChild(0);
							}

							if(! args.containsChildOfType(ASTArgumentList.class))
							{
								continue;
							}
							// 参数列表
							ASTArgumentList argList = (ASTArgumentList) args
									.jjtGetChild(0);

							if (argList == null)
							{
								continue;
							}

							/*
							 * 3 遍历该汇点函数的所有敏感序列
							 */
							for (Map.Entry<Integer, List<String>> e : sf
									.getMapInfo().entrySet())
							{
								Integer ide = e.getKey();

								int index = ide.intValue();
								
								if(argList.jjtGetNumChildren() < index)
								{
									continue;
								}
								
								// 定位到该参数
								if (argList.jjtGetChild(index - 1) == null)
								{
									continue;
								}

								ASTExpression exp = (ASTExpression) argList
										.jjtGetChild(index - 1);

								/*
								 * 4 该表达式中是否包含函数调用，如果没有,那么continue;
								 */
								String path1 = "./PrimaryExpression/PrimaryPrefix[@MethodName='true']";
								String path2 = "./PrimaryExpression/PrimarySuffix[@MethodName='true']";
								String path = path1.trim() + " | "
										+ path2.trim();

								List innerFuns = exp
										.findChildNodesWithXPath(path);

								if (innerFuns.size() == 0)
								{
									continue;
								}

								for (int j = 0; j < innerFuns.size(); j++)
								{
									//ASTPrimaryExpression inner_pe = (ASTPrimaryExpression) innerFuns.get(j);
									/*
									 * 获取type
									 */
									//Object inner_obj = inner_pe.jjtGetChild(inner_pe.jjtGetNumChildren() - 2);
									Object inner_obj = innerFuns.get(j);
									Object inner_type = null;
									if ((inner_obj instanceof ASTPrimaryPrefix)
											|| (inner_obj instanceof ASTPrimarySuffix))
									{
										inner_type = ((ExpressionBase) inner_obj)
												.getType();
									}
									
									if ((inner_type== null) || !(inner_type instanceof Method))
									{
										continue;
									}
									
									/*
									 * 6 如果该函数不是输入函数,continue
									 */
									MethodNode inner_methodnode = MethodNode.findMethodNode(inner_type);
									if (inner_methodnode != null)
									// 自定义函数
									{
										MethodSummary inner_summary = inner_methodnode.getMethodsummary();
										
										if (inner_summary != null)
										{
											for (AbstractFeature inner_ff : inner_summary.getFeatrues()
													.getTable().values())
											{
												// 表示该函数不是输入函数
												if (!(inner_ff instanceof InputFeature))
												{
													continue;
												}
												InputFeature inerif = (InputFeature) inner_ff;
												/*
												 * 7 遍历该输入函数的所有污染序列
												 */
												for (Map.Entry<Integer, List<String>> inner_e : inerif
														.getMapInfo().entrySet())
												{
													Integer inner_ide = inner_e.getKey();
													
													int inner_index = inner_ide.intValue();
													
													if(inner_index != 0)
													{
														continue;
													}
													
													
													/*
													 * 8 创建状态机
													 */
													FSMMachineInstance fsminstance = fsm.creatInstance();
													
//													 创建被感染的变量集合，不添加集合，在Inputed处添加
													TaintedSet tainted = new TaintedSet();
													// 设置标记节点
													tainted.setTagTreeNode((SimpleJavaNode)limppa);
													fsminstance.setRelatedObject(tainted);
													list.add(fsminstance);
												}
											}
										}
									}
									else
									//库函数
									{
										String key = inner_type.toString();
										
										TaintedInfo taintedInfo = InputFeature.inputTable.get(key);
										
										if(taintedInfo == null)
										{
											continue;
										}
										
										List seqs = taintedInfo.getTaintedSeqs();
										
										for(int k=0; k<seqs.size(); k++)
										{
											int inner_index = Integer.parseInt((String) seqs.get(k));
											
											if(inner_index != 0)
											{
												continue;
											}
											/*
											 * 8 创建状态机
											 */
											FSMMachineInstance fsminstance = fsm.creatInstance();
											
//											 创建被感染的变量集合，不添加集合，在Inputed处添加
											TaintedSet tainted = new TaintedSet();
											// 设置标记节点
											tainted.setTagTreeNode((SimpleJavaNode)limppa);
											fsminstance.setRelatedObject(tainted);
											list.add(fsminstance);
										}
									}

								}
							}
						}
					}
				} else
				// 库函数
				{
					String key = type.toString();
					
					SensitiveInfo sensitiveInfo = SinkFeature.useTable.get(key);
					
					if(sensitiveInfo == null)
					{
						continue;
					}
					
//					 获取汇点函数的参数列表
					//Object obj = pe.jjtGetChild(pe.jjtGetNumChildren() - 1);
					Object obj = null;
					
					if(limppa instanceof ASTPrimaryExpression)
					{
						obj = ((ASTPrimaryExpression)limppa).jjtGetChild(((ASTPrimaryExpression)limppa).jjtGetNumChildren() - 1);
					}
					else if(limppa instanceof ASTAllocationExpression)
					{
						obj = ((ASTAllocationExpression)limppa).jjtGetChild(((ASTAllocationExpression)limppa).jjtGetNumChildren() - 1);
					}
					ASTArguments args = null;

					if (obj == null)
					{
						continue;
					} else if (obj instanceof ASTArguments)
					{
						args = (ASTArguments) obj;
					} else if (obj instanceof ASTPrimarySuffix)
					{
						args = (ASTArguments) ((ASTPrimarySuffix) obj)
								.jjtGetChild(0);
					} else if (obj instanceof ASTPrimaryPrefix)
					{
						args = (ASTArguments) ((ASTPrimaryPrefix) obj)
								.jjtGetChild(0);
					}
					
					if(! args.containsChildOfType(ASTArgumentList.class))
					{
						continue;
					}

					// 参数列表
					ASTArgumentList argList = (ASTArgumentList) args
							.jjtGetChild(0);

					if (argList == null)
					{
						continue;
					}

					/*
					 * 3 遍历该汇点函数的所有敏感序列
					 */
					List seqs = sensitiveInfo.getSensitiveSeqs();
					for (int q = 0; q<seqs.size(); q++ )
					{
						int index = Integer.parseInt((String) seqs.get(q));
						
						if(argList.jjtGetNumChildren() < index)
						{
							continue;
						}

						// 定位到该参数
						if (argList.jjtGetChild(index - 1) == null)
						{
							continue;
						}

						ASTExpression exp = (ASTExpression) argList
								.jjtGetChild(index - 1);

						/*
						 * 4 该表达式中是否包含函数调用，如果没有,那么continue;
						 */
						String path1 = "./PrimaryExpression/PrimaryPrefix[@MethodName='true']";
						String path2 = "./PrimaryExpression/PrimarySuffix[@MethodName='true']";
						String path = path1.trim() + " | "
								+ path2.trim();

						List innerFuns = exp
								.findChildNodesWithXPath(path);

						if (innerFuns.size() == 0)
						{
							continue;
						}

						for (int j = 0; j < innerFuns.size(); j++)
						{
							//ASTPrimaryExpression inner_pe = (ASTPrimaryExpression) innerFuns.get(j);
							/*
							 * 获取type
							 */
							Object inner_obj = innerFuns.get(j);
							//Object inner_obj = inner_pe.jjtGetChild(inner_pe.jjtGetNumChildren() - 2);
							Object inner_type = null;
							if ((inner_obj instanceof ASTPrimaryPrefix)
									|| (inner_obj instanceof ASTPrimarySuffix))
							{
								inner_type = ((ExpressionBase) inner_obj)
										.getType();
							}
							
							if ((inner_type== null) || !(inner_type instanceof Method))
							{
								continue;
							}
							
							
							//System.out.println(inner_type);
							/*
							 * 6 如果该函数不是输入函数,continue
							 */
							MethodNode inner_methodnode = MethodNode.findMethodNode(inner_type);
							if (inner_methodnode != null)
							// 自定义函数
							{
								MethodSummary inner_summary = inner_methodnode.getMethodsummary();
								
								if (inner_summary != null)
								{
									for (AbstractFeature inner_ff : inner_summary.getFeatrues()
											.getTable().values())
									{
										// 表示该函数不是输入函数
										if (!(inner_ff instanceof InputFeature))
										{
											continue;
										}
										InputFeature inerif = (InputFeature) inner_ff;
										/*
										 * 7 遍历该输入函数的所有污染序列
										 */
										for (Map.Entry<Integer, List<String>> inner_e : inerif
												.getMapInfo().entrySet())
										{
											Integer inner_ide = inner_e.getKey();
											
											int inner_index = inner_ide.intValue();
											
											if(inner_index != 0)
											{
												continue;
											}
											
											/*
											 * 8 创建状态机
											 */
											FSMMachineInstance fsminstance = fsm.creatInstance();
											
//											 创建被感染的变量集合，不添加集合，在Inputed处添加
											TaintedSet tainted = new TaintedSet();
											// 设置标记节点
											tainted.setTagTreeNode((SimpleJavaNode)limppa);
											fsminstance.setRelatedObject(tainted);
											list.add(fsminstance);
										}
									}
								}
							}
							else
							//库函数
							{
								String inner_key = inner_type.toString();
								
								TaintedInfo taintedInfo = InputFeature.inputTable.get(inner_key);
								
								if(taintedInfo == null)
								{
									continue;
								}
								
								List inner_seqs = taintedInfo.getTaintedSeqs();
								
								for(int k=0; k<inner_seqs.size(); k++)
								{
									int inner_index = Integer.parseInt((String) inner_seqs.get(k));
									
									if(inner_index != 0)
									{
										continue;
									}
									
									/*
									 * 8 创建状态机
									 */
									FSMMachineInstance fsminstance = fsm.creatInstance();
									
//									 创建被感染的变量集合，不添加集合，在Inputed处添加
									TaintedSet tainted = new TaintedSet();
									// 设置标记节点
									tainted.setTagTreeNode((SimpleJavaNode)limppa);
									fsminstance.setRelatedObject(tainted);
									list.add(fsminstance);
								}
							}

						}
					}
				}
			}
		} catch (JaxenException e)
		{
			e.printStackTrace();
		}
		
		/**
		 * add by ppl 2009-7-15
		 * 
		 * case1 new byte[in.read()];
		 * caes2 new byte[in.read()][];
		 * 
		 * .//AllocationExpression/ArrayDimsAndInits/Expression/PrimaryExpression[count(*) >= 2]
		 * 
		 * 1 定位到特定的模式：数组初始化
		 * 2 初始化
		 */
		
		String xpath = ".//AllocationExpression/ArrayDimsAndInits/Expression/PrimaryExpression[count(*) >= 2]";
		
		/*
		 * 1 定位到特定的模式
		 */
		try
		{
			List arrs = node.findChildNodesWithXPath(xpath);
			
			/*
			 * 2 遍历所有的candidate
			 */
			if(arrs.size() != 0)
			{
				Iterator it = arrs.iterator();
				
				while(it.hasNext())
				{
					ASTPrimaryExpression pe = (ASTPrimaryExpression) it.next();
					
					/*
					 * 3 获取innner函数的type
					 */
					Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
					Object type = null;
					if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
						type = ((ExpressionBase)pr).getType();
					} 
					
					if (type != null && (type instanceof Method))
					{
						MethodNode methodnode=MethodNode.findMethodNode(type);
						
						if(methodnode != null){
							// 自定义函数
							MethodSummary summary = methodnode.getMethodsummary();

							if (summary != null) {
								for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
									if (!(ff instanceof InputFeature)) {
										continue;
									}
									InputFeature rf = (InputFeature) ff;
									
									for (Map.Entry<Integer,List<String>> e : rf.getMapInfo().entrySet()) {
										
										Integer ide = e.getKey();
										
										int index = ide.intValue();
										
										if(index != 0)
										{
											continue;
										}
										
										/*
										 * 4 创建状态机
										 */
										FSMMachineInstance fsminstance = fsm.creatInstance();
										
										//创建被感染的变量集合，不添加集合，在Inputed处添加
										TaintedSet tainted = new TaintedSet();
										// 设置标记节点
										tainted.setTagTreeNode(pe);
										fsminstance.setRelatedObject(tainted);
										list.add(fsminstance);
									}
								}
							}
						}
						else
							//库函数
						{
							String key = type.toString();
							
							TaintedInfo taintedInfo = InputFeature.inputTable.get(key);
							
							if(taintedInfo != null)
							{
								List seqs = taintedInfo.getTaintedSeqs();
								
								for(int j=0; j<seqs.size(); j++)
								{
									int kj = Integer.parseInt((String) seqs.get(j));
									
									if(kj != 0)
									{
										continue;
									}
									
									/*
									 * 4 创建状态机
									 */
									FSMMachineInstance fsminstance = fsm.creatInstance();
									
									//创建被感染的变量集合，不添加集合，在Inputed处添加
									TaintedSet tainted = new TaintedSet();
									// 设置标记节点
									tainted.setTagTreeNode(pe);
									fsminstance.setRelatedObject(tainted);
									list.add(fsminstance);
								}
							}
						}
					}
				}
				
			}
		} 
		catch (JaxenException e)
		{
			e.printStackTrace();
		}
		
		

		return list;
	}

	public static boolean checkError(List nodes, FSMMachineInstance fsmin)
	{
		Iterator i = nodes.iterator();
		
		while (i.hasNext()) {
			Object o = i.next();
			TaintedSet tainted = (TaintedSet) fsmin.getRelatedObject();
			
			String xpath = ".//PrimaryExpression | .//AllocationExpression";
			
			List lists = null;
			try
			{
				lists = ((SimpleJavaNode)o).findChildNodesWithXPath(xpath);
			} 
			catch (JaxenException e)
			{
				e.printStackTrace();
			}
			
			if(lists == null)
			{
				continue;
			}
			
			lists.add(o);

			for(int j=0; j<lists.size(); j++)
			{
				Object obj = lists.get(j);
				
				if (tainted.getTagTreeNode() == obj) {
					Object pr = null;
					Object type = null;
					
					if(obj instanceof ASTPrimaryExpression)
					{
						ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
						
						/*
						 * 获取type
						 */
						pr = pe.jjtGetChild(pe.jjtGetNumChildren() - 2);
					}
					else if(obj instanceof ASTAllocationExpression)
					{
						pr = obj;
					}
					
					if ((pr instanceof ASTPrimaryPrefix)
							|| (pr instanceof ASTPrimarySuffix)
							|| (pr instanceof ASTAllocationExpression))
					{
						type = ((ExpressionBase) pr).getType();
					}
					
					fsmin.setResultString(type.toString());
					
					return true;
				}
			}
			

		}
		return false;
	}
}
