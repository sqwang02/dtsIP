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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("数组中toString()的误用: 对array使用toString()，不能看到它的字符，只能看到象[C@16f0472这样的符号，也就是它的地址。如果您要看到数组的每个内容，请使用循环语句依次读出数组中的值。");
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
					//创建状态机
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
					//创建状态机
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
