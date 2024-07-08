/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import softtest.callgraph.java.method.InputFeature;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.summary.lib.java.TaintedInfo;

/**
 * @author ��ƽ��
 *
 * String p = getParameter("name");
 * String p = o.getParameter("name");
 * String p = f().getParameter("name");
 * String p = o.f().getParameter("name");
 * String p = this.g().getParameter("name");
 * .//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@Arguments='true']]]
 */
public class ReturnTDCreator
{
	/** ����״̬��ʵ��������Ϊÿ�����뺯������һ��״̬��ʵ�����������Ⱦ�ı������� */
	public static List<FSMMachineInstance> createTDStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		System.out.println("break 1");
		/*
		 * 1 ״̬��ʵ������
		 */
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/*
		 * 2 ��ģʽ��Xpath
		 */
		String xPath = ".//PrimaryExpression[./PrimarySuffix[@Arguments='true']]";
		List evaluationResults = null;
		
		/*
		 * 3 ��ȡ������type��Ϣ
		 */
		evaluationResults = TDCreatorTools.findTreeNodes(node, xPath);
		System.out.println("evaluationResults.size() = " + evaluationResults.size());
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) 
		{
			ASTPrimaryExpression pe = (ASTPrimaryExpression)i.next();
			if(!(pe == null || pe.jjtGetNumChildren()<2)) 
			{
				continue;
			}
			Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
			Object type = null;
			if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
				type = ((ExpressionBase)pr).getType();
			} 
			
			if(type == null || !(type instanceof Method) )
			{
				continue;
			}
			System.out.println(type.toString());
			/*
			 * 4 ����type������Ӧ��״̬��
			 */
			MethodNode methodnode=MethodNode.findMethodNode(type);
			
			if(methodnode != null)
				/*
				 * 4.1 ����ú���Ϊ�Զ��庯��
				 */
			{
				//��ȡ�ú�����ժҪ��Ϣ
				MethodSummary summary = methodnode.getMethodsummary();
				
				if (summary != null)
					//����ú�����ժҪ��Ϣ��Ϊ��
				{
					for (AbstractFeature ff : summary.getFeatrues().getTable().values()) 
					{
						if (!(ff instanceof InputFeature)) 
						{
							//�����ú������е�������ֱ���ҵ�InputFeatureΪֹ
							continue;
						}
						InputFeature rf = (InputFeature) ff;
						
						for (Map.Entry<Integer,List<String>> e : rf.getMapInfo().entrySet())
							//������InputFeature�е�����Ԫ��
						{
							//��ȡ��Ԫ���key
							Integer ide = e.getKey();
							int index = ide.intValue();
							
							if(index == 0)
								//��ʾ�Ƿ���ֵ����Ⱦ
							{
								//��ȡ����ֵ����
								String case1 = ".//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@Arguments='true']]]";
								List rets = TDCreatorTools.findTreeNodes(node, case1);
								Iterator iter = rets.iterator();
								while (iter.hasNext()) {
									ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) iter.next();
									FSMMachineInstance fsminstance = fsm.creatInstance();
									// ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
									TaintedSet tainted = new TaintedSet();
									// ���ñ�ǽڵ�
									tainted.setTagTreeNode(id);
									fsminstance.setRelatedObject(tainted);
									if (!id.hasLocalMethod(node)) {
										list.add(fsminstance);
									}
								}
							}
							else
								//��ʾ�ǲ���ֵ����Ⱦ
							{
								ASTPrimarySuffix aps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
								ASTArguments aargs = (ASTArguments) aps.jjtGetChild(0);
								ASTExpression aexpr = (ASTExpression) aargs.jjtGetChild(0).jjtGetChild(index-1);
								
								String case1 = ".//PrimaryExpression/PrimaryPrefix/Name";
								List rets = TDCreatorTools.findTreeNodes(aexpr, case1);
								Iterator iter = rets.iterator();
								
								while (iter.hasNext()) {
									ASTName name = (ASTName) iter.next();
									FSMMachineInstance fsminstance = fsm.creatInstance();

									TaintedSet tainted = new TaintedSet();
									// ����Ӽ��ϣ���Inputed�����
									tainted.setTagTreeNode(name);
									fsminstance.setRelatedObject(tainted);
									if (!name.hasLocalMethod(node)) {
										list.add(fsminstance);
									}
								}
							}
						}
						
					}
				}
			}
			else
				/*
				 * 4.2 ����ú���ΪJKD�⺯��
				 */
			{
				//��ȡ�ú�����type��Ϣ
				String key = type.toString();
				
				TaintedInfo taintedInfo = InputFeature.inputTable.get(key);
				
				if(taintedInfo == null)
				{
					continue;
				}
				
				//��ȡJDK�⺯����Ԫ��ؼ���
				List seqs = taintedInfo.getTaintedSeqs();
				
				for(int j=0; j<seqs.size(); j++)
				{
					int kj = Integer.parseInt((String) seqs.get(j));
					Integer k = Integer.valueOf(kj);
					if(k.equals(0))
						//��ʾ�Ƿ���ֵ����Ⱦ
					{
						//��ȡ����ֵ����
						String case1 = ".//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@Arguments='true']]]";
						List rets = TDCreatorTools.findTreeNodes(node, case1);
						Iterator iter = rets.iterator();
						while (iter.hasNext()) {
							ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) iter.next();
							FSMMachineInstance fsminstance = fsm.creatInstance();
							// ��������Ⱦ�ı������ϣ�����Ӽ��ϣ���Inputed�����
							TaintedSet tainted = new TaintedSet();
							// ���ñ�ǽڵ�
							tainted.setTagTreeNode(id);
							fsminstance.setRelatedObject(tainted);
							if (!id.hasLocalMethod(node)) {
								list.add(fsminstance);
							}
						}
					}
					else
						//��ʾ�ǲ���ֵ����Ⱦ
					{
						ASTPrimarySuffix aps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
						ASTArguments aargs = (ASTArguments) aps.jjtGetChild(0);
						ASTExpression aexpr = (ASTExpression) aargs.jjtGetChild(0).jjtGetChild(kj-1);
						
						String case1 = ".//PrimaryExpression/PrimaryPrefix/Name";
						List rets = TDCreatorTools.findTreeNodes(aexpr, case1);
						Iterator iter = rets.iterator();
						
						while (iter.hasNext()) {
							ASTName name = (ASTName) iter.next();
							FSMMachineInstance fsminstance = fsm.creatInstance();

							TaintedSet tainted = new TaintedSet();
							// ����Ӽ��ϣ���Inputed�����
							tainted.setTagTreeNode(name);
							fsminstance.setRelatedObject(tainted);
							if (!name.hasLocalMethod(node)) {
								list.add(fsminstance);
							}
						}
					}
				}
				
			}
		}
		return list;
	}
}
