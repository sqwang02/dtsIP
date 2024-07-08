package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**           6.5
定义6.5：在java语言中，对于不同类型变量的比较有不同的方式，此类缺陷指的是不合适的比较方式。
在Java中对于对象的比较应使用equals，对于数组的比较应使用java.util.Arrays.equals(Object[], Object[])。
1. 对象比较
【例6-8】 下列程序：
   1   Integer i1 = new Integer(1);
   2   Integer i2 = new Integer(1); 
   3   System.out.println("is same Int :" + (i1 == i2));  // false 
   4   System.out.println(“is same Int :” + (i1.equals(i2)));  //true 
判断两个对象的值是否相等时，使用equals()。
/――――――――――――――――――――――――――――――――/
目前只进行基本类型的包装类(Integer, Character, Byte,Float, Double)的比较检测。

2008-3-25
 */

public class UnproperCmpStateMachine {

	private static List getTreeNode(SimpleJavaNode node, String xpathStr) {
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}

	public static List<FSMMachineInstance> createUnproperCmpStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		/////////////////////     Boxed Class, String, float&double    ///////////////////
		String  xpathStr = ".//EqualityExpression";
		List evalRlts = getTreeNode(node, xpathStr);
		if( evalRlts != null && evalRlts.size() > 0 ) {
			
			for(int i = 0; i < evalRlts.size(); i++) {
				ASTEqualityExpression  astEqul = ( ASTEqualityExpression ) evalRlts.get(i);
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
					logc1("TypeImage:" + vldecl.getTypeImage());
					
					if( isBoxedTypeOrStringOrfloat(ltype) ) {
						beBoxedOrString = true;
					}
				}
				if( ! beBoxedOrString ) {
					if(rightDecl instanceof VariableNameDeclaration) {
						vrdecl = (VariableNameDeclaration)rightDecl;
						rtype = vrdecl.getTypeImage();
						logc1("TypeImage:" + vrdecl.getTypeImage());
					
						if( isBoxedTypeOrStringOrfloat(rtype) ) {
							beBoxedOrString = true;
						}
					}
				}
				if( beBoxedOrString ) {
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(astEqul));
					list.add(fsmInst);
					fsmInst.setResultString("comparing Boxed class Or string with == operator");
				}
			}
		}
		//////////   Array Comapre,  or  NonCompatible-type compare  //////////
		xpathStr = ".//Name[ matches(@Image, '\\.equals') ]";
		evalRlts = getTreeNode(node, xpathStr);
		if( evalRlts != null && evalRlts.size() > 0 ) {
			for(int i = 0; i < evalRlts.size(); i++) {
				ASTName  astName = ( ASTName ) evalRlts.get(i);
				String   str     = astName.getImage();
				String  names [] = str.split("\\.");
				if( names.length != 2 ) {
					continue;
				}
				NameDeclaration ldecl = astName.getNameDeclaration();
				if(!(ldecl instanceof VariableNameDeclaration) ) {
					logc1("ldecl:" + ldecl);
					continue;
				}
				VariableNameDeclaration vdecl = (VariableNameDeclaration)ldecl;
				// xx.equals(..);     xx is Xx[] Array type
				if( vdecl.isArray() ) {
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(astName));
					list.add(fsmInst);
					fsmInst.setResultString("comparing Array with equals method");
					continue;
				}
				// xx.equals(..);    xx is non Array type
				ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression)astName.jjtGetParent().jjtGetParent();
				if(astPrimExpr.jjtGetNumChildren() == 2 && astPrimExpr.jjtGetChild(1) instanceof ASTPrimarySuffix) {
					ASTPrimarySuffix astSufix = (ASTPrimarySuffix)astPrimExpr.jjtGetChild(1);
					ASTName astArgName = (ASTName)astSufix.getSingleChildofType(ASTName.class);
					ASTLiteral astArgLit = (ASTLiteral)astSufix.getSingleChildofType(ASTLiteral.class);
	
					if( astArgName != null ) {
						NameDeclaration argnmDecl = astArgName.getNameDeclaration();
						if(argnmDecl instanceof VariableNameDeclaration) {
							VariableNameDeclaration argdecl = (VariableNameDeclaration)argnmDecl;
							
							Config.DEBUG = false; /////////       for temp
							
							if( ! isCompatible(vdecl, argdecl)) {
								FSMMachineInstance fsmInst = fsm.creatInstance();
								fsmInst.setRelatedObject(new FSMRelatedCalculation(astName));
								list.add(fsmInst);
								fsmInst.setResultString("comparing UnCompatible types");
							}
							
							Config.DEBUG = true; /////////       for temp
						}
					}
					else
					if( astArgLit != null ) {
						if( astArgLit.isStringLiteral() ) {
							String tyimge = vdecl.getTypeImage();
							if( !tyimge.equals("String") && !tyimge.equals("java.lang.String") ) {
								FSMMachineInstance fsmInst = fsm.creatInstance();
								fsmInst.setRelatedObject(new FSMRelatedCalculation(astName));
								list.add(fsmInst);
								fsmInst.setResultString("comparing UnCompatible types");
							}
						}
					}
				}
			}
		}

		return list;
	}


	public static boolean checkUnproperCmp(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}

		logc2("+--------------------+");
		logc2("| checkObjectCompare |" );
		logc2("+--------------------+");
		found = true;


		return found;
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
	
	private static boolean isCompatible(VariableNameDeclaration v1, VariableNameDeclaration v2) {
		String typ1 = v1.getTypeImage();
		String typ2 = v2.getTypeImage();
		
		String lst1 = typ1;
		String lst2 = typ2;
		if(typ1.contains(".")) {
			lst1 = typ1.substring(typ1.lastIndexOf('.'));
		}
		if(typ2.contains(".")) {
			lst2 = typ2.substring(typ2.lastIndexOf('.'));
		}
		if(lst1.equals(lst2)) { return true; }

		boolean c = true;
		String heads[] = {"", "java.lang.", "java.util.", "java.io."};
		int   idx =  0;
		Class  cls1 = null;
		Class  cls2 = null;
		
		 
		
		if( typ1.contains(".") ) {
			try{
				cls1 = Class.forName(typ1);
			} catch(Exception ex) {
				if( Config.DEBUG ) {
					ex.printStackTrace();
				}
			}
		} else {
			while(cls1 == null && idx < heads.length) {
				try{
					cls1 = Class.forName(heads[idx]+typ1);
				} catch(Exception ex) {
					if( Config.DEBUG ) {
						ex.printStackTrace();
					}
				}
				idx ++;
				break;
			}
		}
		idx = 0;
		if( typ2.contains(".") ) {
			try{
				cls1 = Class.forName(typ1);
			} catch(Exception ex) {
				if( Config.DEBUG ) {
					ex.printStackTrace();
				}
			}
		} else {
			while(cls2 == null && idx < heads.length) {
				try{
					cls2 = Class.forName(heads[idx]+typ2);
				} catch(Exception ex) {
					if( Config.DEBUG ) {
						ex.printStackTrace();
					}
				}
				idx ++;
				break;
			}
		}
		if(cls1 == null || cls2 == null) {
			return true;
		}
		try{
			if( cls1.equals(cls2) ) {
				return true;
			}else {
				Object o1 = cls1.newInstance();
				Object o2;
				try{
					o2 = o1;
					return true;
				} catch (Exception ex) {
					o2 = cls2.newInstance();
					o1 = o2;
					return true;
				}
			}
		} catch(Exception ex) {
			if( Config.DEBUG ) {
				ex.printStackTrace();
			}
		}
		return c;
	}

	public static void logc1(String str) {
		logc("createUnproperCmpStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkUnproperCmp(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("UnproperCmpStateMachine::" + str);
		}
	}
}
