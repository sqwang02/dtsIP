package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;

/**
 * State machine class for detecting risks called "Wrong Synchronized".
 * 
 * @author yangxiu
 * 
 */
public class WS2StateMachine {
    private final static String XPATH1 = "./TypeDeclaration/ClassOrInterfaceDeclaration[" +
    		"descendant::/ExtendsList/ClassOrInterfaceType[@Image='Thread'] or " +
    		"descendant::/ImplementsList/ClassOrInterfaceType[@Image='Runnable'] or " +
    		"descendant::/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration[" +
    		    "@Synchronized='true' or  " +
    		    "descendant::/Block//SynchronizedStatement]]";
    
    private final static String XPATH2 = "./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration" +
    		"/FieldDeclaration[@Final='false' and @Volatile='false' and @Static='true']" +
    		"/VariableDeclarator/VariableDeclaratorId";
    
    /**
     * This static method is used to create WS2StateMathine instances for {@link ASTCompilationUnit}.
     * 
     * @param node
     *            the current syntax tree node, the type of parameter should be
     *            {@link ASTCompilationUnit}.
     * @param fsm
     *            the fsm used to create {@link FSMMachineInstance}.
     * @return the list containing all created {@link FSMMachineInstance}.
     */
    public static List<FSMMachineInstance> createWS2StateMachines(SimpleJavaNode node,
            FSMMachine fsm) {
        // select class nodes from a.s.t. which may be used in multithread environment.
        List mtclasses = null;
        try {
            mtclasses = node.findChildNodesWithXPath(XPATH1);
        } catch (JaxenException e) {
            if (softtest.config.java.Config.DEBUG) {
                e.printStackTrace();
            }
            throw new RuntimeException("WS2StateMachine.createWS2StateMachines() : xpath error",e);
        }
        
        
        List<FSMMachineInstance> machineList = new LinkedList<FSMMachineInstance>( );
        for ( Object o : mtclasses ) {
            SimpleJavaNode classnode = (SimpleJavaNode) o;
            
            // select all variabledeclaratorid nodes from classnode's descendants which are no-final,
            // no-volatile and static.
            List varids = null;
            try {
                varids = classnode.findChildNodesWithXPath(XPATH2);
            } catch (JaxenException e) {
                if (softtest.config.java.Config.DEBUG) {
                    e.printStackTrace();
                }
                throw new RuntimeException("WS2StateMachine.createWS2StateMachines() : xpath error",e);
            }
            
            // create fsm machine instance for each variabledeclaratorid.
            for ( Object oo : varids ) {
                ASTVariableDeclaratorId varid = (ASTVariableDeclaratorId) oo;
                FSMMachineInstance fsmin = fsm.creatInstance();
                fsmin.setRelatedVariable(varid.getNameDeclaration());
                fsmin.setRelatedObject(new FSMRelatedCalculation(classnode));
                machineList.add(fsmin);
            }
        }

        return machineList;
    }

    /**
     * This static method is used to check whether the variable related with fsmin is synchronized.
     * 
     * @param node
     *            vexnode, the default value is null
     * @param fsmin
     *            FSMMachineInstance
     * @return true all synchronized or all not synchronized false otherwise
     */
    public static boolean checkSynchronized(VexNode node, FSMMachineInstance fsmin) {
        VariableNameDeclaration var = fsmin.getRelatedVariable();
        String classname = fsmin.getRelatedObject().getTagTreeNode().getImage();

        // get all occurrences of the variable
        List occs = var.getOccs();
        if (occs == null || occs.isEmpty()) {
            return false;
        }

        // check every occurrence if synchronized
        for (Object o : occs) {
            NameOccurrence occ = (NameOccurrence) o;
            if (occ.getLocation() == null || occ.getLocation().getScope() == null) {
                continue;
            }

            // get the nearest top MethodScope
            SimpleJavaNode nd = (SimpleJavaNode) occ.getLocation();
            Node methodDeclaration = nd.getFirstParentOfType(ASTMethodDeclaration.class);
            if (methodDeclaration == null) {
                continue;
            }
            ASTMethodDeclaration mdnode = (ASTMethodDeclaration) methodDeclaration;
            
            // is static ?
            if (!mdnode.isStatic()) {
                continue;
            }

           // is synchronized ?
            if (mdnode.isSynchronized()) {
                // this method is synchronized
                // do nothing
            } else {
                // this method 'ASTMethodDeclaration' is not synchronized
                // then check if there is a code block, "synchronized (classname.java) {}"
                Node synStatement = nd.getFirstParentOfType(ASTSynchronizedStatement.class,
                        methodDeclaration);
                while (synStatement != null) {
                    ASTSynchronizedStatement syn = (ASTSynchronizedStatement) synStatement;
                    try {
                        List nodes = syn.findChildNodesWithXPath("./Expression[@printNode='"+classname+"']");
                        if (nodes.size() == 1) {
                            break;
                        }
                    } catch (JaxenException e) {
                        throw new RuntimeException(
                                "WS2StateMachine.checkSynchronized():xpath error",e);
                    }
                    synStatement = ((SimpleNode) synStatement).getFirstParentOfType(
                            ASTSynchronizedStatement.class, methodDeclaration);
                }

                if (synStatement == null) {
                    return true;
                }
            }
        }

        return false;
    }
}
