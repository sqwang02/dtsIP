package softtest.rules.java.question.BC;


import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;

public class MtMAStateMachine extends
		AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("������toString()������: ��arrayʹ��toString()�����ܿ��������ַ���ֻ�ܿ�����[C@16f0472�����ķ��ţ�Ҳ�������ĵ�ַ�������Ҫ���������ÿ�����ݣ���ʹ��ѭ��������ζ��������е�ֵ��");
		}else{
			f.format("Misusing toString Method in Array: the array misuses toString() method on line %d,that may cause Bad Code.",
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

	private static String XPATH1 = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'^.+\\.toString$']";
	private static String XPATH2 =".//PrimaryExpression/PrimarySuffix[@Image='toString']";
	public static List<FSMMachineInstance> createMtMAs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTName name=(ASTName)o;
			if(name.getNameDeclaration() instanceof VariableNameDeclaration){
				VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
				if((v.getType()!=null&&v.getType().isArray())|| v.isArray()){
					//����״̬��
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("Misusing toString Method in Array.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
					list.add(fsminstance);
				}	
			}				
		}
		
		
		List result1=null;
		
		result1=node.findXpath(XPATH2);
		for(Object o:result1){
			ASTPrimarySuffix suffix=(ASTPrimarySuffix)o;
			if(suffix.getPrevSibling() instanceof ASTPrimarySuffix)
			{

				ASTPrimarySuffix presuffix=(ASTPrimarySuffix)suffix.getPrevSibling();
				Object type=presuffix.getType();
				if(type!=null&&(type instanceof Class)&&((Class)type).isArray()){
					//����״̬��
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("Misusing toString Method in Array.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)presuffix.getPrevSibling()));
					list.add(fsminstance);
					continue;
				}
				if(presuffix.getPrevSibling() instanceof ASTPrimarySuffix){
					ASTPrimarySuffix methodname=(ASTPrimarySuffix)presuffix.getPrevSibling();
					if(methodname.getNameDeclaration() instanceof MethodNameDeclaration){
						MethodNameDeclaration mn=(MethodNameDeclaration)methodname.getNameDeclaration();
						ASTMethodDeclarator mdtor=mn.getMethodNameDeclaratorNode();
						ASTMethodDeclaration mdtion=(ASTMethodDeclaration)mdtor.getFirstParentOfType(ASTMethodDeclaration.class);
						if(mdtion.getResultType().returnsArray()){
							FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setResultString("Misusing toString Method in Array.");
							fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)presuffix.getPrevSibling()));
							list.add(fsminstance);
							continue;
						}
					}
				}
				if(presuffix.getPrevSibling() instanceof ASTPrimaryPrefix){
					ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)presuffix.getPrevSibling();
					ASTName methodname=(ASTName)prefix.getSingleChildofType(ASTName.class);
					if(methodname!=null&&methodname.getNameDeclaration() instanceof MethodNameDeclaration){
						MethodNameDeclaration mn=(MethodNameDeclaration)methodname.getNameDeclaration();
						ASTMethodDeclarator mdtor=mn.getMethodNameDeclaratorNode();
						ASTMethodDeclaration mdtion=(ASTMethodDeclaration)mdtor.getFirstParentOfType(ASTMethodDeclaration.class);
						if(mdtion.getResultType().returnsArray()){
							FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setResultString("Misusing toString Method in Array.");
							fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)presuffix.getPrevSibling()));
							list.add(fsminstance);
							continue;
						}
					}
				}
			}
			
			
		}
		
		
		return list;
	}
}
