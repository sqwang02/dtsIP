package softtest.rules.java.safety.AAPI;

import java.io.File;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import softtest.ast.java.*;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.jaxen.java.DocumentNavigator;
import softtest.rules.java.AbstractStateMachine;
import softtest.rules.java.sensdt.XMLUtil;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

/**       安全缺陷模型  （二） 滥用API
（4）	故障名称：未检查返回值
	故障描述：没有检查方法的返回值，从而造成信息的泄露。
	举例：
FileInputStream fis;
byte[] byteArray = new byte[1024];
for (Iterator i=users.iterator(); i.hasNext();) {
    String userName = (String) i.next();
    String pFileName = PFILE_ROOT + "/" + userName;
    FileInputStream fis = new FileInputStream(pFileName);   
    fis.read(byteArray); // the file is always 1k bytes
    fis.close();
	processPFile(userName, byteArray);
}

原因分析：
程序中没有检查read()的返回值，这可能会造成私有数据在用户间发生泄漏。

如果在意外情况下，某个文件的长度小于编程人员假设的1024，则在byteArray中存在前一
次循环中其他用户拥有的数据，从而导致前一用户的数据漏入当前用户中。

2008-06-25

检测方法：非路径相关，查找在循环中的.read方法，判断其使用的文件对象能够根据循环
而改变，同时字节数组没有复位或重新赋值操作。
在.read之前的循环体内部存在输入对象的创建操作，
*/
public class AbuseAPI_UncheckReturnStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式: %d 行没有检查read方法的返回值，可能造成信息泄漏。没有检查函数的返回值，会造成私有数据在用户间发生泄漏。", errorline);
		}else{
			f.format("Abuse Application Program Interface: missing checking the return vlaue of mehtod read on line %d.No checking returned value，it may leak the data.",errorline);
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
	
	private static String escapeFilePath(String path) {
		String ret1 = path.replace('/', File.separatorChar);
		String ret2 = ret1.replace('\\', File.separatorChar);
		return ret2;
	}
	/**
	 * contains all the classes that are to do input
	 */
	public static Set<String>  clses = null;

	static {
		try{
			clses = new HashSet<String>();
			/**  get string name of classes, which can do read(char[]) operation  **/
			List<String>  ret = XMLUtil.getStrsFromFile(escapeFilePath("softtest\\rules\\java\\safety\\AAPI\\AAPI-Data"), "AAPI-Data", "Class");
			clses.addAll(ret);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Get SensInfo failed.",ex);
		}
	}

	/**
	 * 
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createUncheckReturnStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		String  xpathStr = ".//Name[ matches(@Image, \'\\.read\') and ../../PrimarySuffix/Arguments[@ArgumentCount=\'1\'] ]";
		
		List evalRlts = findTreeNodes(node , xpathStr);

		if( evalRlts == null || evalRlts.size() == 0) {
			logc1("No .read found");
		}
		//Hashtable<String, String>  literals = new Hashtable<String, String>();
		for( int i = 0; i < evalRlts.size(); i++ ) {
			ASTName  astNameFis = (ASTName) evalRlts.get(i);
			NameDeclaration ndeclFis = astNameFis.getNameDeclaration();
			
			
			if( ! (ndeclFis instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration vdeclFis = (VariableNameDeclaration)ndeclFis;
			/**  必须是规定的几个输入类对象   **/
			if( ! clses.contains(vdeclFis.getTypeImage()) ) {
				continue;
			}
			
			/**  必须没有赋值  xx = yy.read(..) **/
			if( hasAssignment(astNameFis) ) {
				continue;
			}
			
			/**  必须没有初始化赋值  int xx = yy.read(..) **/
			if( hasInit(astNameFis) ) {
				continue;
			}
			
			FSMMachineInstance fsmInst;
			
			//logc1("create ExtendAlias :" + astLiter.getImage());
			
			fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astNameFis));
			fsmInst.setResultString(astNameFis.getImage());
			list.add( fsmInst );
			
			
			//  必须在循环中  
			/*SimpleJavaNode loopNode = getLoopNode(astNameFis); 
			if( null == loopNode ) {
				continue;
			}
			
			//  获取唯一的参数中的数组对象的读语句中的出现  
			NameOccurrence  aryOcc = null;
			ASTName aryName = getArgu0(astNameFis);
			NameDeclaration aryDecl = aryName.getNameDeclaration();
			VariableNameDeclaration vAryDecl = null;
			
			ASTBlockStatement astBs = (ASTBlockStatement)astNameFis.getFirstParentOfType(ASTBlockStatement.class);
			List<VexNode> vexNodes = astNameFis.getCurrentVexList();
			VexNode vex = vexNodes.get(0);
			List<NameOccurrence> occs = vex.getOccurrences();
			boolean  arg0IsArray = false;
			
			for(NameOccurrence occ : occs) {
				NameDeclaration ndeclo = occ.getDeclaration();
				//  判断是否是 xx.read 中对应的 xx 的声明，如果是，则当前的 occ 就是输入对象的 occ  
				if( ! (ndeclo instanceof VariableNameDeclaration) ) {
					continue;
				}
				vAryDecl = (VariableNameDeclaration) ndeclo;
				if( ! vAryDecl.isArray() ) {
					vAryDecl = null;
					continue;
				}
				aryOcc = occ;
				break;
			}
			
			//  数组的声明必须在当前循环的范围之外  
			Scope aryScop  = vAryDecl.getDeclareScope();
			Scope loopScop = loopNode.getScope();
			if(aryScop.isSelfOrAncestor(loopScop)) {
				continue;
			}
			// 数组在循环的范围内有显式赋值 ary[..] = xx;  
			if( existsAssign2Name(loopNode, aryName.getImage()) ) {
				continue;
			}
			
			//  在循环中寻找输入对象的赋值，或者是该对象在循环中声明  
			boolean  existsAsgn2Name = false;
			boolean  existsDecl2Name = false;
			if( existsAlloc2Name(loopNode, vdeclFis.getImage()) ) {
				//  检查右边是否为 new 表达式，并且参数不是常量  
				existsAsgn2Name = true;
			} else
			if( existsDeclInNode(loopNode, vdeclFis.getImage()) ) {
				existsDecl2Name = true;
			}
			
			// 如果存在赋值或者声明，都表明得到的是新的 输入 对象
			if(existsAsgn2Name || existsAsgn2Name) {
				FSMMachineInstance fsmInst;
				
				//logc1("create ExtendAlias :" + astLiter.getImage());
				
				fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(astNameFis));
				list.add( fsmInst );
			}*/
			
			
		}
		return list;
	}

	
	/***
	 * 寻找 loopNode 下面的对 astN 的赋值。
	 * @param astN
	 * @return
	 */
	private static boolean  existsDeclInNode(SimpleJavaNode loopNode, String  img) {
		String  xpath = ".//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId[ @Image='" + img + "' ]";
		List  relt = findTreeNodes(loopNode, xpath);
		if(relt.size() > 0) {
			return true;
		}
				
		return false;
	}
	
	/***
	 * 寻找 loopNode 下面的对 astN 的赋值。
	 * @param img 变量名
	 */
	private static boolean  existsAssign2Name(SimpleJavaNode loopNode, String  img) {
		String  xpath = ".//Name[ @Image='" + img + "' and ./../../../AssignmentOperator ]";
		List  relt = findTreeNodes(loopNode, xpath);
		for(int i = 0; i < relt.size(); i++) {
			ASTName ast = (ASTName) relt.get(i);
			/**  如果 astN 出现在赋值运算符的左边  **/
			if(ast.jjtGetParent().jjtGetParent() == ast.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(0)) {
				return true;				
			}
		}
		return false;
	}
	
	/***
	 * 寻找 loopNode 下面的对 astN 的 allocation 赋值。并且 new Xxx(..)里面是非常量
	 * @param img 变量名
	 */
	private static boolean  existsAlloc2Name(SimpleJavaNode loopNode, String  img) {
		String  xpath = ".//Name[ @Image='" + img + "' and ./../../../AssignmentOperator ]";
		List  relt = findTreeNodes(loopNode, xpath);
		for(int i = 0; i < relt.size(); i++) {
			ASTName ast = (ASTName) relt.get(i);
			/**  astN 必须出现在赋值运算符的右边  **/
			if(ast.jjtGetParent().jjtGetParent() != ast.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(0)) {
				continue;
			}
			SimpleJavaNode stmt = (SimpleJavaNode)ast.jjtGetParent().jjtGetParent().jjtGetParent();
			if(stmt.jjtGetNumChildren() != 3) {
				continue;
			}
			ASTAllocationExpression astAlloc = (ASTAllocationExpression)((SimpleJavaNode)stmt.jjtGetChild(2)).getSingleChildofType(ASTAllocationExpression.class);
			if(null == astAlloc || astAlloc.jjtGetNumChildren() < 2) {
				continue;
			}
			ASTClassOrInterfaceType cils = (ASTClassOrInterfaceType)astAlloc.jjtGetChild(0);
			if( ! clses.contains(cils.getImage()) ) {
				continue;
			}
			ASTArguments argu = (ASTArguments)astAlloc.jjtGetChild(1);
			if(argu.getArgumentCount() < 1) {
				continue;
			}
			/**  确保参数不全为常量  **/
			ASTArgumentList argList = (ASTArgumentList)argu.jjtGetChild(0);
			for(int j = 0; j < argList.jjtGetNumChildren(); j++) {
				ASTExpression argi = (ASTExpression) argList.jjtGetChild(j);
				if(null != argi.getSingleChildofType(ASTLiteral.class)
				|| null != argi.getSingleChildofType(ASTNullLiteral.class)
				|| null != argi.getSingleChildofType(ASTBooleanLiteral.class)) {
					continue;
				}
			}
			if(i == relt.size() - 1) {
				return true;
			}
		}
		return false;
	}
	
	/****
	 * 获得 xx.read(ary) 中的ary对应的 ASTName 节点
	 * @param astN
	 * @return
	 */
	private static ASTName  getArgu0(SimpleJavaNode astN) {
		SimpleJavaNode p = (SimpleJavaNode)astN.jjtGetParent().jjtGetParent();
		if(p.jjtGetNumChildren() != 2) {
			return null;
		}
		ASTName ret = (ASTName)((SimpleJavaNode)p.jjtGetChild(1)).getSingleChildofType(ASTName.class);
		return ret;
	}
	
	/**
	 * 判断在 xx.read(..) 的 xx.read 对应的 ASTName 节点的父节点中是否有赋值节点
	 * @param astN
	 * @return
	 */
	private static boolean  hasAssignment(SimpleJavaNode astN) {
		SimpleJavaNode p = (SimpleJavaNode)astN.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent();
		if(p.jjtGetNumChildren() == 3 && p.jjtGetChild(1) instanceof ASTAssignmentOperator) {
			return true;
		}
		return false;
	}
	
	private static boolean  hasInit(SimpleJavaNode astN) {
		SimpleJavaNode p = (SimpleJavaNode)astN.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent();
		if(p instanceof ASTVariableInitializer) {
			return true;
		}
		return false;
	}
	
	/**
	 * 检查astN对应的语句是否在循环里,并且要求是
	 * @param astN
	 * @return
	 */
	private static SimpleJavaNode getLoopNode(SimpleJavaNode astN) {
		SimpleJavaNode p = (SimpleJavaNode)astN.jjtGetParent();
		while(p!=null && !(p instanceof ASTBlockStatement)) {
			p = (SimpleJavaNode) p.jjtGetParent();
		}
		if(p != null) {
			while(p!=null) {
				if(p instanceof ASTForStatement
				|| p instanceof  ASTWhileStatement
				|| p instanceof  ASTDoStatement) {
					return p;
				}
				p = (SimpleJavaNode) p.jjtGetParent();
			}
		}
		return null;
	}
	
	private static List findTreeNodes(SimpleJavaNode node, String xPath) {
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
	

	public static void logc1(String str) {
		logc("createStateMachines(..) - " + str);
	}
	
	public static void logc2(String str) {
		logc("check(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("AAPI_UncheckReturnStateMachine::" + str);
		}
	}

	public static void main(String args[] ) {
		String  str = "System.out.println;";
		String strs[] = str.split("\\.");
		for( String s : strs ) {
			System.out.println(s);
		}
		str = "\"ab\"";
		System.out.println( str.substring(1, str.length() - 1)  );
	}
}
