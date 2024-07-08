package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;
import softtest.rules.java.sensdt.XMLUtil;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTCastExpression;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTTypeArguments;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**   6.1 不合适的强制类型转换
1. 没有进行判断。
【例6-1】 下列程序：
   1   class Filter {
   2   		HashMap len=new HashMap();
   3   		void fill(File dir){
   4   			File[] list = dir.listFiles();
   5   			for (int i = 0; i < list.length; i++) {
   6   				File file = list[i];
   7   				len.put(file,new Long(file.length()));
   8   			}
   9  	 	}
   10   	int getLength(String file){
   11   		Long l = (Long) len.get(file);
   12   		if (l!=null) return l.intValue();
   13   		return 0;
   14   	}
   15   } 
在上述程序中11行，需要在转换之前用instanceOf进行判断一下。

2008-3-24
 */

public class ClassCastStateMachine {

	public static Set<String> containers = new HashSet<String>();
	
	static {
		try{
			List<String> res = XMLUtil.getStrsFromFile("softtest\\rules\\java\\ICTC-Data.xml", "ICTC-Data", "Containers", "Container");
			containers.addAll(res);
		}catch(Exception ex){
			if(Config.DEBUG) {
				ex.printStackTrace();
			}
		}finally{
			// Add the following classes by default
			// Map ----|
			// AbstractMap, Attributes, AuthProvider, ConcurrentHashMap, EnumMap, HashMap,
			// Hashtable, IdentityHashMap, LinkedHashMap, PrinterStateReasons, Properties,
			// Provider, RenderingHints, TabularDataSupport, TreeMap, UIDefaults, WeakHashMap
			// List ----|
			// AbstractList, AbstractSequentialList, ArrayList, AttributeList,
			// CopyOnWriteArrayList, LinkedList, RoleList, RoleUnresolvedList,
			// Stack, Vector 
			containers.add("Map");
			containers.add("AbstractMap");
			containers.add("Attributes");
			containers.add("AuthProvider");
			containers.add("ConcurrentHashMap");
			containers.add("EnumMap");
			containers.add("HashMap");
			containers.add("Hashtable");
			containers.add("IdentityHashMap");
			containers.add("LinkedHashMap");
			containers.add("PrinterStateReasons");
			containers.add("Properties");
			containers.add("Provider");
			containers.add("RenderingHints");
			containers.add("TabularDataSupport");
			containers.add("TreeMap");
			containers.add("UIDefaults");
			containers.add("WeakHashMap");
			containers.add("List");
			containers.add("AbstractList");
			containers.add("AbstractSequentialList");
			containers.add("ArrayList");
			containers.add("AttributeList");
			containers.add("CopyOnWriteArrayList");
			containers.add("LinkedList");
			containers.add("RoleList");
			containers.add("RoleUnresolvedList");
			containers.add("Stack");
			containers.add("Vector");
		}
	}
	
	public static List  getTreeNode(SimpleJavaNode node, String xpathStr) {
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createClassCastStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//CastExpression";
		
		List evalRlts = getTreeNode(node, xpathStr);
		
		for(int i = 0; i < evalRlts.size(); i++) {
			String type;
			ASTCastExpression castExpr = (ASTCastExpression)evalRlts.get(i);
			// 最终转换到的类型(HashMap)(Map)(Ojbect)expr;
			if( null == (type=toppestCast(castExpr))) {
				continue;
			}
			ASTPrimaryExpression  finalPrimExpr = (ASTPrimaryExpression)getCastedExpr(castExpr);
			if(finalPrimExpr.jjtGetChild(0).jjtGetChild(0) instanceof ASTName) {
				ASTName astNm = (ASTName) finalPrimExpr.jjtGetChild(0).jjtGetChild(0);
				
				// 如果当前容器变量是在类范围内的声明*
				if(node instanceof ASTMethodDeclaration) {
					ASTMethodDeclaration astMd = (ASTMethodDeclaration) node;
					if(((VariableNameDeclaration)astNm.getNameDeclaration()).getDeclareScope() == astMd.getScope().getParent()) {
						logc1("**** ---- ****" + astNm.printNode(ProjectAnalysis.getCurrent_file()));
						logc1("----" + ((VariableNameDeclaration)astNm.getNameDeclaration()).getDeclareScope());
						logc1("----" + astMd.getScope().getParent());
					} else {
						logc1("||||" + ((VariableNameDeclaration)astNm.getNameDeclaration()).getDeclareScope());
						logc1("||||" + astMd.getScope().getParent());
						continue;
					}
				}
				if(astNm.getNameDeclaration() instanceof VariableNameDeclaration) {
					VariableNameDeclaration vndecl = (VariableNameDeclaration)astNm.getNameDeclaration();
					// 必须是容器类型
					logc(vndecl.getImage() + "  " + vndecl.getTypeImage());
					if(!isContainer(vndecl.getTypeImage())) {
						continue;
					}else {
						logc1(vndecl.getImage() + ":" + vndecl.getTypeImage() + " is not container type");
					}
					// 必须是非模版类型
					if(isTemplate(vndecl)) {
						continue;
					}
				}
			}
			ASTIfStatement astIf = null;
			SimpleJavaNode parent = castExpr;
			boolean  match = false;
			while(parent!=null && !(parent instanceof ASTMethodDeclaration) ) {
				parent = (SimpleJavaNode) parent.jjtGetParent();
				if( parent instanceof ASTIfStatement ) {
					if( matchInstanceOfExpr(parent, finalPrimExpr, type) ) {
						match = true;
						break;
					}
				}
			}
			if( ! match ) {
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(castExpr));
				list.add(fsmInst);
				fsmInst.setResultString("ICTC");//"Error Cast :" + castExpr.printNode());
			}
		}
		logc1("-----------------------------------------");
		return list;
	}
	
	
	public static boolean checkClassCast(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		logc2("+----------------+");
		logc2("| checkClassCast |" );
		logc2("+----------------+");
		found = true;
		return found;
	}
	
	private static boolean matchInstanceOfExpr(SimpleJavaNode ifstat, SimpleJavaNode expr, String type) {
		boolean  match = false;
		/*String  xstr = ".//InstanceOfExpression";
		List insts = getTreeNode((SimpleJavaNode)ifstat.jjtGetChild(0), xstr);
		for(int i = 0; i < insts.size(); i++) {
			ASTInstanceOfExpression astInst = (ASTInstanceOfExpression)insts.get(i);
			String tp = ((SimpleJavaNode)astInst.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)).getImage();
			// type name match
			if( tp.equals(type)) {
				ASTPrimaryExpression inif = (ASTPrimaryExpression)astInst.jjtGetChild(0);
				if( inif.printNode().equals(expr.printNode())) {
					match = true;
					break;
				}
			}
		}*/
		Object a = null;
		if(a instanceof Integer && (Integer)a>5) {
			Integer i = (Integer) a;
			i = i + 1;
		}
		
		// Just process the form of  if(xx instanceof Xxx)
		ASTExpression  condition = (ASTExpression)ifstat.jjtGetChild(0);
		if( condition.jjtGetChild(0) instanceof ASTInstanceOfExpression ) {
			ASTInstanceOfExpression astInst = (ASTInstanceOfExpression)condition.jjtGetChild(0);
			String tp = ((SimpleJavaNode)astInst.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)).getImage();
			// type name match
			if( tp.equals(type)) {
				ASTPrimaryExpression inif = (ASTPrimaryExpression)astInst.jjtGetChild(0);
				if( inif.printNode(ProjectAnalysis.getCurrent_file()).equals(expr.printNode(ProjectAnalysis.getCurrent_file()))) {
					match = true;
				}
			}
		}
		return match;
	}
	
	/**
	 * If the castExpr is the toppest cast expression, return the image of the type 
	 * (A)(B)(C)obj;  return the A
	 */
	private static String toppestCast(SimpleJavaNode castExpr) {
		if( ! (castExpr instanceof ASTCastExpression )) {
			return null;
		}
		String  str = null;
		SimpleJavaNode parent = (SimpleJavaNode)castExpr.jjtGetParent();
		if(parent instanceof ASTExpression
		&& parent.jjtGetParent() instanceof ASTPrimaryPrefix
		&& parent.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression ) {
			parent = (SimpleJavaNode)parent.jjtGetParent().jjtGetParent().jjtGetParent();
		}
		if(parent instanceof ASTCastExpression ) {
			return null;
		}
		if(castExpr.jjtGetChild(0).jjtGetChild(0) instanceof ASTPrimitiveType) {
			return ((ASTPrimitiveType)castExpr.jjtGetChild(0).jjtGetChild(0)).getImage();
		}
		if(castExpr.jjtGetChild(0).jjtGetChild(0).jjtGetNumChildren() > 0) {
			str = ((ASTClassOrInterfaceType)castExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).getImage();
		}
		
		return str;
	}
	
	private static SimpleJavaNode  getCastedExpr(SimpleJavaNode castExpr) {
		SimpleJavaNode son = castExpr;
		if( son == null ) {
			return null;
		}
		SimpleJavaNode tmp;
		while( true ) {
			if(son instanceof ASTPrimaryExpression) {
				tmp = (SimpleJavaNode)son.getSingleChildofType(ASTCastExpression.class);
				if( tmp == null) {
					break;
				}
				son = tmp;
			}
			else if(son instanceof ASTCastExpression){
				tmp = (SimpleJavaNode)son.jjtGetChild(1);
				if( tmp instanceof ASTCastExpression ) {
					son = tmp;
				} else if(tmp instanceof ASTPrimaryExpression) {
					son = tmp;
				} else {
					break;
				}
			}
		}
		if( ! (son instanceof ASTPrimaryExpression)) {
			logc("?????? Unexpected expression" + son);
			return null;
		}
		return son;
	}
	
	private static boolean isContainer(String typeImage) {
		if(containers.contains(typeImage)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断vndecl是否是模版类型的变量
	 */
	private static boolean isTemplate(VariableNameDeclaration vndecl) {
		SimpleJavaNode node = (SimpleJavaNode)vndecl.getNode();
		SimpleJavaNode declaration = (SimpleJavaNode)node.jjtGetParent().jjtGetParent();
		ASTClassOrInterfaceType astCI = (ASTClassOrInterfaceType)declaration.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
		if(astCI.jjtGetNumChildren() > 0 && astCI.jjtGetChild(0) instanceof ASTTypeArguments) {
			return true;
		}
		//logc("||" + node.printNode() + "   " + node.getClass());
		return false;
	}
	
	public static void logc1(String str) {
		logc("createClassCastStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkClassCast(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("ClassCastStateMachine::" + str);
		}
	}
}
