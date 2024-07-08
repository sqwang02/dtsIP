package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * LSCStateMachine
 * ���ʹ����ѭ���ַ�������
 * ���������Ҫ��ѭ���н����ַ������ӣ�Ϊ���������Ӧ��String���͵ı���ת����StringBuffer��JDK1.4��ǰ�汾���Ժ�汾��ΪStringBuilder�����ٵ�����append()�����������ת����String���͡� 
 * ������
   1   // This is bad
   2   String s = "";
   3   for (int i = 0; i < field.length; ++i) {
   4   		s = s + field[i];
   5   }

 * @author cjie
 * 
 */
public class LSCStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ѭ���ַ�������: ��%d ��ʹ����ѭ���ַ�������Ϊ���������Ӧ��String���͵ı���ת����StringBuffer��JDK1.4��ǰ�汾���Ժ�汾��ΪStringBuilder�����ٵ�����append()�����������ת����String����", errorline);
		}else{
			f.format("Loop String Concatenate�� Line %d applyed loop string concatenate.", errorline);
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
	
	private static String XPATH=".//*[self::ForStatement or self::WhileStatement or self::DoStatement]/Statement//AssignmentOperator[(@Image='+=')or(@Image='=' and preceding-sibling::PrimaryExpression/PrimaryPrefix/Name/@Image=following-sibling::Expression/AdditiveExpression/PrimaryExpression/PrimaryPrefix/Name/@Image)]/preceding-sibling::PrimaryExpression/PrimaryPrefix/Name";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createLSCStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTName name=(ASTName)o;
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration))
			{
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^(String)$")) {
				continue;
			}
		    if(v.getNode()!=null&&
		    		(v.getNode().getParentsOfType(ASTForStatement.class).size()>0
		    				||v.getNode().getParentsOfType(ASTWhileStatement.class).size()>0
		    				||v.getNode().getParentsOfType(ASTDoStatement.class).size()>0))
		    {
		    	continue;
		    }
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Loop String Concatenate");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}

	public void f()
	{
		
	}
}
