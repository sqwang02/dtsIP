/**
 * 
 */
package softtest.rules.java.safety.TD;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.lang.reflect.Method;
import java.util.*;

import org.jaxen.*;

import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.method.AbstractFeature;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.InputFeature;
import softtest.callgraph.java.method.InputFeatureListener;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.callgraph.java.method.SinkFeatureListener;
import softtest.cfg.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.summary.lib.java.TaintedInfo;
import softtest.symboltable.java.*;

public class TDStateMachine extends AbstractStateMachine
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
		listeners.addListener(InputFeatureListener.getInstance());
		// listeners.addListener(SensitiveUseFeatureListener.getInstance());
		listeners.addListener(SinkFeatureListener.getInstance());
	}

	/** 在节点node上查找xPath */
	private static List findTreeNodes(SimpleNode node, String xPath)
	{
		List evaluationResults = new ArrayList();
		try
		{
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e)
		{
			// e.printStackTrace();
			throw new RuntimeException("xpath error", e);
		}
		return evaluationResults;
	}
	
//	//[@Arguments=\'true\']
//	/*
//	 * String xpath1 = "//VariableDeclarator[@VariableDeclaratorId=\'true\']/VariableInitializer/Expression/PrimaryExpression[./PrimaryPrefix[@MethodName=\'true\'] or ./PrimarySuffix[@MethodName=\'true\']]";
//	 * String xpath2 = "//VariableDeclarator[@VariableDeclaratorId=\'true\']/VariableInitializer/Expression/PrimaryExpression[./PrimaryPrefix[@MethodName=\'true\'] or ./PrimarySuffix[@MethodName=\'true\']]";
//	 * String xpath1 = "//VariableDeclarator[@VariableDeclaratorId=\'true\']/VariableInitializer/Expression/PrimaryExpression[./PrimaryPrefix[@MethodName=\'true\'] or ./PrimarySuffix[@MethodName=\'true\']]";
//	 */
//	public List<FSMMachineInstance> backup(SimpleJavaNode node, FSMMachine fsm)
//	{
//		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
//		
//		
//	}

	/** 创建状态集实例函数，为每个输入函数创建一个状态机实例，跟踪其感染的变量集合 */
	public static List<FSMMachineInstance> createTDStateMachines(
			SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		/*
		 * 1 潜在的函数调用表达式
		 */
		List evalRets = null;
		evalRets = findTreeNodes(node, ".//PrimaryExpression");

		for (int p = 0; p < evalRets.size(); p++)
		{
			ASTPrimaryExpression pe = (ASTPrimaryExpression) evalRets.get(p);

			/*
			 * 2 保证该节点是函数调用节点。
			 */
			if (!(pe == null || pe.jjtGetNumChildren() < 2))
			{

				/*
				 * 获取子函数的实参列表
				 */
				// ASTPrimarySuffix ps =
				// (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
				/*
				 * 获取type
				 */
				// Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
				// Object type = null;
				// if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof
				// ASTPrimarySuffix) ) {
				// type = ((ExpressionBase)pr).getType();
				// }
				Object type = null;

				for (int q = 0; q < pe.jjtGetNumChildren(); q++)
				{
					Object pr = pe.jjtGetChild(q);
					if ((pr instanceof ASTPrimaryPrefix)
							|| (pr instanceof ASTPrimarySuffix))
					{
						type = ((ExpressionBase) pr).getType();
					}

					/*
					 * 3 创建状态机
					 */
					if (type != null && (type instanceof Method))
					{
						MethodNode methodnode = MethodNode.findMethodNode(type);
						if (methodnode != null)
						{
							// 3.1 如果该函数为自定义函数
							MethodSummary summary = methodnode
									.getMethodsummary();

							if (summary != null)
							{
								for (AbstractFeature ff : summary.getFeatrues()
										.getTable().values())
								{
									if (!(ff instanceof InputFeature))
									{
										continue;
									}
									InputFeature rf = (InputFeature) ff;

									for (Map.Entry<Integer, List<String>> e : rf
											.getMapInfo().entrySet())
									{

										Integer ide = e.getKey();

										int index = ide.intValue();

										if (index == 0)
										/*
										 * 3.1.1 表示是返回值 e.g 1: String s =
										 * o.readLine(); e.g 2: String s = null;
										 * s = o.readLine(); e.g 3: String s =
										 * null; if((s=o.readLine())!=null){}
										 */

										{
											ASTBlockStatement bs = (ASTBlockStatement) pe
													.getFirstParentOfType(ASTBlockStatement.class);

											String case1 = ".//VariableDeclaratorId";
											String case2 = ".//StatementExpression[./AssignmentOperator[@Image=\'=\']]/PrimaryExpression/PrimaryPrefix/Name";
											String case3 = ".//Expression[./AssignmentOperator[@Image=\'=\']]/PrimaryExpression/PrimaryPrefix/Name";

											try
											{
												List rets1 = bs
														.findChildNodesWithXPath(case1);
												List rets2 = bs
														.findChildNodesWithXPath(case2);
												List rets3 = bs
														.findChildNodesWithXPath(case3);
												/*
												 * case1 : 创建状态机
												 */
												Iterator it = rets1.iterator();
												while (it.hasNext())
												{

													ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) it
															.next();
													
													ASTVariableDeclarator idfa = (ASTVariableDeclarator) id.getFirstParentOfType(ASTVariableDeclarator.class);
													if(idfa == null) continue;
													List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
													int flag = 0;
													Iterator itCheck = checkList.iterator();
													
													while(itCheck.hasNext())
													{
														Object cmp = itCheck.next();
														
														if(cmp == pe)
														{
															flag = 1;
															break;
														}
													}
													
													if(flag == 0)
													{
														continue;
													}
													
													// System.out.println("自定义函数的返回值收到污染，创建状态机
													// ： " + id.getImage());
													FSMMachineInstance fsminstance = fsm
															.creatInstance();
													// 创建被感染的变量集合，不添加集合，在Inputed处添加
													TaintedSet tainted = new TaintedSet();
													// 设置标记节点
													tainted.setTagTreeNode(id);
													fsminstance
															.setRelatedObject(tainted);
													if (!id
															.hasLocalMethod(node))
													{
														list.add(fsminstance);
													}
												}

												/*
												 * case2 : 创建状态机
												 */
												it = rets2.iterator();
												while (it.hasNext())
												{

													ASTName name = (ASTName) it
															.next();
													
													ASTStatementExpression idfa = (ASTStatementExpression) name.getFirstParentOfType(ASTStatementExpression.class);
													if(idfa == null) continue;
													List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
													int flag = 0;
													Iterator itCheck = checkList.iterator();
													
													while(itCheck.hasNext())
													{
														Object cmp = itCheck.next();
														
														if(cmp == pe)
														{
															flag = 1;
															break;
														}
													}
													
													if(flag == 0)
													{
														continue;
													}
													
													FSMMachineInstance fsminstance = fsm
															.creatInstance();
													TaintedSet tainted = new TaintedSet();
													// 不添加集合，在Inputed处添加

													tainted
															.setTagTreeNode(name);
													fsminstance
															.setRelatedObject(tainted);
													if (!name
															.hasLocalMethod(node))
													{
														list.add(fsminstance);
													}
												}

												/*
												 * case3 : 创建状态机
												 */
												it = rets3.iterator();
												while (it.hasNext())
												{
													ASTName name = (ASTName) it
															.next();
													
													ASTExpression idfa = (ASTExpression) name.getFirstParentOfType(ASTExpression.class);
													if(idfa == null) continue;
													List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
													int flag = 0;
													Iterator itCheck = checkList.iterator();
													
													while(itCheck.hasNext())
													{
														Object cmp = itCheck.next();
														
														if(cmp == pe)
														{
															flag = 1;
															break;
														}
													}
													
													if(flag == 0)
													{
														continue;
													}
													
													FSMMachineInstance fsminstance = fsm
															.creatInstance();
													TaintedSet tainted = new TaintedSet();
													// 不添加集合，在Inputed处添加

													tainted
															.setTagTreeNode(name);
													fsminstance
															.setRelatedObject(tainted);
													if (!name
															.hasLocalMethod(node))
													{
														list.add(fsminstance);
													}
												}
											} catch (JaxenException e1)
											{
												e1.printStackTrace();
											}

										}// 返回值被污染的情况 end
//										else
//										/*
//										 * 3.1.2 表示是参数值 e.g 1: o.read(buf);
//										 */
//										{
//											/*
//											 * 获取子函数的实参列表
//											 */
//											ASTPrimarySuffix aps = (ASTPrimarySuffix) pe
//													.jjtGetChild(pe
//															.jjtGetNumChildren() - 1);
//											ASTArguments aargs = (ASTArguments) aps
//													.jjtGetChild(0);
//											ASTExpression aexpr = (ASTExpression) aargs
//													.jjtGetChild(0)
//													.jjtGetChild(index - 1);
//
//											String case1 = ".//PrimaryExpression/PrimaryPrefix/Name";
//											try
//											{
//												/*
//												 * case1 : 创建状态机
//												 */
//												List rets1 = aexpr
//														.findChildNodesWithXPath(case1);
//												Iterator it = rets1.iterator();
//												while (it.hasNext())
//												{
//													ASTName name = (ASTName) it
//															.next();
//													
////													System.out
////															.println("创建状态机 " + name.getImage());
//													
//													FSMMachineInstance fsminstance = fsm
//															.creatInstance();
//													TaintedSet tainted = new TaintedSet();
//													// 不添加集合，在Inputed处添加
//
//													tainted
//															.setTagTreeNode(name);
//													fsminstance
//															.setRelatedObject(tainted);
//													if (!name
//															.hasLocalMethod(node))
//													{
//														list.add(fsminstance);
//													}
//												}
//
//											} catch (JaxenException e1)
//											{
//												e1.printStackTrace();
//											}
//										}// 参数值被污染的情况 end

									}// 函数摘要中每一个参数处理 end

								}// 获取InputFeature end
							}// 存在函数摘要 end
						}// 存在函数节点 end
						else
						/*
						 * 3.2 如果该函数为JDK库函数 查JDK方法表，是否为输入函数
						 */
						{
							String key = type.toString();
							
							//System.out.println(key);

							TaintedInfo taintedInfo = InputFeature.inputTable.get(key);

							if (taintedInfo != null)
							{
								List seqs = taintedInfo.getTaintedSeqs();
								/**
								 * 调试
								 */
								for (int j = 0; j < seqs.size(); j++)
								{
									int kj = Integer.parseInt((String) seqs
											.get(j));
									Integer k = Integer.valueOf(kj);

									if (k.equals(0))
									/*
									 * 3.2.1 表示是返回值 e.g 1: String s =
									 * o.readLine(); e.g 2: String s = null; s =
									 * o.readLine(); e.g 3: String s = null;
									 * if((s=o.readLine())!=null){}
									 */
									{
										ASTBlockStatement bs = (ASTBlockStatement) pe
												.getFirstParentOfType(ASTBlockStatement.class);
										
										//"./LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId"
										//"./Statement/StatementExpression[./AssignmentOperator[@Image=\'=\']]/PrimaryExpression/PrimaryPrefix/Name"
										String case1 = ".//VariableDeclaratorId";
										String case2 = ".//StatementExpression[./AssignmentOperator[@Image=\'=\']]/PrimaryExpression/PrimaryPrefix/Name";
										String case3 = ".//Expression[./AssignmentOperator[@Image=\'=\']]/PrimaryExpression/PrimaryPrefix/Name";

										try
										{
											if (bs == null)
											{
												continue;
											}
											List rets1 = bs
													.findChildNodesWithXPath(case1);
											List rets2 = bs
													.findChildNodesWithXPath(case2);
											List rets3 = bs
													.findChildNodesWithXPath(case3);
											/*
											 * case1 : 创建状态机
											 */
											Iterator it = rets1.iterator();
											while (it.hasNext())
											{
												ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) it
														.next();
												
												ASTVariableDeclarator idfa = (ASTVariableDeclarator) id.getFirstParentOfType(ASTVariableDeclarator.class);
												if(idfa == null) continue;
												List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
												int flag = 0;
												Iterator itCheck = checkList.iterator();
												
												while(itCheck.hasNext())
												{
													Object cmp = itCheck.next();
													
													if(cmp == pe)
													{
														flag = 1;
														break;
													}
												}
												
												if(flag == 0)
												{
													continue;
												}
												
//												System.out.println("库函数的返回值收到污染，创建状态机： " + id.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();
												// 创建被感染的变量集合，不添加集合，在Inputed处添加
												TaintedSet tainted = new TaintedSet();
												// 设置标记节点
												tainted.setTagTreeNode(id);
												fsminstance
														.setRelatedObject(tainted);
												if (!id.hasLocalMethod(node))
												{
													list.add(fsminstance);
												}
											}

											/*
											 * case2 : 创建状态机
											 */
											it = rets2.iterator();
											while (it.hasNext())
											{
												ASTName name = (ASTName) it
														.next();
												
												ASTStatementExpression idfa = (ASTStatementExpression) name.getFirstParentOfType(ASTStatementExpression.class);
												if(idfa == null) continue;
												List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
												int flag = 0;
												Iterator itCheck = checkList.iterator();
												
												while(itCheck.hasNext())
												{
													Object cmp = itCheck.next();
													
													if(cmp == pe)
													{
														flag = 1;
														break;
													}
												}
												
												if(flag == 0)
												{
													continue;
												}
												
//												 System.out.println("库函数的返回值收到污染，创建状态机： " + name.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();

												TaintedSet tainted = new TaintedSet();
												// 不添加集合，在Inputed处添加

												tainted.setTagTreeNode(name);
												fsminstance
														.setRelatedObject(tainted);
												if (!name.hasLocalMethod(node))
												{
													list.add(fsminstance);
												}
											}

											/*
											 * case3 : 创建状态机
											 */
											it = rets3.iterator();
											while (it.hasNext())
											{
												ASTName name = (ASTName) it
														.next();
												
												ASTExpression idfa = (ASTExpression) name.getFirstParentOfType(ASTExpression.class);
												if(idfa == null) continue;
												List checkList = idfa.findChildNodesWithXPath(".//PrimaryExpression");
												int flag = 0;
												Iterator itCheck = checkList.iterator();
												
												while(itCheck.hasNext())
												{
													Object cmp = itCheck.next();
													
													if(cmp == pe)
													{
														flag = 1;
														break;
													}
												}
												
												if(flag == 0)
												{
													continue;
												}
//												 System.out.println("库函数的返回值收到污染，创建状态机： " + name.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();

												TaintedSet tainted = new TaintedSet();
												// 不添加集合，在Inputed处添加

												tainted.setTagTreeNode(name);
												fsminstance
														.setRelatedObject(tainted);
												if (!name.hasLocalMethod(node))
												{
													list.add(fsminstance);
												}
											}
										} catch (JaxenException e1)
										{
											e1.printStackTrace();
										}

									}// 返回值被污染的情况 end
//									else
//									/*
//									 * 3.2.2 表示是参数值 e.g 1: o.read(buf);
//									 */
//									{
//
//										/*
//										 * 获取子函数的实参列表
//										 */
//										ASTPrimarySuffix aps = (ASTPrimarySuffix) pe
//												.jjtGetChild(pe
//														.jjtGetNumChildren() - 1);
//										ASTArguments aargs = (ASTArguments) aps
//												.jjtGetChild(0);
//										ASTExpression aexpr = (ASTExpression) aargs
//												.jjtGetChild(0).jjtGetChild(
//														kj - 1);
//
//										String case1 = ".//PrimaryExpression/PrimaryPrefix/Name";
//										try
//										{
//											/*
//											 * case1 : 创建状态机
//											 */
//											List rets1 = aexpr
//													.findChildNodesWithXPath(case1);
//											Iterator it = rets1.iterator();
//											while (it.hasNext())
//											{
//												ASTName name = (ASTName) it
//														.next();
////												 System.out.println("库函数的参数值收到污染，创建状态机： " + name.getImage());
//												
//												FSMMachineInstance fsminstance = fsm
//														.creatInstance();
//
//												TaintedSet tainted = new TaintedSet();
//												// 不添加集合，在Inputed处添加
//
//												tainted.setTagTreeNode(name);
//												fsminstance
//														.setRelatedObject(tainted);
//												if (!name.hasLocalMethod(node))
//												{
//													list.add(fsminstance);
//												}
//											}
//
//										} catch (JaxenException e1)
//										{
//											e1.printStackTrace();
//										}
//									}// 参数值被污染的情况 end

								}
							}
							// else
							// //如果JDK库中没有录入该函数的摘要信息，则不处理
							// {
							// }
						}
					}
				}
			}
		}// 保证该节点是函数调用节点 end
		// 所有潜在的函数调用处理 end

		if (list.size() != 0)
		{
			return list;
		}
		/**
		 * 上一版本的识别方式
		 */
		// 在句柄变量声明时，输入
		/**
		 * 形如： String p=getParameter("name");
		 */
		String xPath = "";
		StringBuffer buffer = new StringBuffer(
				".//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
		xPath = addInputString(buffer);
		xPath += ")$\')] and ./PrimarySuffix[@Arguments=\'true\']]]";
		List evaluationResults = null;

		evaluationResults = findTreeNodes(node, xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext())
		{
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			// 创建被感染的变量集合，不添加集合，在Inputed处添加
			TaintedSet tainted = new TaintedSet();
			// 设置标记节点
			tainted.setTagTreeNode(id);
			fsminstance.setRelatedObject(tainted);
			if (!id.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		// 不在在句柄变量声明时，输入，表达式语句
		/**
		 * 形如： String p=null; p=getParameter("name");
		 */
		buffer = new StringBuffer(
				"..//StatementExpression[./AssignmentOperator[@Image=\'=\'] and ./Expression//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
		xPath = addInputString(buffer);
		xPath += ")$\')] and ./PrimarySuffix[@Arguments=\'true\'] ] ]/PrimaryExpression/PrimaryPrefix/Name";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();

			TaintedSet tainted = new TaintedSet();
			// 不添加集合，在Inputed处添加

			tainted.setTagTreeNode(name);
			fsminstance.setRelatedObject(tainted);
			if (!name.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		// 表达式
		/**
		 * 形如： String p=null; if((p=getParameter("name"))!=null){}
		 */
		buffer = new StringBuffer(
				".//Expression[./AssignmentOperator[@Image=\'=\'] and ./Expression//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
		xPath = addInputString(buffer);
		xPath += ")$\')] and ./PrimarySuffix[@Arguments=\'true\'] ] ]/PrimaryExpression/PrimaryPrefix/Name";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();

			TaintedSet tainted = new TaintedSet();
			// 不添加集合，在Inputed处添加

			tainted.setTagTreeNode(name);
			fsminstance.setRelatedObject(tainted);
			if (!name.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		return list;
	}

	/** 检查是否为同一个input函数 */
	public static boolean checkSameInput(List nodes, FSMMachineInstance fsmin)
	{
		Iterator i = nodes.iterator();
		while (i.hasNext())
		{
			Object o = i.next();
			TaintedSet tainted = (TaintedSet) fsmin.getRelatedObject();
			if (tainted.getTagTreeNode() == o)
			{
				// 添加集合
				if (o instanceof ASTVariableDeclaratorId)
				{
					ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) o;
					VariableNameDeclaration v = (VariableNameDeclaration) id
							.getNameDeclaration();
					//jdh
					if(v==null)
						return false;
					
					tainted.add(v);
				} else if (o instanceof ASTName)
				{
					ASTName name = (ASTName) o;
					VariableNameDeclaration v = (VariableNameDeclaration) name
							.getNameDeclaration();
					//jdh
					if(v==null)
						return false;
					
					tainted.add(v);
				}
				return true;
			}
		}
		return false;
	}

	/** 检查当前被感染的变量集合是否为空集 */
	public static boolean checkTaintedSetEmpty(VexNode vex,
			FSMMachineInstance fsmin)
	{
		TaintedSet tainted = (TaintedSet) fsmin.getRelatedObject();
		if (tainted.isEmpty())
		{
			return true;
		}
		return false;
	}

	/** 检查当前节点是否使用了被感染变量集合中的变量 */
	public static boolean checkTaintedDataUsed(VexNode vex,
			FSMMachineInstance fsmin)
	{
		TaintedSet tainted = (TaintedSet) fsmin.getRelatedObject();
		if (tainted.checkUsed(vex, fsmin))
		{
			return true;
		}
		return false;
	}

	/** 能被识别的输入函数名称集合 */
	private static String[] INPUT_STRINGS = { "getParameter" };

	/** 在正则表达式中添加输入函数名称集合 */
	private static String addInputString(StringBuffer buffer)
	{
		for (String s : INPUT_STRINGS)
		{
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (INPUT_STRINGS.length > 0)
		{
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
}