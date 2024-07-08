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
 * ���ͬһ����ͬ����������
 * һ��ͬ��������������ӵ��ͬһ����ͬ�����������������ͬ���Ĵ��󣬵��ǻ�������в��õ�Ӱ�죬Ӧ������������� 
 ������
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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ͬһ����ͬ����������: %d �����ظ�ʹ����ͬ��synchronized", beginline);
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
	/**�Ƿ���ͬһ����ͬ����������*/
    public static boolean isSyn=false; 
    private static StringBuffer sb = new StringBuffer();
    private static List<ASTMethodDeclaration> methods = new ArrayList<ASTMethodDeclaration>();
	/**
	 * ���ܣ� ����ͬһ����ͬ����������״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 * @throws
	 */
	public static List<FSMMachineInstance> createSLSMIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH1);
		for(Object o:result){
			/**ƥ����synchronized�ķ�������*/
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
	 * @param ASTMethodDeclaration �����ͬ����������
	 * @return void
	 * @throws
	 */
	public static void  checkSameLockSynchronizedMethodInvoke(ASTMethodDeclaration methodDeclaration, List<String> list)
	{
		List resultSynBlock=null;
		List resultSynMethod=null;
		try {
			 /**ƥ�䵱ǰ�����е�ͬ�������*/
			 resultSynBlock=methodDeclaration.findChildNodesWithXPath(XPATH2);
			 /**ƥ�䵱ǰ�������õ����з���*/
			 resultSynMethod=methodDeclaration.findChildNodesWithXPath(XPATH3);
		} catch (JaxenException e) {
			e.printStackTrace();
		}
		
		/**��������ͬ�������*/
		if(resultSynBlock!=null&&resultSynBlock.size()>0)
		{
			isSyn= true;
			return;
		}

		/**��鵱ǰ���������õķ����Ƿ���ͬ������*/
		for(Object o2:resultSynMethod){
			ASTName name=(ASTName) o2;
			/**ȡ��������*/
			NameDeclaration declaration=name.getNameDeclaration();
			if(declaration!=null)
			{
				ASTMethodDeclaration methodNode=null;
				if(declaration.getNode().jjtGetParent() instanceof ASTMethodDeclaration)
					methodNode=(ASTMethodDeclaration) declaration.getNode().jjtGetParent();
				
				if(methodNode!=null&&methodNode.isSynchronized())
				{
					isSyn= true;
					sb.append("����");
					for(String s : list) {
						sb.append(s + "->");
					}
					sb.delete(sb.length() - 2, sb.length());
					sb.append("�ظ�ʹ����Synchronized��");
					return;
				}
				if(methodNode!=null) {
					if (methods.contains(methodNode)) {
						continue;
					} else {
						methods.add(methodNode);
					}
					 /**�ݹ����Ƿ����ͬ�������������ͬ������*/
					list.add(name.getImage());
					checkSameLockSynchronizedMethodInvoke(methodNode,list);
					list.remove(list.size() - 1);
				}

				
			}
		}
	}
}
