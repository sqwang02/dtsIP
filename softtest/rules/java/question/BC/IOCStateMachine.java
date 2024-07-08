package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class IOCStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("对象的不正确的比较:判断两个对象的值是否相等时，应使用equals()，若使用==只是判断两个对象的引用地址是否相同。");
		} else {
			f.format("Incorrect Object Comparison: two objects comparises each other with \"==\" on line %d ,that may cause Bad Code.",
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

	private static String XPATH1 = ".//EqualityExpression[./PrimaryExpression[1][following-sibling::PrimaryExpression]]";
	public static List<FSMMachineInstance> createIOCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null,result1=null;

		result = node.findXpath(XPATH1);
		// result1=node.findXpath(XPATH2);
		for (Object o : result) {
			ASTEqualityExpression astEqul=(ASTEqualityExpression)o;
			ASTPrimaryExpression astLeft = (ASTPrimaryExpression) astEqul.jjtGetChild(0);
			ASTPrimaryExpression astRight = (ASTPrimaryExpression) astEqul.jjtGetChild(1);
			ASTName  leftName  = (ASTName)astLeft.getSingleChildofType(ASTName.class);
			ASTName  rightName = (ASTName)astRight.getSingleChildofType(ASTName.class);
			if(leftName == null && rightName == null) {
				// process the case of  ( new Xx(..) == new Xx(..) )
				continue;
			}
			NameDeclaration leftDecl = null;
			NameDeclaration rightDecl = null;
			if( leftName != null ) {
				leftDecl  =  leftName.getNameDeclaration();
			}
			if( rightName != null ) {
				rightDecl = rightName.getNameDeclaration();
			}
			VariableNameDeclaration vldecl = null;
			VariableNameDeclaration vrdecl = null;
			String ltype = null;
			String rtype = null;
			boolean  beBoxedOrString = false;
			if(leftDecl instanceof VariableNameDeclaration) {
				vldecl = (VariableNameDeclaration)leftDecl;
				ltype = vldecl.getTypeImage();
				
				if( isBoxedTypeOrStringOrfloat(ltype) ) {
					beBoxedOrString = true;
				}
			}
			if( ! beBoxedOrString ) {
				if(rightDecl instanceof VariableNameDeclaration) {
					vrdecl = (VariableNameDeclaration)rightDecl;
					rtype = vrdecl.getTypeImage();
				
					if( isBoxedTypeOrStringOrfloat(rtype) ) {
						beBoxedOrString = true;
					}
				}
			}
			if( beBoxedOrString ) {
				if(!(leftName==null||rightName==null))
				{
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(astEqul));
					list.add(fsmInst);
					fsmInst.setResultString("Incorrect Object Comparison");	
				}
				
			}
		}
		
		return list;
	}
	
	
	private static boolean isBoxedTypeOrStringOrfloat(String str) {
		String type = str;
		if( type.indexOf('.') >= 0 ) {
			type = type.substring(type.lastIndexOf('.')+1);
		}
		if(str.equals("Byte")) {
			return true;
		}else if(str.equals("Character")) {
			return true;
		}else if(str.equals("Short")) {
			return true;
		}else if(str.equals("Integer")) {
			return true;
		}else if(str.equals("Long")) {
			return true;
		}else if(str.equals("Double")) {
			return true;
		}else if(str.equals("Float")) {
			return true;
		}else if(str.equals("String")) {
			return true;
		}else if(str.equals("double")) {
			return true;
		}else if(str.equals("float")) {
			return true;
		}
		return false;
	}
}
