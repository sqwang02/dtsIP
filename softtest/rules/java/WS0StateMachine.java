package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;

/**
 * State machine class for detecting risks called "Wrong Synchronization". class WS0StateMachine
 * checks all fields declared in a class whether are all synchronized or all not synchronized for
 * all appearances in the class's methods.
 * 
 * @author yangxiu
 * 
 */
public class WS0StateMachine {
    private final static String XPATH      = "./TypeDeclaration/ClassOrInterfaceDeclaration"
                                                   + "/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration"
                                                   + "/FieldDeclaration[@Final='false']/VariableDeclarator"
                                                   + "/VariableDeclaratorId";
    private final static String XPATH_THIS = "./Expression/PrimaryExpression"
                                                   + "/PrimaryPrefix[matches(@Label,'this')]";

    /**
     * This static method is used to create WS0StateMathine instances for {@link ASTCompilationUnit}.
     * It uses {@link XPath} tools to select all field declaration nodes, that is
     * {@link ASTFieldDeclaration}, in a class declaration, and then creates a independent
     * {@link FSMMachineInstance} for each field declaration.
     * 
     * @param node
     *            the current syntax tree node, the type of parameter should be
     *            {@link ASTCompilationUnit}.
     * @param fsm
     *            the fsm used to create {@link FSMMachineInstance}.
     * @return the list containing all created {@link FSMMachineInstance}.
     */
    public static List<FSMMachineInstance> createWS0StateMachines(SimpleJavaNode node,
            FSMMachine fsm) {
        // find all nodes named VariableDeclaratorId which are the descendants
        // of the node named FieldDeclaration
        List fieldVariables = null;
        try {
            fieldVariables = node.findChildNodesWithXPath(XPATH);
        } catch (JaxenException e) {
            if (softtest.config.java.Config.DEBUG) {
                e.printStackTrace();
            }
            throw new RuntimeException("RC1StateMachine.createWS0StateMachines() : xpath error",e);
        }

        // create FSMMachinInstance for each field
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        for (Object o : fieldVariables) {
            ASTVariableDeclaratorId varId = (ASTVariableDeclaratorId) o;
            FSMMachineInstance fsminstance = fsm.creatInstance();
            fsminstance.setRelatedVariable(varId.getNameDeclaration());
            fsminstance.setResultString("checking field-synchronized usage");
            list.add(fsminstance);
        }

        return list;
    }

    /**
     * This static method is used to check the variable related with fsmin is all synchronized or
     * all not synchronized for all appearances in methods.
     * 
     * @param node
     *            vexnode, the default value is null
     * @param fsmin
     *            FSMMachineInstance
     * @return true all synchronized or all not synchronized false otherwise
     */
    public static boolean checkSynchronized(VexNode node, FSMMachineInstance fsmin) {
        VariableNameDeclaration var = fsmin.getRelatedVariable();

        // get all occurrences of the variable
        List occs = var.getOccs();
        if (occs == null || occs.isEmpty()) {
            return false;
        }

        // check every occurrence if synchronized
        // the list of all methods is for reporting more detailed informations in the future.
        // List methods = new ArrayList();
        List issyns = new ArrayList();
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

            if (mdnode.isSynchronized()) {
                // this method is synchronized
                issyns.add(true);
            } else {
                // this method 'ASTMethodDeclaration' is not synchronized
                // then check if there is a code block, "synchronized (this) {}"
                Node synStatement = nd.getFirstParentOfType(ASTSynchronizedStatement.class,
                        methodDeclaration);
                while (synStatement != null) {
                    ASTSynchronizedStatement syn = (ASTSynchronizedStatement) synStatement;
                    try {
                        List nodes = syn.findChildNodesWithXPath(XPATH_THIS);
                        if (nodes.size() == 1) {
                            break;
                        }
                    } catch (JaxenException e) {
                        throw new RuntimeException(
                                "RC1StateMachine.checkSynchronized():xpath error",e);
                    }
                    synStatement = ((SimpleNode) synStatement).getFirstParentOfType(
                            ASTSynchronizedStatement.class, methodDeclaration);
                }

                if (synStatement != null) {
                    issyns.add(true);
                } else {
                    issyns.add(false);
                }
            }
        }

        // if some is synchronized and some is not synchronized, then return true, that is to report
        // an error.
        return issyns.contains(Boolean.TRUE) && issyns.contains(Boolean.FALSE);
    }
}
