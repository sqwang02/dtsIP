package softtest.rules.java.safety.IEH;

import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;

/**
 * 
 */

public class IEHStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("不合理的异常处理模式: %d 行的异常处理不合适", errorline);
		}else{
			f.format("Improper Exception Handle: Improper Exception Handle on line %d",errorline);
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
	 * 
	 * @param n ASTCatchStatement
	 * @param fsmmi
	 * @return
	 */
	private static boolean checkCatchReturn(SimpleJavaNode n, FSMMachineInstance fsmmi) {
		String xpath="./Block/BlockStatement/Statement[./ThrowStatement or ./ReturnStatement]";
		List eval = null;
		try {
			eval = n.findChildNodesWithXPath(xpath);
		} catch (JaxenException e) {
			e.printStackTrace();
            throw new RuntimeException("xpath error",e);
		}
		
		return !eval.isEmpty();
	}
	
	/**
	 * 
	 * @param n ASTCatchStatement
	 * @param fsmmi
	 * @return
	 */
	private static boolean checkCatchUnlock(SimpleJavaNode n, FSMMachineInstance fsmmi) {
		String xpath = ".//PrimaryExpression[" +
				"./PrimarySuffix/Arguments[@ArgumentCount=0]]" +
				"/PrimaryPrefix/Name[matches(@Image,'^[a-zA-Z_0-9]+\\.unlock$')]";
		List eval = null;
		try {
			eval = n.findChildNodesWithXPath(xpath);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error");
		}

		for (Object obj : eval) {
			ASTName name = (ASTName) obj;
			if (name.getNameDeclaration().equals(fsmmi.getRelatedVariable())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param n ASTFinallyStatement
	 * @param fsmmi
	 * @return
	 */
	private static boolean checkFinallyUnlock(SimpleJavaNode n, FSMMachineInstance fsmmi) {
		String xpath="./Block" +
				"/BlockStatement/Statement/StatementExpression" +
				"/PrimaryExpression[./PrimarySuffix/Arguments[@ArgumentCount=0]]" +
				"/PrimaryPrefix/Name[matches(@Image,'^[a-zA-Z_0-9]+\\.unlock$')]";
		List eval = null;
		try {
			eval = n.findChildNodesWithXPath(xpath);
		} catch (JaxenException e) {
			e.printStackTrace();
            throw new RuntimeException("xpath error");
		}
		
		for ( Object obj : eval ) {
			ASTName name = (ASTName) obj;
			if ( name.getNameDeclaration().equals(fsmmi.getRelatedVariable()) ) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkTryError(VexNode n, FSMMachineInstance fsmmi) {
		if ( n.isBackNode() || !(n.getTreeNode() instanceof ASTTryStatement) ) {
			return false;
		}
		
		ASTTryStatement tr = (ASTTryStatement) n.getTreeNode();		
		List ff = tr.findDirectChildOfType(ASTFinallyStatement.class);
		if ( !ff.isEmpty() && checkFinallyUnlock( (SimpleJavaNode) ff.get(0), fsmmi ) ) {
			return false;
		}
		
		List catchList = tr.findDirectChildOfType(ASTCatchStatement.class);
		for ( Object obj : catchList ) {
			ASTCatchStatement cat = (ASTCatchStatement) obj;
			if ( checkCatchReturn(cat, fsmmi) && !checkCatchUnlock(cat, fsmmi) ) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean checkTryUnlock(VexNode n, FSMMachineInstance fsmmi) {
		if ( n.isBackNode() || !(n.getTreeNode() instanceof ASTTryStatement) ) {
			return false;
		}
		if ( checkTryError(n, fsmmi) ) {
			return false;
		}
		ASTTryStatement tr = (ASTTryStatement) n.getTreeNode();
		List ff = tr.findDirectChildOfType(ASTFinallyStatement.class);
		if ( !ff.isEmpty() && checkFinallyUnlock( (SimpleJavaNode) ff.get(0), fsmmi ) ) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkUnlockOutsideTry(VexNode n, FSMMachineInstance fsmmi) {
		if ( !n.isBackNode() || !(n.getTreeNode() instanceof ASTTryStatement) ) {
			return false;
		}
		if ( !checkTryError(n, fsmmi) ) {
			return false;
		}
		ASTTryStatement tr = (ASTTryStatement) n.getTreeNode();
		return checkFinallyUnlock(
				(SimpleJavaNode) tr.findDirectChildOfType(ASTFinallyStatement.class).get(0),
				fsmmi);
	}
	
	public static boolean checkNoUnlock(List list, FSMMachineInstance fsmmi) {
		if ( list == null || fsmmi == null ) {
			return false;
		}
		
		String xpath = ".//PrimaryExpression[./PrimarySuffix/Arguments[@ArgumentCount=0]]/PrimaryPrefix/Name[matches(@Image,\"^[a-zA-Z_0-9]+\\.unlock$\")]";
		
		for ( Object obj : list ) {
			ASTCatchStatement cat = (ASTCatchStatement)obj;
			List eval = null;
			try {
				eval = cat.findChildNodesWithXPath(xpath);
			} catch (JaxenException e) {
				e.printStackTrace();
	            throw new RuntimeException("xpath error");
			}
			boolean flag = true;
			for ( Object o : eval ) {
				if ( ((ASTName)o).getNameDeclaration().equals(fsmmi.getRelatedVariable()) ) {
					flag = false;
					break;
				}
			}
			if ( flag ) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkNoNameDeclaration(List list, FSMMachineInstance fsmmi) {
		if ( list == null || fsmmi == null ) {
			return false;
		}
		
		for ( Object obj : list ) {
			if ( obj instanceof ASTName ) {
				if ( ((ASTName)obj).getNameDeclaration().equals(fsmmi.getRelatedVariable()) ) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean checkNameDeclaration(List list, FSMMachineInstance fsmmi) {
		if ( list == null || fsmmi == null ) {
			return false;
		}
		
		for ( Object obj : list ) {
			if ( obj instanceof ASTName ) {
				//可以直接使用==判断是否相等，使用equals反而可能造成空指针异常，xqing 2008-09-27
				if ( ((ASTName)obj).getNameDeclaration()==(fsmmi.getRelatedVariable()) ) {
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createIEHStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配具有3个参数的DriverManager.getConnection调用，且要求第三个参
         * 数是一个变量，返回结点是第三个参数对应的ASTName对象。例如：
         * connection = DriverManager.getConnection(a, b, c);
         * connection = DriverManager.getConnection(a, b, pass);
         */
        String xpathStr = 
            ".//StatementExpression/PrimaryExpression[./PrimarySuffix/Arguments[@ArgumentCount=0]]" +
            "/PrimaryPrefix/Name[matches(@Image,\"^[a-zA-Z_0-9]+\\.lock$\")]";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error");
        }

        if (evalRlts == null || evalRlts.size() <= 0) {
            return list;
        }

        /*
         * table-用于去重的hash表
         */
        Hashtable<NameDeclaration, FSMMachineInstance> table = 
            new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            ASTName lockName = (ASTName) obj;
            if (!(lockName.getNameDeclaration() instanceof VariableNameDeclaration)) {
                continue;
            }

            VariableNameDeclaration vDecl = (VariableNameDeclaration) lockName.getNameDeclaration();
            if (vDecl.getTypeImage().endsWith("Lock") && !table.containsKey(vDecl)) {
                FSMMachineInstance fsmInst = fsm.creatInstance();
                fsmInst.setRelatedVariable(vDecl);
                table.put(vDecl, fsmInst);
            }
        }

        for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
            list.add(e.nextElement());
        }

        return list;
    }
    
    /**
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createIEHAStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配
         */
        String xpathStr = ".//ThrowStatement[.//AllocationExpression//ArgumentList/Expression//Name]";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error");
        }

        if (evalRlts == null || evalRlts.size() <= 0) {
            return list;
        }

        for (Object obj : evalRlts) {
        	ASTThrowStatement ts = (ASTThrowStatement) obj;
        	List names = ts.findChildrenOfType(ASTName.class);
        	
        	if ( names == null || names.isEmpty() ) {
        		continue;
        	}
        	
        	for (Object obj1 : names) {
        		ASTName fileName = (ASTName) obj1;
                if (!(fileName.getNameDeclaration() instanceof VariableNameDeclaration)) {
                    continue;
                }

                VariableNameDeclaration vDecl = (VariableNameDeclaration) fileName.getNameDeclaration();
                if ((vDecl.getTypeImage().equalsIgnoreCase("file") || 
                		vDecl.getTypeImage().equalsIgnoreCase("string") && vDecl.getImage().toLowerCase().indexOf("file") >= 0 ) 
                		) {
                    FSMMachineInstance fsmInst = fsm.creatInstance();
                    fsmInst.setRelatedObject(new FSMRelatedCalculation(ts));
                    fsmInst.setResultString("var "+fileName.getImage()+" exposes file path");
                    list.add(fsmInst);
                    break;
                }
        	}
        }

        return list;
    }
    
    /**
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createIEHBStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配
         */
        String xpathStr = "//ThrowStatement//ArgumentList//PrimaryPrefix/Name";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error");
        }

        if (evalRlts == null || evalRlts.size() <= 0) {
            return list;
        }

        /*
         * table-用于去重的hash表
         */
        Hashtable<NameDeclaration, FSMMachineInstance> table = 
            new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            ASTName fileName = (ASTName) obj;
            if (!(fileName.getNameDeclaration() instanceof VariableNameDeclaration)) {
                continue;
            }

            VariableNameDeclaration vDecl = (VariableNameDeclaration) fileName.getNameDeclaration();
            if ((vDecl.getTypeImage().equalsIgnoreCase("file") || 
            		vDecl.getTypeImage().equalsIgnoreCase("string") && vDecl.getImage().indexOf("file") >= 0 ) 
            		&& !table.containsKey(vDecl)) {
                FSMMachineInstance fsmInst = fsm.creatInstance();
                fsmInst.setRelatedVariable(vDecl);
                table.put(vDecl, fsmInst);
            }
        }

        for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
            list.add(e.nextElement());
        }

        return list;
    }
    
    public static boolean checkThrowInCatch(VexNode n, FSMMachineInstance fsmmi) {
		if (n == null || !n.isBackNode() || n.getTreeNode() == null 
				|| !(n.getTreeNode() instanceof ASTTryStatement) ) {
			return false;
		}
		
		ASTTryStatement tr = (ASTTryStatement) n.getTreeNode();
		List catchList = tr.findDirectChildOfType(ASTCatchStatement.class);
		String xpath = ".//ThrowStatement/Expression/PrimaryExpression" +
				"/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList" +
				"/Expression/AdditiveExpression/PrimaryExpression/PrimaryPrefix/Name";
		for ( Object obj : catchList ) {
			ASTCatchStatement cat = (ASTCatchStatement) obj;
			List eval = null;
			try {
				eval = cat.findChildNodesWithXPath(xpath);
			} catch (JaxenException e) {
				e.printStackTrace();
	            throw new RuntimeException("xpath error");
			}
			if ( checkNameDeclaration(eval, fsmmi)) {
				return true;
			}
		}
		
		return false;
	}

}
