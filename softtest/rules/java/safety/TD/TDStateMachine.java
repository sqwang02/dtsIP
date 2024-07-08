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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline)
	{
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0)
		{
			f.format("δ��֤������: ���� \'%s\' ���������� %d �е��ⲿ���룬�� %d ���Ͻ���ʹ��ǰ��û�н������ݺϷ��Լ�飬�������һ��©��",
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

	/** �ڽڵ�node�ϲ���xPath */
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

	/** ����״̬��ʵ��������Ϊÿ�����뺯������һ��״̬��ʵ�����������Ⱦ�ı������� */
	public static List<FSMMachineInstance> createTDStateMachines(
			SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		/*
		 * 1 Ǳ�ڵĺ������ñ��ʽ
		 */
		List evalRets = null;
		evalRets = findTreeNodes(node, ".//PrimaryExpression");

		for (int p = 0; p < evalRets.size(); p++)
		{
			ASTPrimaryExpression pe = (ASTPrimaryExpression) evalRets.get(p);

			/*
			 * 2 ��֤�ýڵ��Ǻ������ýڵ㡣
			 */
			if (!(pe == null || pe.jjtGetNumChildren() < 2))
			{

				/*
				 * ��ȡ�Ӻ�����ʵ���б�
				 */
				// ASTPrimarySuffix ps =
				// (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
				/*
				 * ��ȡtype
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
					 * 3 ����״̬��
					 */
					if (type != null && (type instanceof Method))
					{
						MethodNode methodnode = MethodNode.findMethodNode(type);
						if (methodnode != null)
						{
							// 3.1 ����ú���Ϊ�Զ��庯��
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
										 * 3.1.1 ��ʾ�Ƿ���ֵ e.g 1: String s =
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
												 * case1 : ����״̬��
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
													
													// System.out.println("�Զ��庯���ķ���ֵ�յ���Ⱦ������״̬��
													// �� " + id.getImage());
													FSMMachineInstance fsminstance = fsm
															.creatInstance();
													// ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
													TaintedSet tainted = new TaintedSet();
													// ���ñ�ǽڵ�
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
												 * case2 : ����״̬��
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
													// ����Ӽ��ϣ���Inputed�����

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
												 * case3 : ����״̬��
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
													// ����Ӽ��ϣ���Inputed�����

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

										}// ����ֵ����Ⱦ����� end
//										else
//										/*
//										 * 3.1.2 ��ʾ�ǲ���ֵ e.g 1: o.read(buf);
//										 */
//										{
//											/*
//											 * ��ȡ�Ӻ�����ʵ���б�
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
//												 * case1 : ����״̬��
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
////															.println("����״̬�� " + name.getImage());
//													
//													FSMMachineInstance fsminstance = fsm
//															.creatInstance();
//													TaintedSet tainted = new TaintedSet();
//													// ����Ӽ��ϣ���Inputed�����
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
//										}// ����ֵ����Ⱦ����� end

									}// ����ժҪ��ÿһ���������� end

								}// ��ȡInputFeature end
							}// ���ں���ժҪ end
						}// ���ں����ڵ� end
						else
						/*
						 * 3.2 ����ú���ΪJDK�⺯�� ��JDK�������Ƿ�Ϊ���뺯��
						 */
						{
							String key = type.toString();
							
							//System.out.println(key);

							TaintedInfo taintedInfo = InputFeature.inputTable.get(key);

							if (taintedInfo != null)
							{
								List seqs = taintedInfo.getTaintedSeqs();
								/**
								 * ����
								 */
								for (int j = 0; j < seqs.size(); j++)
								{
									int kj = Integer.parseInt((String) seqs
											.get(j));
									Integer k = Integer.valueOf(kj);

									if (k.equals(0))
									/*
									 * 3.2.1 ��ʾ�Ƿ���ֵ e.g 1: String s =
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
											 * case1 : ����״̬��
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
												
//												System.out.println("�⺯���ķ���ֵ�յ���Ⱦ������״̬���� " + id.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();
												// ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
												TaintedSet tainted = new TaintedSet();
												// ���ñ�ǽڵ�
												tainted.setTagTreeNode(id);
												fsminstance
														.setRelatedObject(tainted);
												if (!id.hasLocalMethod(node))
												{
													list.add(fsminstance);
												}
											}

											/*
											 * case2 : ����״̬��
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
												
//												 System.out.println("�⺯���ķ���ֵ�յ���Ⱦ������״̬���� " + name.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();

												TaintedSet tainted = new TaintedSet();
												// ����Ӽ��ϣ���Inputed�����

												tainted.setTagTreeNode(name);
												fsminstance
														.setRelatedObject(tainted);
												if (!name.hasLocalMethod(node))
												{
													list.add(fsminstance);
												}
											}

											/*
											 * case3 : ����״̬��
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
//												 System.out.println("�⺯���ķ���ֵ�յ���Ⱦ������״̬���� " + name.getImage());
												FSMMachineInstance fsminstance = fsm
														.creatInstance();

												TaintedSet tainted = new TaintedSet();
												// ����Ӽ��ϣ���Inputed�����

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

									}// ����ֵ����Ⱦ����� end
//									else
//									/*
//									 * 3.2.2 ��ʾ�ǲ���ֵ e.g 1: o.read(buf);
//									 */
//									{
//
//										/*
//										 * ��ȡ�Ӻ�����ʵ���б�
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
//											 * case1 : ����״̬��
//											 */
//											List rets1 = aexpr
//													.findChildNodesWithXPath(case1);
//											Iterator it = rets1.iterator();
//											while (it.hasNext())
//											{
//												ASTName name = (ASTName) it
//														.next();
////												 System.out.println("�⺯���Ĳ���ֵ�յ���Ⱦ������״̬���� " + name.getImage());
//												
//												FSMMachineInstance fsminstance = fsm
//														.creatInstance();
//
//												TaintedSet tainted = new TaintedSet();
//												// ����Ӽ��ϣ���Inputed�����
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
//									}// ����ֵ����Ⱦ����� end

								}
							}
							// else
							// //���JDK����û��¼��ú�����ժҪ��Ϣ���򲻴���
							// {
							// }
						}
					}
				}
			}
		}// ��֤�ýڵ��Ǻ������ýڵ� end
		// ����Ǳ�ڵĺ������ô��� end

		if (list.size() != 0)
		{
			return list;
		}
		/**
		 * ��һ�汾��ʶ��ʽ
		 */
		// �ھ����������ʱ������
		/**
		 * ���磺 String p=getParameter("name");
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
			// ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
			TaintedSet tainted = new TaintedSet();
			// ���ñ�ǽڵ�
			tainted.setTagTreeNode(id);
			fsminstance.setRelatedObject(tainted);
			if (!id.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		// �����ھ����������ʱ�����룬���ʽ���
		/**
		 * ���磺 String p=null; p=getParameter("name");
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
			// ����Ӽ��ϣ���Inputed�����

			tainted.setTagTreeNode(name);
			fsminstance.setRelatedObject(tainted);
			if (!name.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		// ���ʽ
		/**
		 * ���磺 String p=null; if((p=getParameter("name"))!=null){}
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
			// ����Ӽ��ϣ���Inputed�����

			tainted.setTagTreeNode(name);
			fsminstance.setRelatedObject(tainted);
			if (!name.hasLocalMethod(node))
			{
				list.add(fsminstance);
			}
		}

		return list;
	}

	/** ����Ƿ�Ϊͬһ��input���� */
	public static boolean checkSameInput(List nodes, FSMMachineInstance fsmin)
	{
		Iterator i = nodes.iterator();
		while (i.hasNext())
		{
			Object o = i.next();
			TaintedSet tainted = (TaintedSet) fsmin.getRelatedObject();
			if (tainted.getTagTreeNode() == o)
			{
				// ��Ӽ���
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

	/** ��鵱ǰ����Ⱦ�ı��������Ƿ�Ϊ�ռ� */
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

	/** ��鵱ǰ�ڵ��Ƿ�ʹ���˱���Ⱦ���������еı��� */
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

	/** �ܱ�ʶ������뺯�����Ƽ��� */
	private static String[] INPUT_STRINGS = { "getParameter" };

	/** ��������ʽ��������뺯�����Ƽ��� */
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