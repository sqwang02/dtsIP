package softtest.rules.java.question.BC;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTType;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class NDTBTCStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("没提前判断类型就进行类型转换或类型判断不准确:当把调用函数得到的返回值赋给某个变量时，如果要进行类型转换，建议您提前判断一下类型是否相符");
		} else {
			f.format("No Determinning Type: type converses on line %d,but before conversion,not determine type or inaccuratly determine type,that may cause Bad Code.",errorline);
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

	private static String XPATH1 = ".//CastExpression/PrimaryExpression";
	public static List<FSMMachineInstance> createNDTBTCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimaryExpression prim = (ASTPrimaryExpression) o;
			Method meth = prim.getLastMethod();
			if (meth != null) {
				if (!meth.getReturnType().getName().equals(Void.TYPE.getName())) {
					String name = meth.getReturnType().getName();
					int i = name.indexOf('.');

					String subname = null;
					String typename = null;

					if (i > 0) {
						subname = name.substring(name.lastIndexOf('.') + 1);
					} else {									
						subname =name;
					}
                   
//					Pattern pattern = Pattern.compile(subname);
//					Matcher matcher = pattern.matcher("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");
					
					Pattern pattern = Pattern.compile("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");
					Matcher matcher = pattern.matcher(subname);
					if (prim.getPrevSibling() instanceof ASTType) {
						ASTType type = (ASTType) prim.getPrevSibling();
						typename = type.getTypeImage();
						/**
						 * modified by yang
						 */
				//		Pattern pattern1 = Pattern.compile(typename);
				//		Matcher matcher1 = pattern1.matcher("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");
						
						Pattern pattern1 = Pattern.compile("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");
						Matcher matcher1 = pattern1.matcher(typename);

						if (!(matcher.find() && matcher1.find())){
							if (prim.getFirstParentOfType(ASTIfStatement.class,node) != null) {
								ASTIfStatement astifstate = (ASTIfStatement) prim.getFirstParentOfType(ASTIfStatement.class, node);
								if (astifstate.jjtGetChild(0).jjtGetChild(0) instanceof ASTInstanceOfExpression) {
									if (astifstate.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1) instanceof ASTType) {
										ASTType astType = (ASTType) astifstate.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1);
										String type1 = astType.getTypeImage();
										if (!type1.equals(typename)) {
											FSMMachineInstance fsminstance = fsm.creatInstance();
											fsminstance.setResultString("No Determinning Type Before Type Conversion or Inaccurate Type Determination.");
											fsminstance.setRelatedObject(new FSMRelatedCalculation(prim));
											list.add(fsminstance);
										}
									}
								}
							}
						}
					}
				}
			}
			else {
				if (prim.getPrevSibling() instanceof ASTType) {
					ASTType type = (ASTType) prim.getPrevSibling();
					String typename = type.getTypeImage();
					/**
					 * modified by yang
					 */
				//	Pattern pattern1 = Pattern.compile(typename);
				//	Matcher matcher1 = pattern1.matcher("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");

					Pattern pattern1 = Pattern.compile("\bint\b|\blong\b|\bshort\b|\bbyte\b|\bfloat\b|\bdouble\b|\bchar\b");
					Matcher matcher1 = pattern1.matcher(typename);
					
					if (! matcher1.find()){
						if (prim.getFirstParentOfType(ASTIfStatement.class,node) != null) {
							ASTIfStatement astifstate = (ASTIfStatement) prim.getFirstParentOfType(ASTIfStatement.class, node);
							if (astifstate.jjtGetChild(0).jjtGetChild(0) instanceof ASTInstanceOfExpression) {
								if (astifstate.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1) instanceof ASTType) {
									ASTType astType = (ASTType) astifstate.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1);
									String type1 = astType.getTypeImage();
									if (!type1.equals(typename)) {
										FSMMachineInstance fsminstance = fsm.creatInstance();
										fsminstance.setResultString("No Determinning Type Before Type Conversion or Inaccurate Type Determination.");
										fsminstance.setRelatedObject(new FSMRelatedCalculation(prim));
										list.add(fsminstance);
									}
								}
							}
						}
					}
				}
			}
			continue;
		}

		return list;
	}

}
