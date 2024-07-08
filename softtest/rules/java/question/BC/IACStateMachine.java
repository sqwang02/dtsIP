package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class IACStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("数组的不正确的比较: 比较两个数组时，应该使用java.util.Arrays.equals(Object[], Object[])方法，否则，请检查是否跟您预计设计一致。");
		} else {
			f.format("Incorrect Array Comparison: two arrays comparise each other without java.util.Arrays.equals on line %d,that may cause Bad Code.",
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

	private static String XPATH1 = ".//Name[ matches(@Image, '\\.equals') ]";

	public static List<FSMMachineInstance> createIACs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTName astName = (ASTName) o;
			String str = astName.getImage();
			String names[] = str.split("\\.");
			if (names.length != 2) {
				continue;
			}
			NameDeclaration ldecl = astName.getNameDeclaration();
			if (!(ldecl instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration vdecl = (VariableNameDeclaration) ldecl;
			// xx.equals(..); xx is Xx[] Array type
			if (vdecl.isArray()) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Incorrect Array Comparison.");
				fsminstance
						.setRelatedObject(new FSMRelatedCalculation(astName));
				list.add(fsminstance);
				continue;
			}
			// xx.equals(..); xx is non Array type
			ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression) astName
					.jjtGetParent().jjtGetParent();
			if (astPrimExpr.jjtGetNumChildren() == 2
					&& astPrimExpr.jjtGetChild(1) instanceof ASTPrimarySuffix) {
				ASTPrimarySuffix astSufix = (ASTPrimarySuffix) astPrimExpr
						.jjtGetChild(1);
				ASTName astArgName = (ASTName) astSufix
						.getSingleChildofType(ASTName.class);
				ASTLiteral astArgLit = (ASTLiteral) astSufix
						.getSingleChildofType(ASTLiteral.class);

				if (astArgName != null) {
					NameDeclaration argnmDecl = astArgName.getNameDeclaration();
					if (argnmDecl instanceof VariableNameDeclaration) {
						VariableNameDeclaration argdecl = (VariableNameDeclaration) argnmDecl;


						if (!isCompatible(vdecl, argdecl)) {
							FSMMachineInstance fsminstance = fsm
									.creatInstance();
							fsminstance
									.setResultString("Incorrect Array Comparison.");
							fsminstance
									.setRelatedObject(new FSMRelatedCalculation(
											astName));
							list.add(fsminstance);
						}

					}
				} else if (astArgLit != null) {
					if (astArgLit.isStringLiteral()) {
						String tyimge = vdecl.getTypeImage();
						if (!tyimge.equals("String")
								&& !tyimge.equals("java.lang.String")) {
							FSMMachineInstance fsminstance = fsm
									.creatInstance();
							fsminstance
									.setResultString("Incorrect Array Comparison.");
							fsminstance
									.setRelatedObject(new FSMRelatedCalculation(
											astName));
							list.add(fsminstance);
						}
					}
				}
			}

		}
		return list;
	}

	private static boolean isCompatible(VariableNameDeclaration v1,
			VariableNameDeclaration v2) {
		String typ1 = v1.getTypeImage();
		String typ2 = v2.getTypeImage();

		String lst1 = typ1;
		String lst2 = typ2;
		if (typ1.contains(".")) {
			lst1 = typ1.substring(typ1.lastIndexOf('.'));
		}
		if (typ2.contains(".")) {
			lst2 = typ2.substring(typ2.lastIndexOf('.'));
		}
		if (lst1.equals(lst2)) {
			return true;
		}

		boolean c = true;
		String heads[] = { "", "java.lang.", "java.util.", "java.io." };
		int idx = 0;
		Class cls1 = null;
		Class cls2 = null;

		if (typ1.contains(".")) {
			try {
				cls1 = Class.forName(typ1);
			} catch (Exception ex) {
				if (Config.DEBUG) {
					ex.printStackTrace();
				}
			}
		} else {
			while (cls1 == null && idx < heads.length) {
				try {
					cls1 = Class.forName(heads[idx] + typ1);
				} catch (Exception ex) {
					if (Config.DEBUG) {
						ex.printStackTrace();
					}
				}
				idx++;
				break;
			}
		}
		idx = 0;
		if (typ2.contains(".")) {
			try {
				cls1 = Class.forName(typ1);
			} catch (Exception ex) {
				if (Config.DEBUG) {
					ex.printStackTrace();
				}
			}
		} else {
			while (cls2 == null && idx < heads.length) {
				try {
					cls2 = Class.forName(heads[idx] + typ2);
				} catch (Exception ex) {
					if (Config.DEBUG) {
						ex.printStackTrace();
					}
				}
				idx++;
				break;
			}
		}
		if (cls1 == null || cls2 == null) {
			return true;
		}
		try {
			if (cls1.equals(cls2)) {
				return true;
			} else {
				Object o1 = cls1.newInstance();
				Object o2;
				try {
					o2 = o1;
					return true;
				} catch (Exception ex) {
					o2 = cls2.newInstance();
					o1 = o2;
					return true;
				}
			}
		} catch (Exception ex) {
			if (Config.DEBUG) {
				ex.printStackTrace();
			}
		}
		return c;
	}

}
