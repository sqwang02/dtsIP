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

/**       ��ȫȱ��ģ��  ������ ����API
��4��	�������ƣ�δ��鷵��ֵ
	����������û�м�鷽���ķ���ֵ���Ӷ������Ϣ��й¶��
	������
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

ԭ�������
������û�м��read()�ķ���ֵ������ܻ����˽���������û��䷢��й©��

�������������£�ĳ���ļ��ĳ���С�ڱ����Ա�����1024������byteArray�д���ǰһ
��ѭ���������û�ӵ�е����ݣ��Ӷ�����ǰһ�û�������©�뵱ǰ�û��С�

2008-06-25

��ⷽ������·����أ�������ѭ���е�.read�������ж���ʹ�õ��ļ������ܹ�����ѭ��
���ı䣬ͬʱ�ֽ�����û�и�λ�����¸�ֵ������
��.read֮ǰ��ѭ�����ڲ������������Ĵ���������
*/
public class AbuseAPI_UncheckReturnStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ: %d ��û�м��read�����ķ���ֵ�����������Ϣй©��û�м�麯���ķ���ֵ�������˽���������û��䷢��й©��", errorline);
		}else{
			f.format("Abuse Application Program Interface: missing checking the return vlaue of mehtod read on line %d.No checking returned value��it may leak the data.",errorline);
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
			/**  �����ǹ涨�ļ������������   **/
			if( ! clses.contains(vdeclFis.getTypeImage()) ) {
				continue;
			}
			
			/**  ����û�и�ֵ  xx = yy.read(..) **/
			if( hasAssignment(astNameFis) ) {
				continue;
			}
			
			/**  ����û�г�ʼ����ֵ  int xx = yy.read(..) **/
			if( hasInit(astNameFis) ) {
				continue;
			}
			
			FSMMachineInstance fsmInst;
			
			//logc1("create ExtendAlias :" + astLiter.getImage());
			
			fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astNameFis));
			fsmInst.setResultString(astNameFis.getImage());
			list.add( fsmInst );
			
			
			//  ������ѭ����  
			/*SimpleJavaNode loopNode = getLoopNode(astNameFis); 
			if( null == loopNode ) {
				continue;
			}
			
			//  ��ȡΨһ�Ĳ����е��������Ķ�����еĳ���  
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
				//  �ж��Ƿ��� xx.read �ж�Ӧ�� xx ������������ǣ���ǰ�� occ ������������ occ  
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
			
			//  ��������������ڵ�ǰѭ���ķ�Χ֮��  
			Scope aryScop  = vAryDecl.getDeclareScope();
			Scope loopScop = loopNode.getScope();
			if(aryScop.isSelfOrAncestor(loopScop)) {
				continue;
			}
			// ������ѭ���ķ�Χ������ʽ��ֵ ary[..] = xx;  
			if( existsAssign2Name(loopNode, aryName.getImage()) ) {
				continue;
			}
			
			//  ��ѭ����Ѱ���������ĸ�ֵ�������Ǹö�����ѭ��������  
			boolean  existsAsgn2Name = false;
			boolean  existsDecl2Name = false;
			if( existsAlloc2Name(loopNode, vdeclFis.getImage()) ) {
				//  ����ұ��Ƿ�Ϊ new ���ʽ�����Ҳ������ǳ���  
				existsAsgn2Name = true;
			} else
			if( existsDeclInNode(loopNode, vdeclFis.getImage()) ) {
				existsDecl2Name = true;
			}
			
			// ������ڸ�ֵ�����������������õ������µ� ���� ����
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
	 * Ѱ�� loopNode ����Ķ� astN �ĸ�ֵ��
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
	 * Ѱ�� loopNode ����Ķ� astN �ĸ�ֵ��
	 * @param img ������
	 */
	private static boolean  existsAssign2Name(SimpleJavaNode loopNode, String  img) {
		String  xpath = ".//Name[ @Image='" + img + "' and ./../../../AssignmentOperator ]";
		List  relt = findTreeNodes(loopNode, xpath);
		for(int i = 0; i < relt.size(); i++) {
			ASTName ast = (ASTName) relt.get(i);
			/**  ��� astN �����ڸ�ֵ����������  **/
			if(ast.jjtGetParent().jjtGetParent() == ast.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(0)) {
				return true;				
			}
		}
		return false;
	}
	
	/***
	 * Ѱ�� loopNode ����Ķ� astN �� allocation ��ֵ������ new Xxx(..)�����Ƿǳ���
	 * @param img ������
	 */
	private static boolean  existsAlloc2Name(SimpleJavaNode loopNode, String  img) {
		String  xpath = ".//Name[ @Image='" + img + "' and ./../../../AssignmentOperator ]";
		List  relt = findTreeNodes(loopNode, xpath);
		for(int i = 0; i < relt.size(); i++) {
			ASTName ast = (ASTName) relt.get(i);
			/**  astN ��������ڸ�ֵ��������ұ�  **/
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
			/**  ȷ��������ȫΪ����  **/
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
	 * ��� xx.read(ary) �е�ary��Ӧ�� ASTName �ڵ�
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
	 * �ж��� xx.read(..) �� xx.read ��Ӧ�� ASTName �ڵ�ĸ��ڵ����Ƿ��и�ֵ�ڵ�
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
	 * ���astN��Ӧ������Ƿ���ѭ����,����Ҫ����
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
