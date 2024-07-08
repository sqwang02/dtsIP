package softtest.rules.java.safety.PCQ;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

/** ����������״̬�������� Poor Code Quality ,applet servlet����main����*/
public class PCQMainStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����������ģʽ: %d �е�webӦ�ó����J2EEӦ�ó����applets�к���main��������main�����ŵ�webӦ�ó����н��ᵼ�¸ó������һ���򵥷��ʵĺ��š�", errorline);
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
		//applet��servlet�����а���main������ɵ���������
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
