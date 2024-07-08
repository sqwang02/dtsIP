package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * VNUStateMachine
 * ���û�б�ʹ�ù��ı���
 * ����������û�б���ȡ���еı����ڶ�����δ�����ù����еı�����������¸�ֵ��δ��ʹ�ù��������ı��������ڲ����Ĵ��룬������ϵͳ�������в�����Ӱ�죬Ӧ�ñ���������������
 ������
	1    class A{
	2       void foo(int i){
	3			����
	4 			int var;
	5  			����
	6     };
	7   }


 * @author cjie
 * 
 */
public class VNUStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����δʹ��: %d ���ϵı���  \'%s\' ��δ��ʹ�ù�", errorline,fsmmi.getResultString());
		}else{
			f.format("Variable Never Used: the variable \'%s\' in line %d never be used.", fsmmi.getResultString(),errorline);
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
	
	private static String XPATH=".//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId";
	
	/**
	 * ���ܣ� ����û�б�ʹ�ù��ı���״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 * @throws
	 */
	public static List<FSMMachineInstance> createVNUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**�����������*/
		result=node.findXpath(XPATH);
		For1:
		for(Object o:result){
			ASTVariableDeclaratorId declNode=(ASTVariableDeclaratorId)o;
			/**����Ǿ�̬��ʼ�������򲻽��д���*/
			List<ASTInitializer> staticBlock=declNode.getParentsOfType(ASTInitializer.class);
			for( ASTInitializer init:staticBlock)
			{
				if(init.isStatic())
					continue For1;
			}
			/**ȡ�ñ�������*/
			VariableNameDeclaration decl=declNode.getNameDeclaration();
		    List<NameOccurrence> occrList=decl.getOccs();
		    if(occrList==null||occrList.size()==0)
		    {
		    	FSMMachineInstance fsminstance = fsm.creatInstance();
	    		fsminstance.setResultString(declNode.getImage());
				fsminstance.setRelatedObject(new FSMRelatedCalculation(declNode));
				list.add(fsminstance);
		    }
		    else 
		    {
		    	NameOccurrence occ=null;
		    	boolean use=false;
		    	for(Object t:occrList){
		    		occ=(NameOccurrence)t;
		    		if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.USE||occ.isSelfAssignment()){
		    			use=true;
		    			break;
		    		}
		    	}
		    	if(!use){
					FSMMachineInstance fsminstance = fsm.creatInstance();
		    		fsminstance.setResultString(declNode.getImage());
					fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)occ.getLocation()));
		    	   //fsminstance.setRelatedObject(new FSMRelatedCalculation(declNode));
					list.add(fsminstance);
		    	}
		    }
		    	
		}			
	   return list;
	}
}
