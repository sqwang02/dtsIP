package softtest.rules.java.question.BC;

import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTFormalParameters;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class URCStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("无条件递归调用: 方法没有任何预先检查就调用自己，可能导致stack overflow，提醒您对此进行检查。");
		} else {
			f.format("Unconditional Recursive Calls: the variable calls itself on line %d,that may cause Bad Code.",errorline);
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

	private static String XPATH1 = ".//MethodDeclaration";

	public static List<FSMMachineInstance> createURCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;
		result = node.findXpath(XPATH1);
		
		outer:	
		for (Object o : result) {
			ASTMethodDeclaration meth = (ASTMethodDeclaration) o;
			if (meth.jjtGetChild(1) instanceof ASTMethodDeclarator) {
				ASTMethodDeclarator methodD = (ASTMethodDeclarator) meth.jjtGetChild(1);
				String str = methodD.getImage();
				
				ASTFormalParameters formalParameters=(ASTFormalParameters)methodD.jjtGetChild(0);
				int childernum=formalParameters.jjtGetNumChildren();
				LinkedList<String>nameList=new LinkedList<String>();
				ASTVariableDeclaratorId vId=null;
				boolean isVarargs=false;
				for (int i = 0; i < childernum; i++) {
					ASTFormalParameter formParameter=(ASTFormalParameter)formalParameters.jjtGetChild(i);
					if (formParameter.isVarargs()) {
						isVarargs=true;
					}
//					vId=(ASTVariableDeclaratorId)formParameter.jjtGetChild(1);//modified by yang
					vId=(ASTVariableDeclaratorId)formParameter.getFirstChildOfType(ASTVariableDeclaratorId.class);
					nameList.add(vId.getImage());
				}
				

				String XPATH2 = ".//PrimaryExpression/PrimarySuffix[preceding-sibling::PrimarySuffix[1][@Image='"
						+ str
						+ "']  and preceding-sibling::PrimaryPrefix[1][@ThisModifier='true']]/Arguments/ArgumentList";

				List result2 = null;
				result2 = meth.findXpath(XPATH2);
				
				int arglistchildnumI=0;
				if (result2 != null) {
					for (Object o2 : result2) {
						ASTArgumentList arg = (ASTArgumentList) o2;
						arglistchildnumI=arg.jjtGetNumChildren();
						
						LinkedList<String>argList=new LinkedList<String>();
						for (int i = 0; i < arglistchildnumI; i++) {
							ASTName argName=(ASTName)((ASTExpression)arg.jjtGetChild(i)).getSingleChildofType(ASTName.class);
							if (argName!=null) {
								argList.add(argName.getImage());
							}else {
								argList.add("");
							}
						}
						
						if (java.util.Arrays.equals(arg.getParameterTypes(),methodD.getParameterTypes())) {
							
							if (isVarargs) {
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setResultString("Unconditional Recursive Calls.");
								fsminstance.setRelatedObject(new FSMRelatedCalculation(arg));
								list.add(fsminstance);
								break outer;
							}
							
							List lis01 = arg.getParentsOfType(ASTIfStatement.class);
							List lis02 = arg.getParentsOfType(ASTForStatement.class);
							List lis03 = arg.getParentsOfType(ASTWhileStatement.class);
							List lis04 = arg.getParentsOfType(ASTSwitchStatement.class);
							
							if (lis01.size() + lis02.size() + lis03.size()+ lis04.size() == 0) {
								if(arg.jjtGetParent().jjtGetParent().jjtGetParent()instanceof ASTPrimaryExpression)
								{
									ASTPrimaryExpression exp=(ASTPrimaryExpression)arg.jjtGetParent().jjtGetParent().jjtGetParent();	
									if(exp.jjtGetChild(1)instanceof ASTPrimarySuffix)
									{
										ASTPrimarySuffix suf=(ASTPrimarySuffix)exp.jjtGetChild(1);
										if(suf.getImage().equals(str))
										{
											boolean flag=false;
											int i = 0;
											while (i <nameList.size()) {
												if (!nameList.get(i).equals(argList.get(i))) {
													break;
												}
												i++;
											}
											if (i==nameList.size()) {
												flag=true;
											}
											if (flag) {
												FSMMachineInstance fsminstance = fsm.creatInstance();
												fsminstance.setResultString("Unconditional Recursive Calls.");
												fsminstance.setRelatedObject(new FSMRelatedCalculation(arg));
												list.add(fsminstance);
											}
										}
									}
								}
							}
						}
					}
				}

				String XPATH3 = ".//PrimaryExpression/PrimarySuffix[preceding-sibling::PrimaryPrefix[1]/Name[@Image='"
						+ str + "']]/Arguments/ArgumentList";

				List result3 = null;
				result3 = meth.findXpath(XPATH3);
				if (result3 != null) {
					
					for (Object o3 : result3) {
						ASTArgumentList arg = (ASTArgumentList) o3;
						arglistchildnumI=arg.jjtGetNumChildren();
						
						LinkedList<String>argList=new LinkedList<String>();
						for (int i = 0; i < arglistchildnumI; i++) {
							ASTName argName=(ASTName)((ASTExpression)arg.jjtGetChild(i)).getSingleChildofType(ASTName.class);
							if (argName!=null) {
								argList.add(argName.getImage());
							}else {
								argList.add("");
							}
						}

						if (java.util.Arrays.equals(arg.getParameterTypes(),
								methodD.getParameterTypes())) {
							
							if (isVarargs) {
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setResultString("Unconditional Recursive Calls.");
								fsminstance.setRelatedObject(new FSMRelatedCalculation(arg));
								list.add(fsminstance);
								break outer;
							}
							
							List lis01 = arg.getParentsOfType(ASTIfStatement.class);
							List lis02 = arg.getParentsOfType(ASTForStatement.class);
							List lis03 = arg.getParentsOfType(ASTWhileStatement.class);
							List lis04 = arg.getParentsOfType(ASTSwitchStatement.class);
							
							if (lis01.size() + lis02.size() + lis03.size()
									+ lis04.size() == 0) {
								boolean flag=false;
								int i = 0;
								while (i <nameList.size()) {
									if (!nameList.get(i).equals(argList.get(i))) {
										break;
									}
									i++;
								}
								if (i==nameList.size()) {
									flag=true;
								}
								if (flag) {
									FSMMachineInstance fsminstance = fsm.creatInstance();
									fsminstance.setResultString("Unconditional Recursive Calls.");
									fsminstance.setRelatedObject(new FSMRelatedCalculation(arg));
									list.add(fsminstance);
								}
							}
						}
					}
				}
			}
		}
		return list;
	}
}