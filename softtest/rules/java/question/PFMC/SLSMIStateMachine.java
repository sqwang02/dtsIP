package softtest.rules.java.question.PFMC;


import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;


/**
 * SLSMIStateMachine
 * 检查同一锁的同步方法调用
 * 一个同步方法调用其他拥有同一锁的同步方法，并不会产生同步的错误，但是会对性能有不好的影响，应避免这种情况。 
 举例：
   1   public class MyClass {
   2   		public synchronized List getElements() {
   3   			return internalGetElements();
   4   		}
   5   		synchronized List internalGetElements() {
   6   			List list = new ArrayList();
   7   			// calculate and return list of elements
   8   			return list;
   9   		}
   10   // ...
   11   }

 * @author cjie
 * 
 */
public class SLSMIStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("同一锁的同步方法调用: %d 行上重复使用了同步synchronized", beginline);
		}else{
			f.format("Same Lock Synchronized Method Invoke :line %d used synchronized repeatedly.", beginline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	private static String XPATH1=".//MethodDeclaration[@Synchronized='true']";
	private static String XPATH2=".//SynchronizedStatement";
	private static String XPATH3=".//Name[@MethodName='true']";
	/**是否是同一锁的同步方法调用*/
    public static boolean isSyn=false; 
    private static StringBuffer sb = new StringBuffer();
    private static List<ASTMethodDeclaration> methods = new ArrayList<ASTMethodDeclaration>();
	/**
	 * 功能： 创建同一锁的同步方法调用状态机
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 * @throws
	 */
	public static List<FSMMachineInstance> createSLSMIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH1);
		for(Object o:result){
			/**匹配是synchronized的方法定义*/
			ASTMethodDeclaration methodDeclaration=(ASTMethodDeclaration)o;
			List<String> lists = new ArrayList<String>();
			lists.add(methodDeclaration.getMethodName());
			isSyn=false;
			sb.delete(0, sb.length());
			methods.clear();
			methods.add(methodDeclaration);
			checkSameLockSynchronizedMethodInvoke(methodDeclaration, lists);
			if(isSyn)
	    	{
	    		FSMMachineInstance fsminstance = fsm.creatInstance();
	    		fsminstance.setResultString("Same Lock Synchronized Method Invoke");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(methodDeclaration));
				fsminstance.setTraceinfo(sb.toString());
				list.add(fsminstance);
	    	}

		}			
	   return list;
	}
	/**
	 * 
	 *
	 * @param ASTMethodDeclaration 传入的同步方法定义
	 * @return void
	 * @throws
	 */
	public static void  checkSameLockSynchronizedMethodInvoke(ASTMethodDeclaration methodDeclaration, List<String> list)
	{
		List resultSynBlock=null;
		List resultSynMethod=null;
		try {
			 /**匹配当前方法中的同步代码块*/
			 resultSynBlock=methodDeclaration.findChildNodesWithXPath(XPATH2);
			 /**匹配当前方法调用的所有方法*/
			 resultSynMethod=methodDeclaration.findChildNodesWithXPath(XPATH3);
		} catch (JaxenException e) {
			e.printStackTrace();
		}
		
		/**方法包括同步代码块*/
		if(resultSynBlock!=null&&resultSynBlock.size()>0)
		{
			isSyn= true;
			return;
		}

		/**检查当前方法被调用的方法是否是同步方法*/
		for(Object o2:resultSynMethod){
			ASTName name=(ASTName) o2;
			/**取方法定义*/
			NameDeclaration declaration=name.getNameDeclaration();
			if(declaration!=null)
			{
				ASTMethodDeclaration methodNode=null;
				if(declaration.getNode().jjtGetParent() instanceof ASTMethodDeclaration)
					methodNode=(ASTMethodDeclaration) declaration.getNode().jjtGetParent();
				
				if(methodNode!=null&&methodNode.isSynchronized())
				{
					isSyn= true;
					sb.append("方法");
					for(String s : list) {
						sb.append(s + "->");
					}
					sb.delete(sb.length() - 2, sb.length());
					sb.append("重复使用了Synchronized。");
					return;
				}
				if(methodNode!=null) {
					if (methods.contains(methodNode)) {
						continue;
					} else {
						methods.add(methodNode);
					}
					 /**递归检查是否包含同步代码块或调用了同步方法*/
					list.add(name.getImage());
					checkSameLockSynchronizedMethodInvoke(methodNode,list);
					list.remove(list.size() - 1);
				}

				
			}
		}
	}
}
