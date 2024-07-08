package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class EoNRMStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("�յĻ�ȱʡ��run��������: �����Ǽ̳���Thread����ʵ����Runnable���̶߳�Ӧ�ö���һ��run()��������ʹ��û���κβ����Ŀյ�run���������˷�ʱ��");
		} else {
			f.format("Empty or No Run Method: the class extending  thread or implementing runnable on line %d do not define run() or have no run(),that may cause Bad Code.",
					 errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}

	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH1 = ".//ClassOrInterfaceDeclaration[./ExtendsList/ClassOrInterfaceType[@Image='Thread']or ./ImplementsList/ClassOrInterfaceType[@Image='Runnable']]/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration[./MethodDeclarator[@Image='run']]/Block";
//	private static String XPATH2 =".//ClassOrInterfaceDeclaration[./ExtendsList/ClassOrInterfaceType[@Image='Thread'] or ./ImplementsList/ClassOrInterfaceType[@Image='Runnable']]/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration[./MethodDeclarator[@Image='run']]";
	public static List<FSMMachineInstance> createEoNRMs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null,result1=null;

		result = node.findXpath(XPATH1);
		// result1=node.findXpath(XPATH2);
		for (Object o : result) {
			ASTBlock block=(ASTBlock)o;
			if(block.jjtGetNumChildren()==0/*||result1.size()==0*/)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Empty or No Run Method.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(block));
				list.add(fsminstance);
			}
			
		}
		return list;
	}
}
