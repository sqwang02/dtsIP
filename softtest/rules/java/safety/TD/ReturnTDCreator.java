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
 * @author 彭平雷
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
	/** 创建状态集实例函数，为每个输入函数创建一个状态机实例，跟踪其感染的变量集合 */
	public static List<FSMMachineInstance> createTDStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		System.out.println("break 1");
		/*
		 * 1 状态机实例容器
		 */
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/*
		 * 2 该模式的Xpath
		 */
		String xPath = ".//PrimaryExpression[./PrimarySuffix[@Arguments='true']]";
		List evaluationResults = null;
		
		/*
		 * 3 获取函数的type信息
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
			 * 4 根据type创建相应的状态机
			 */
			MethodNode methodnode=MethodNode.findMethodNode(type);
			
			if(methodnode != null)
				/*
				 * 4.1 如果该函数为自定义函数
				 */
			{
				//获取该函数的摘要信息
				MethodSummary summary = methodnode.getMethodsummary();
				
				if (summary != null)
					//如果该函数的摘要信息不为空
				{
					for (AbstractFeature ff : summary.getFeatrues().getTable().values()) 
					{
						if (!(ff instanceof InputFeature)) 
						{
							//遍历该函数所有的特征，直到找到InputFeature为止
							continue;
						}
						InputFeature rf = (InputFeature) ff;
						
						for (Map.Entry<Integer,List<String>> e : rf.getMapInfo().entrySet())
							//遍历该InputFeature中的所有元组
						{
							//获取该元组的key
							Integer ide = e.getKey();
							int index = ide.intValue();
							
							if(index == 0)
								//表示是返回值被污染
							{
								//获取返回值变量
								String case1 = ".//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@Arguments='true']]]";
								List rets = TDCreatorTools.findTreeNodes(node, case1);
								Iterator iter = rets.iterator();
								while (iter.hasNext()) {
									ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) iter.next();
									FSMMachineInstance fsminstance = fsm.creatInstance();
									// 创建被感染的变量集合，不添加集合，在Inputed处添加
									TaintedSet tainted = new TaintedSet();
									// 设置标记节点
									tainted.setTagTreeNode(id);
									fsminstance.setRelatedObject(tainted);
									if (!id.hasLocalMethod(node)) {
										list.add(fsminstance);
									}
								}
							}
							else
								//表示是参数值被污染
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
									// 不添加集合，在Inputed处添加
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
				 * 4.2 如果该函数为JKD库函数
				 */
			{
				//获取该函数的type信息
				String key = type.toString();
				
				TaintedInfo taintedInfo = InputFeature.inputTable.get(key);
				
				if(taintedInfo == null)
				{
					continue;
				}
				
				//获取JDK库函数的元组关键字
				List seqs = taintedInfo.getTaintedSeqs();
				
				for(int j=0; j<seqs.size(); j++)
				{
					int kj = Integer.parseInt((String) seqs.get(j));
					Integer k = Integer.valueOf(kj);
					if(k.equals(0))
						//表示是返回值被污染
					{
						//获取返回值变量
						String case1 = ".//VariableDeclaratorId[../VariableInitializer/Expression//PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@Arguments='true']]]";
						List rets = TDCreatorTools.findTreeNodes(node, case1);
						Iterator iter = rets.iterator();
						while (iter.hasNext()) {
							ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) iter.next();
							FSMMachineInstance fsminstance = fsm.creatInstance();
							// 创建被感染的变量集合，不添加集合，在Inputed处添加
							TaintedSet tainted = new TaintedSet();
							// 设置标记节点
							tainted.setTagTreeNode(id);
							fsminstance.setRelatedObject(tainted);
							if (!id.hasLocalMethod(node)) {
								list.add(fsminstance);
							}
						}
					}
					else
						//表示是参数值被污染
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
							// 不添加集合，在Inputed处添加
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
