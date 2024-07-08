

package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.NameOccurrence;


public class USICStateMachine extends
		AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("未定义为静态的内部类: 应将内部类声明为static类型,除非您的内部类使用指向其外围类对象的引用。");
		}else{
			f.format("Undefine Static Inner Classes: the code has defined unstatic inner class on line %d,that belongs to Low Performance Code.",errorline);
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

	private static String XPATH1 = ".//ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration[./ClassOrInterfaceDeclaration[@Static='false']]";

	public static List<FSMMachineInstance> createUSICs(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;
		ArrayList<NameOccurrence>nameList=new ArrayList<NameOccurrence>();
		NameOccurrence temp=null;
		
		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTClassOrInterfaceBodyDeclaration classorinterface = (ASTClassOrInterfaceBodyDeclaration) o;
			ClassScope classScope=classorinterface.getScope().getEnclosingClassScope();
			Iterator iterator=classScope.getVariableDeclarations().values().iterator();
			while (iterator.hasNext()) {
				List listocurenceList = (List) iterator.next();
				for (int i=0;i<listocurenceList.size();i++) {
					temp=((NameOccurrence)listocurenceList.get(i));
					nameList.add(temp);
				}
			}
			
			iterator=classScope.getMethodDeclarations().values().iterator();
			while (iterator.hasNext()) {
				List listocurenceList = (List) iterator.next();
				for (int i=0;i<listocurenceList.size();i++) {
					temp=((NameOccurrence)listocurenceList.get(i));
					nameList.add(temp);
				}
			}
			int namelistlength=nameList.size();
			if (namelistlength==0) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Undefine Static Inner Classes.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(classorinterface));
				list.add(fsminstance);
			}else {
				boolean flagB=true;
				ASTClassOrInterfaceBodyDeclaration clzbodyclz=null;
				for (int i = 0; i < namelistlength; i++) {
					ASTClassOrInterfaceDeclaration clzoringerdecl=(ASTClassOrInterfaceDeclaration)nameList.get(i).getLocation().getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
					if (clzoringerdecl!=null) {
					clzbodyclz=(ASTClassOrInterfaceBodyDeclaration)clzoringerdecl.getSingleParentofType(ASTClassOrInterfaceBodyDeclaration.class);
					}
					if (clzbodyclz!=null&&clzbodyclz.equals(classorinterface)) {
						flagB=false;
					}
				}
				if (flagB) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("Undefine Static Inner Classes.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(classorinterface));
					list.add(fsminstance);
				}
			}
		}

		return list;
	}
}
