package softtest.rules.java.question.PFMC;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTTryStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;

public class VABUStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("����ʹ��ǰ�����¸�ֵ:��Щ������ʹ��֮ǰ����θ�ֵ�����һ�θ�ֵ��ǰ�ĸ�ֵ���������ø�ֵ�������ڲ������룬Ӧ�ñ��⣡");
		} else {
			f.format("Variable Reassignment Before Used: the code has reassigned the variable before it is used on line %d,that belongs to Low Performance Code.",errorline);
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

	private static String XPATH1 = ".//LocalVariableDeclaration";

	// MethodDeclaration
	public static List<FSMMachineInstance> createVABUs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;
		ArrayList<Scope> scopeList=new ArrayList<Scope>();
		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTLocalVariableDeclaration meth = (ASTLocalVariableDeclaration) o;
			Scope scope = meth.getScope();
			if (scopeList.contains(scope)) {
				continue;
			}else {
				scopeList.add(scope);
			}
			Map map=scope.getVariableDeclarations();
			Iterator iterator=map.keySet().iterator();
			ArrayList<NameOccurrence> aList=null;
			SimpleJavaNode sJavaNode=null;
			while (iterator.hasNext()){
				NameDeclaration nDeclaration=(NameDeclaration)iterator.next();
				if (nDeclaration instanceof VariableNameDeclaration) {
					
					VariableNameDeclaration variable=(VariableNameDeclaration)nDeclaration;
					if (!variable.isArray()) {
						aList=(ArrayList<NameOccurrence>)map.get(variable);
						Vector<Integer> vector=new Vector<Integer>();
						int aaa=0;
						NameOccurrence al=null;
						int flag=-2;
						for(int i=0;i<aList.size();i++) {
							al=aList.get(i);
							if (al.getOccurrenceType() != NameOccurrence.OccurrenceType.DEF) {
							
								vector.add(al.getLocation().getBeginLine());
								continue;
							}
						}
						
						for(int i=0;i<aList.size();i++) {
							al=aList.get(i);
							if (al.getOccurrenceType() != NameOccurrence.OccurrenceType.DEF) {
								++aaa;
								continue;
							}
							List<NameOccurrence> list1 = al.getDefUndefList();// ����-ȡ������
							List<NameOccurrence> list2 = al.getDefUseList();// ����-ʹ��
							ASTTryStatement tryStatement1=null;
							ASTTryStatement tryStatement2=null;
							if ((list1.size() > 0)&& (list2.size() == 0)) {
								if (al.getLocation().getFirstParentOfType(ASTIfStatement.class) == null) {
									flag=i;
									if (al.getLocation() instanceof SimpleJavaNode) {
										sJavaNode=(SimpleJavaNode)al.getLocation();
										if (al.getLocation().getFirstParentOfType(ASTTryStatement.class)!=null) {
											tryStatement1=(ASTTryStatement)al.getLocation().getFirstParentOfType(ASTTryStatement.class);
										}
									}
								}
							}
							if (i-flag==1) {
								if (!vector.contains(al.getLocation().getBeginLine())) {
									if (al.getLocation().getFirstParentOfType(ASTTryStatement.class)!=null) {
										tryStatement2=(ASTTryStatement)al.getLocation().getFirstParentOfType(ASTTryStatement.class);
									}
									if (!(tryStatement1!=null&&tryStatement2!=null&&tryStatement1!=tryStatement2)) {
										if (!al.isSelfAssignment()) {
											FSMMachineInstance fsminstance = fsm.creatInstance();
											fsminstance.setResultString("Variable Assignment Before Used.");
											fsminstance.setRelatedObject(new FSMRelatedCalculation(sJavaNode));
											list.add(fsminstance);
										}
									}
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
