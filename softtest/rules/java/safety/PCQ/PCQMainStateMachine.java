package softtest.rules.java.safety.PCQ;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

/** 低质量代码状态机动作类 Poor Code Quality ,applet servlet存在main函数*/
public class PCQMainStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("低质量代码模式: %d 行的web应用程序或J2EE应用程序或applets中含有main方法。将main方法放到web应用程序中将会导致该程序存在一个简单访问的后门。", errorline);
		}else{
			f.format("Poor Code Quality: web application or J2EE application or applets contains main method on line %d",errorline);
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
	public static List<FSMMachineInstance> createPCQStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		//applet或servlet程序中包含main方法造成低质量代码
		String xPath = ".//TypeDeclaration/ClassOrInterfaceDeclaration/ExtendsList/ClassOrInterfaceType[1]";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceType parentclass = (ASTClassOrInterfaceType) i.next();
			String image=parentclass.getImage();
			if(image==null||!(image.toLowerCase().contains("applet")||image.toLowerCase().contains("servlet"))){
				continue;
			}
			xPath="./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration/MethodDeclarator[@Image=\'main\']";
			ASTClassOrInterfaceDeclaration classdecl=(ASTClassOrInterfaceDeclaration)parentclass.jjtGetParent().jjtGetParent();
			List mains=classdecl.findXpath(xPath);
			if(mains.size()==0){
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(classdecl));
			fsminstance.setResultString(classdecl.getImage());
			list.add(fsminstance);
		}
		return list;
	}
}
