package softtest.rules.java;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.*;

import java.util.*;

import softtest.ast.java.*;

/**
 * 
 * @author yangxiu
 * 
 */
public class WS1StateMachine {
    /**
     * This static method is used to create WS1StateMathine instances for {@link ASTCompilationUnit}.
     * 
     * @param node
     *            the current syntax tree node, the type of parameter should be
     *            {@link ASTCompilationUnit}.
     * @param fsm
     *            the fsm used to create {@link FSMMachineInstance}.
     * @return the list containing all created {@link FSMMachineInstance}.
     */
    public static List<FSMMachineInstance> createWS1StateMachines(SimpleJavaNode node,
            FSMMachine fsm) {
        // create FSMMachinInstance for each field
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        FSMMachineInstance fsminstance = fsm.creatInstance();
        fsminstance.setRelatedObject(new FSMRelatedCalculation(node));
        fsminstance.setResultString("useless keyword \"synchronized\" when implementing \"interface Serializable\"");
        list.add(fsminstance);

        return list;
    }
}
