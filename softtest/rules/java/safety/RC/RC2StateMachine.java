package softtest.rules.java.safety.RC;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTTypeDeclaration;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.symboltable.java.*;

/**
 * The class contains some static methods for detecting possible File-usage faults of Race
 * condition.
 * 
 * @author yangxiu
 * 
 */
public class RC2StateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("竞争条件模式:Servlet程序的成员变量 '%s' 在 %d 行声明，在 %d 行的输出可能造成其被其他用户看到，造成漏洞",fsmmi.getRelatedVariable(),beginline, errorline);
		}else{
			f.format("RaceCondition: Servlet field '%s' declared on line %d may cause a racecondition on line %d ",fsmmi.getRelatedVariable(),beginline, errorline);
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
     * This static method is used to create RC2StateMathine instances for
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
    @SuppressWarnings("unchecked")
	public static List<FSMMachineInstance> createRC2StateMachines(SimpleJavaNode node,
            FSMMachine fsm) {
    	List list = new LinkedList<FSMMachineInstance>();
    	if ( node == null || fsm == null ) {
    		return list;
    	}
    	
    	if ( !(node instanceof ASTMethodDeclaration) || ((ASTMethodDeclaration)node).isSynchronized() ) {
    		return list;
    	}
    	
    	Node tmp = node.getFirstParentOfType(ASTTypeDeclaration.class);
    	if ( tmp == null ) {
    		return list;
    	}
    	
    	ASTTypeDeclaration typeDecl = (ASTTypeDeclaration) tmp;
    	String xpath1 = "./ClassOrInterfaceDeclaration/ExtendsList" +
    			"/ClassOrInterfaceType[matches(@Image,'Servlet$')]";
    	try {
			if ( typeDecl.findChildNodesWithXPath(xpath1).isEmpty() ) {
				return list;
			}
		} catch (JaxenException e) {
			e.printStackTrace();
            throw new RuntimeException("xpath error",e);
		}
    	
    	String xpath2 = "./ClassOrInterfaceDeclaration/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration" +
    			"/FieldDeclaration[./Type/ReferenceType/ClassOrInterfaceType[@Image='String']]" +
    			"/VariableDeclarator/VariableDeclaratorId";
    	
    	List eval = null;
    	try {
    		eval = typeDecl.findChildNodesWithXPath(xpath2);
    	} catch (JaxenException e) {
    		e.printStackTrace();
            throw new RuntimeException("xpath error",e);
    	}
    	if ( eval.isEmpty() ) {
    		return list;
    	}
    	
    	Hashtable<NameDeclaration,Integer> vstr = new Hashtable<NameDeclaration,Integer>();
    	for ( Object obj : eval ) {
    		ASTVariableDeclaratorId vid = (ASTVariableDeclaratorId) obj;
    		vstr.put(vid.getNameDeclaration(), 0);
    	}
    	eval.clear();
    	
    	String xpath4 = ".//Statement/StatementExpression" +
    			"/PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,'out.print')]]" +
    			"/PrimarySuffix/Arguments/ArgumentList/Expression/" +
    			"/PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name";
    	eval = null;
    	try {
    		eval = node.findChildNodesWithXPath(xpath4);
    	} catch (JaxenException e) {
    		e.printStackTrace();
            throw new RuntimeException("xpath error",e);
    	}
    	if ( eval.isEmpty() ) {
    		return list;
    	}
    	for ( Object obj : eval ) {
    		ASTName vid = (ASTName) obj;
    		if ( vstr.containsKey(vid.getNameDeclaration()) ) {
    			if ( vstr.get(vid.getNameDeclaration()) == 0 ) {
    				vstr.put(vid.getNameDeclaration(), 1);
    			}
    		}
    	}
    	eval.clear();
    	
    	String xpath3 = ".//Statement/StatementExpression[./AssignmentOperator]"
				+ "/PrimaryExpression/PrimaryPrefix/Name";
		eval = null;
		try {
			eval = node.findChildNodesWithXPath(xpath3);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if (eval.isEmpty()) {
			return list;
		}
		
		for (Object obj : eval) {
			ASTName vid = (ASTName) obj;
			if (vstr.get(vid.getNameDeclaration())!=null &&  vstr.get(vid.getNameDeclaration()) == 1 ) {
				vstr.put(vid.getNameDeclaration(), 2);
				list.add(createFSMMachineInstance(fsm, vid.getNameDeclaration()));
			}
		}
		eval.clear();

        return list;
    }
    
    public static boolean checkUnSynchronized(List list, FSMMachineInstance fsmmi) {
    	if ( list == null || fsmmi == null ) {
			return false;
		}
		
		for ( Object obj : list ) {
			if ( obj instanceof ASTName ) {
				if ( ((ASTName)obj).getNameDeclaration().equals(fsmmi.getRelatedVariable()) ) {
					 Node tmp = ((ASTName)obj).getFirstParentOfType(ASTSynchronizedStatement.class);
					 if ( tmp == null ) {
						 fsmmi.setRelatedObject(null);
						 return true;
					 }
					 ASTSynchronizedStatement ss = (ASTSynchronizedStatement) tmp;
					 ASTPrimaryPrefix nm = (ASTPrimaryPrefix) ss.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
					 if ( ! nm.getLabel().equals("this") ) {
						 fsmmi.setRelatedObject(null);
						 return true;
					 }
					 
					 if ( fsmmi.getRelatedObject() == null ) {
						 fsmmi.setRelatedObject(new FSMRelatedCalculation(ss));
					 }
					 else if ( ! ss.equals(fsmmi.getRelatedObject().getTagTreeNode()) ) {
						 fsmmi.setRelatedObject(null);
						 return true;
					 }
				}
			}
		}
		return false;
    }

    /*
    public static boolean checkSynAssign(List list, FSMMachineInstance fsmmi) {
		if ( list == null || fsmmi == null ) {
			return false;
		}
		
		for ( Object obj : list ) {
			if ( obj instanceof ASTName ) {
				if ( ((ASTName)obj).getNameDeclaration().equals(fsmmi.getRelatedVariable()) ) {
					 Node tmp = ((ASTName)obj).getFirstParentOfType(ASTSynchronizedStatement.class);
					 if ( tmp == null ) {
						 continue;
					 }
					 ASTSynchronizedStatement ss = (ASTSynchronizedStatement) tmp;
					 ASTPrimaryPrefix nm = (ASTPrimaryPrefix) ss.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
					 if ( nm.getLabel().equals("this") ) {
						 fsmmi.setRelatedObject(new FSMRelatedCalculation(ss));
						 return true;
					 }
				}
			}
		}
		return false;
	}
	*/
    
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
            NameDeclaration nd) {
        FSMMachineInstance fsmmi = fsm.creatInstance();
        fsmmi.setRelatedVariable((VariableNameDeclaration)nd);
        return fsmmi;
    }

}
