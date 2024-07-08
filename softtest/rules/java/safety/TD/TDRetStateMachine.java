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
 * @author ��ƽ��
 * 
 */
public class TDRetStateMachine extends AbstractStateMachine
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
		//listeners.addListener(InputFeatureListener.getInstance());
		//listeners.addListener(SensitiveUseFeatureListener.getInstance());
	}

	/**
	 * ����״̬��ʵ��������Ϊÿ�����뺯������һ��״̬��ʵ�����������Ⱦ�ı�������
	 * 
	 * 1 ��λ���ض����﷨ģʽ�� f( g() ); o.f( g() ); this.f( g() ); h().f( g() ); o.h().f(
	 * g() ); if( f( g() ) ){...} 2 ��ÿ���������ý����жϣ� 3 ����ú�����Ϊ���뺯����continue; 4
	 * ����ú�����Ⱦ�ı������Ƿ���ֵ��continue; 5 ����ú����ⲻ����1�׸��������ã�continue; 6
	 * ���1�׸��������ǻ�㺯����continue; 7 ���1�׸����������б�����Ų��Ǹú���������ţ�continue; 8 return
	 * true;
	 * 
	 * ��ζ�λ�������� PrimarySuffix[@Arguments='true']
	 * 
	 * ��ζ�λ���������� .//PrimaryPrefix[@MethodName='true'] e.g : f()
	 * .//PrimarySuffix[@MethodName='true'] e.g : this.f()
	 * 
	 * ��ζ�λ��1�׸������� o = getFirstParentOf()�� ������﷨Ԫ�ؽ��к��������֤��
	 * 
	 * ��μ�¼�Ӻ����ڲ����б��е���ţ� ���������㷨�Ľ����£�
	 * 
	 * 1 ��λ���ض����﷨ģʽ�� f(),o.f() ; this.f(); h().f(), o.h().f(); if( f()
	 * ){......}; 2 ��ÿ���������ý����жϣ� 3 ����ú������ǻ�㺯����continue; 4 �����û�㺯���������������У� 5
	 * ��������б�i�����Ǻ������ã�continue; 6 ��������б�i���������뺯����continue; 7
	 * ��������б�i�������뺯��δ�Է���ֵ��Ⱦ��continue;
	 */
	public static List<FSMMachineInstance> createTDRetStateMachines(
			SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		/*
		 * 1 ��λ���ض����﷨ģʽ f() || o.f() :
		 * .//PrimaryExpression/PrimaryPrefix[@MethodName='true'] this.f()
		 * this.h().g() h().f() o.h().f() :
		 * .//PrimaryExpression/PrimarySuffix[@MethodName='true'] if(f()){...} :
		 * ͬ�� new A(); .//AllocationExpression[./Arguments]
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
					 * ��ȡtype
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
//				 * ��ȡtype
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
				 * 2 ����ú������ǻ�㺯����continue;
				 */
				MethodNode methodnode = MethodNode.findMethodNode(type);
				if (methodnode != null)
				// �Զ��庯��
				{
					MethodSummary summary = methodnode.getMethodsummary();

					if (summary != null)
					{
						for (AbstractFeature ff : summary.getFeatrues().getTable().values())
						{
							// ��ʾ�ú������ǻ�㺯��
							if (!(ff instanceof SinkFeature))
							{
								continue;
							}

							SinkFeature sf = (SinkFeature) ff;

							// ��ȡ��㺯���Ĳ����б�
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
							// �����б�
							ASTArgumentList argList = (ASTArgumentList) args
									.jjtGetChild(0);

							if (argList == null)
							{
								continue;
							}

							/*
							 * 3 �����û�㺯����������������
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
								
								// ��λ���ò���
								if (argList.jjtGetChild(index - 1) == null)
								{
									continue;
								}

								ASTExpression exp = (ASTExpression) argList
										.jjtGetChild(index - 1);

								/*
								 * 4 �ñ��ʽ���Ƿ�����������ã����û��,��ôcontinue;
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
									 * ��ȡtype
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
									 * 6 ����ú����������뺯��,continue
									 */
									MethodNode inner_methodnode = MethodNode.findMethodNode(inner_type);
									if (inner_methodnode != null)
									// �Զ��庯��
									{
										MethodSummary inner_summary = inner_methodnode.getMethodsummary();
										
										if (inner_summary != null)
										{
											for (AbstractFeature inner_ff : inner_summary.getFeatrues()
													.getTable().values())
											{
												// ��ʾ�ú����������뺯��
												if (!(inner_ff instanceof InputFeature))
												{
													continue;
												}
												InputFeature inerif = (InputFeature) inner_ff;
												/*
												 * 7 ���������뺯����������Ⱦ����
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
													 * 8 ����״̬��
													 */
													FSMMachineInstance fsminstance = fsm.creatInstance();
													
//													 ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
													TaintedSet tainted = new TaintedSet();
													// ���ñ�ǽڵ�
													tainted.setTagTreeNode((SimpleJavaNode)limppa);
													fsminstance.setRelatedObject(tainted);
													list.add(fsminstance);
												}
											}
										}
									}
									else
									//�⺯��
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
											 * 8 ����״̬��
											 */
											FSMMachineInstance fsminstance = fsm.creatInstance();
											
//											 ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
											TaintedSet tainted = new TaintedSet();
											// ���ñ�ǽڵ�
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
				// �⺯��
				{
					String key = type.toString();
					
					SensitiveInfo sensitiveInfo = SinkFeature.useTable.get(key);
					
					if(sensitiveInfo == null)
					{
						continue;
					}
					
//					 ��ȡ��㺯���Ĳ����б�
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

					// �����б�
					ASTArgumentList argList = (ASTArgumentList) args
							.jjtGetChild(0);

					if (argList == null)
					{
						continue;
					}

					/*
					 * 3 �����û�㺯����������������
					 */
					List seqs = sensitiveInfo.getSensitiveSeqs();
					for (int q = 0; q<seqs.size(); q++ )
					{
						int index = Integer.parseInt((String) seqs.get(q));
						
						if(argList.jjtGetNumChildren() < index)
						{
							continue;
						}

						// ��λ���ò���
						if (argList.jjtGetChild(index - 1) == null)
						{
							continue;
						}

						ASTExpression exp = (ASTExpression) argList
								.jjtGetChild(index - 1);

						/*
						 * 4 �ñ��ʽ���Ƿ�����������ã����û��,��ôcontinue;
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
							 * ��ȡtype
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
							 * 6 ����ú����������뺯��,continue
							 */
							MethodNode inner_methodnode = MethodNode.findMethodNode(inner_type);
							if (inner_methodnode != null)
							// �Զ��庯��
							{
								MethodSummary inner_summary = inner_methodnode.getMethodsummary();
								
								if (inner_summary != null)
								{
									for (AbstractFeature inner_ff : inner_summary.getFeatrues()
											.getTable().values())
									{
										// ��ʾ�ú����������뺯��
										if (!(inner_ff instanceof InputFeature))
										{
											continue;
										}
										InputFeature inerif = (InputFeature) inner_ff;
										/*
										 * 7 ���������뺯����������Ⱦ����
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
											 * 8 ����״̬��
											 */
											FSMMachineInstance fsminstance = fsm.creatInstance();
											
//											 ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
											TaintedSet tainted = new TaintedSet();
											// ���ñ�ǽڵ�
											tainted.setTagTreeNode((SimpleJavaNode)limppa);
											fsminstance.setRelatedObject(tainted);
											list.add(fsminstance);
										}
									}
								}
							}
							else
							//�⺯��
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
									 * 8 ����״̬��
									 */
									FSMMachineInstance fsminstance = fsm.creatInstance();
									
//									 ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
									TaintedSet tainted = new TaintedSet();
									// ���ñ�ǽڵ�
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
		 * 1 ��λ���ض���ģʽ�������ʼ��
		 * 2 ��ʼ��
		 */
		
		String xpath = ".//AllocationExpression/ArrayDimsAndInits/Expression/PrimaryExpression[count(*) >= 2]";
		
		/*
		 * 1 ��λ���ض���ģʽ
		 */
		try
		{
			List arrs = node.findChildNodesWithXPath(xpath);
			
			/*
			 * 2 �������е�candidate
			 */
			if(arrs.size() != 0)
			{
				Iterator it = arrs.iterator();
				
				while(it.hasNext())
				{
					ASTPrimaryExpression pe = (ASTPrimaryExpression) it.next();
					
					/*
					 * 3 ��ȡinnner������type
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
							// �Զ��庯��
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
										 * 4 ����״̬��
										 */
										FSMMachineInstance fsminstance = fsm.creatInstance();
										
										//��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
										TaintedSet tainted = new TaintedSet();
										// ���ñ�ǽڵ�
										tainted.setTagTreeNode(pe);
										fsminstance.setRelatedObject(tainted);
										list.add(fsminstance);
									}
								}
							}
						}
						else
							//�⺯��
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
									 * 4 ����״̬��
									 */
									FSMMachineInstance fsminstance = fsm.creatInstance();
									
									//��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
									TaintedSet tainted = new TaintedSet();
									// ���ñ�ǽڵ�
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
						 * ��ȡtype
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
