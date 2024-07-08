package softtest.rules.java.safety.PWL;

import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**
��鲻��ȫ��α�����
   ���г���
   1   public static void main(String[] args)throws SQLException {
   2       Random rand = new Random();
   3   }
 */

public class NonSecurityRandomStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ȫ����ȱ��ģʽ: %d ��ʹ����һ������ȫ�������ֵ�������߿��Ը��ݲ���Ԥ����������ɵ�ֵ��", errorline);
		}else{
			f.format("Password Leak: using an unsafe random function on line %d",errorline);
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
	
    public static List<FSMMachineInstance> createNSRStateMachines(SimpleJavaNode node, FSMMachine fsm) {
        String xpathStr = 
            ".//PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='Random']";
        List<ASTClassOrInterfaceType> evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        if ( evalRlts == null || evalRlts.size() == 0 ) {
            return list;
        }
        
        for ( ASTClassOrInterfaceType coit : evalRlts ) {
            FSMMachineInstance fsmi = fsm.creatInstance();
            fsmi.setRelatedObject(new FSMRelatedCalculation(coit));
            fsmi.setResultString("new Random([long]) that is not reliable.");
            list.add(fsmi);
        }
        
        return list;
    }

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("NonSecurityRandomStateMachine::" + str);
		}
	}
}
