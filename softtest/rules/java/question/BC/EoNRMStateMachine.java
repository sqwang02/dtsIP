package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class EoNRMStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("空的或缺省的run（）方法: 无论是继承了Thread还是实现了Runnable的线程都应该定义一个run()方法，您使用没有任何操作的空的run方法会是浪费时间");
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
