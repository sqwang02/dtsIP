package softtest.rules.java.safety.RC;

import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;

/**
 * The class contains some static methods for detecting possible File-usage faults of Race
 * condition.
 * 
 * @author yangxiu
 * 
 */
public class RC0StateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("竞争条件模式:文件句柄变量 '%s' 在 %d 行声明，在 %d 行使用非原子文件操作，可能造成漏洞",fsmmi.getRelatedVariable(),beginline, errorline);
		}else{
			f.format("RaceCondition: variable '%s' declared on line %d may cause a racecondition on line %d ",fsmmi.getRelatedVariable(),beginline, errorline);
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
    /**
     * names of part of methods from class File.
     */
    private static final String[] FILE_CHECKCALL = { "canRead", "canExecute", "canWrite", "exists",
            "isDirectory", "isFile", "isHidden" };

    private static final String   XPATH_CREATE   = "../../ClassOrInterfaceBodyDeclaration/FieldDeclaration[descendant::/Type/ReferenceType/ClassOrInterfaceType[@Image='File']]/VariableDeclarator/VariableDeclaratorId"
                                                         + "|"
                                                         + "./MethodDeclarator/FormalParameters/FormalParameter[descendant::/Type/ReferenceType/ClassOrInterfaceType[@Image='File']]/VariableDeclaratorId"
                                                         + "|"
                                                         + "./Block//LocalVariableDeclaration[descendant::/Type/ReferenceType/ClassOrInterfaceType[@Image='File']]/VariableDeclarator/VariableDeclaratorId";

    /**
     * This static method is used to create RC0StateMathine instances for
     * {@link ASTConstructorDeclaration} and {@link ASTMethodDeclaration}. It uses {@link XPath}
     * tools to select all variables declared in the method, that is {@link ASTVariableDeclaratorId},
     * in a class declaration, and then creates a independent {@link FSMMachineInstance} for each
     * field declaration.
     * 
     * @param node
     *            the current syntax tree node, the type of parameter should be
     *            {@link ASTCompilationUnit}.
     * @param fsm
     *            the fsm used to create {@link FSMMachineInstance}.
     * @return the list containing all created {@link FSMMachineInstance}.
     */
    public static List<FSMMachineInstance> createRC0StateMachines(SimpleJavaNode node,
            FSMMachine fsm) {
        // find all nodes VariableDeclaratorId
        List vidList = null;
        try {
            vidList = node.findChildNodesWithXPath(XPATH_CREATE);
        } catch (JaxenException e) {
            if (softtest.config.java.Config.DEBUG) {
                e.printStackTrace();
            }
            throw new RuntimeException(
                    "RaceConditionStateMachine.createRC0StateMachines() : xpath error",e);
        }

        // create fsminstances
        List list = new ArrayList();
        for (Object o : vidList) {
            list.add(createFSMMachineInstance(fsm, (ASTVariableDeclaratorId) o));
        }
        return list;
    }

    /**
     * A help method for creating a {@link FSMMachineInstance} instance.
     * 
     * @param fsm
     *            the fsm used to create {@link FSMMachineInstance}.
     * @param vid
     *            the node typeof {@link ASTVariableDeclaratorId} whose {@link NameDeclaration} will
     *            be the related variable of the returned {@link FSMMachineInstance}.
     * @return a newly-created {@link FSMMachineInstance} instance
     */
    private static FSMMachineInstance createFSMMachineInstance(FSMMachine fsm,
            ASTVariableDeclaratorId vid) {
        FSMMachineInstance fsmmi = fsm.creatInstance();
        fsmmi.setRelatedVariable(vid.getNameDeclaration());
        fsmmi.setResultString("checking File-oper racecondition faults");
        return fsmmi;
    }

    /**
     * This method checks the existence of File usages.
     * 
     * @param node
     *            vexnode
     * @param fsmin
     *            fsminstance
     * @return true if there is a file usage in the current method declaration
     */
    public static boolean checkFileUsage(VexNode node, FSMMachineInstance fsmin) {
        if (node == null || fsmin == null) {
            return false;
        }

        if (node.isBackNode() || node.getTreeNode().getConcreteNode() == null) {
            return false;
        }

        VariableNameDeclaration var = fsmin.getRelatedVariable();

        String xPath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'^" + var.getImage()
                + "\\.|^" + var.getImage() + "$')]";
        List evaluationResults = null;
        try {
            evaluationResults = node.getTreeNode().getConcreteNode().findChildNodesWithXPath(xPath);
        } catch (JaxenException e) {
            if (softtest.config.java.Config.DEBUG) {
                e.printStackTrace();
            }
            throw new RuntimeException("RaceConditionStateMachine.checkFileUsage() : xpath error",e);
        }

        for (Object o : evaluationResults) {
            ASTName nn = (ASTName) o;
            if (var.equals(nn.getNameDeclaration())) {
                return true;
            }
        }
        
        return false;

    }

    /**
     * A help method for generating xpath for finding FileTest function call.
     * 
     * @param node
     *            abstract node from syntax tree
     * @param name
     *            the name of the variable
     * @return string represents xpath
     */
    private static String getFileTestXPath(String name) {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append(".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'");

        strBuf.append(name);
        strBuf.append(".");
        strBuf.append(FILE_CHECKCALL[0]);

        for (int i = 1; i < FILE_CHECKCALL.length; ++i) {
            strBuf.append("|");
            strBuf.append(name);
            strBuf.append(".");
            strBuf.append(FILE_CHECKCALL[i]);
        }

        strBuf.append("')]");

        return strBuf.toString();
    }

    /**
     * This method checks FILE_CALL
     * 
     * @param node
     *            vexnode in vex graph
     * @param fsmin
     *            fsminstance
     * @return true if at least one of FILE_CALL exists; false else
     */
    public static boolean checkFileTest(VexNode node, FSMMachineInstance fsmin) {
        if (node == null || fsmin == null) {
            return false;
        }
        if (node.isBackNode() || node.getTreeNode().getConcreteNode() == null) {
            return false;
        }

        String xPath = getFileTestXPath(fsmin.getRelatedVariable().getImage());
        List evaluationResults = null;
        try {
            evaluationResults = node.getTreeNode().getConcreteNode().findChildNodesWithXPath(xPath);
        } catch (JaxenException e) {
            if (softtest.config.java.Config.DEBUG) {
                e.printStackTrace();
            }
            throw new RuntimeException("RaceConditionStateMachine.checkFileCheck() : xpath error",e);
        }

        VariableNameDeclaration namedecl = fsmin.getRelatedVariable();
        for (Object o : evaluationResults) {
            ASTName nn = (ASTName) o;
            if (namedecl.equals(nn.getNameDeclaration())) {
                return true;
            }
        }
        return false;
    }

}
