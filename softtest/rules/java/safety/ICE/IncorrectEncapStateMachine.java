package softtest.rules.java.safety.ICE;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTReferenceType;
import softtest.ast.java.ASTResultType;
import softtest.ast.java.ASTType;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.rules.java.AbstractStateMachine;
import softtest.rules.java.sensdt.*;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;


/** ��1�� �������ƣ�����ֵй©
�������������������ڷ������ױ�������ֵʱй¶�ڲ��ı��(EI_EXPOSE_REP)��
����applet����ͨ���޸ķ��ؽ���޸Ķ�����ڲ�״̬��
������
private ArrayList adminUsers;
public Collection getAdminUsers() 
{
return adminUsers;
}
void maliciousUserCode()
{
getAdminUsers().add("myself");
}
����취��
��Щ��в����ͨ�����ڹ��������д洢�ɱ���������������ֹ������ʹ�ò��ɱ������档

2008-9-1
 */

public class IncorrectEncapStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��װ����ģʽ: %d �з��������ױ���󣬿�����©��", errorline);
		}else{
			f.format("Incorrect Encapsule:method return an mutable object on line %d",errorline);
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
	
	static Map markGroups = new HashMap();
	
	private static List getTreeNode(SimpleJavaNode node, String xStr) {
		List evalRlts = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	//
	private static ASTMethodDeclaration getMethodDeclaration(SimpleJavaNode node) {
		SimpleJavaNode par = node;
		while(par != null && !(par instanceof ASTMethodDeclaration)) {
			par = (SimpleJavaNode)par.jjtGetParent();
		}
		return (ASTMethodDeclaration)par;
	}
	
	public static List<FSMMachineInstance> createICEStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		//  ////////////////   ����ֵй©   /////////////////
		procReturnObjectLeak(node, fsm, list);
		
		return list;
	}
	
	
	public static boolean checkICE(VexNode vex,FSMMachineInstance fsmInst) {
		
		return true;
	}
	
	private static boolean  retObject(ASTName name) {
		String img = name.getImage();
		if( ! img.contains(".") ) {
			return false;
		}
		String  mthds[] = img.split("\\.");
		if(0 == mthds[mthds.length-1].compareTo("get")) {
			return true;
		}
		return false;
	}
	
	//  ///////////    ����ֵй©    /////////////
    //  �ҵ�getXxxx��������ֱ�ӷ��ط�final�������Ͷ����Ҹö����Ǹ�������Ա���򱨴�
	private static void  procReturnObjectLeak(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
		int  declUseTextDistance = 10;
		int  declUseSyntaxDistance = 10;
		Set<String> clsnames = new HashSet<String>(); 
		try{
			List<String> res = XMLUtil.getStrsFromFile("softtest\\rules\\java\\safety\\ICE\\ICE-Data", "ICE-Data", "Mutable", "ClassName");
			if( res.size() > 0 ) {
				clsnames.addAll(res);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		List<ASTName> retstmts = getTreeNode(whole, ".//ReturnStatement/Expression/PrimaryExpression/PrimaryPrefix/Name");
		for(ASTName name : retstmts) {
			// ���� "return set.add(obj);  return list.get(5);"�����
			/*if( retObject(name) ) {
				continue;
			}*/
			
			//System.out.println(name.getBeginLine()+" "+name.getBeginColumn());
			List<NameDeclaration> ndeclList = name.getNameDeclarationList();
			if(ndeclList.size() == 0) {
				continue;
			}
			NameDeclaration ndecl = ndeclList.get(0);
			if(! (ndecl instanceof VariableNameDeclaration)){
				continue;
			}
			// ����return var.methodX(..);����������Һ��Ե�
			int chn = name.jjtGetParent().jjtGetParent().jjtGetNumChildren();
			if(chn > 1) {
				ASTPrimarySuffix astSfix = (ASTPrimarySuffix)((SimpleJavaNode)name.jjtGetParent().jjtGetParent().jjtGetChild(chn-1));
				// return xx.mX().mY();
				if(astSfix.jjtGetNumChildren() > 0) {
					SimpleJavaNode astSP = ((SimpleJavaNode)name.jjtGetParent().jjtGetParent().jjtGetChild(chn-2));
					// ����� return xx.mX().getXxx(); �����Ӿͱ���
					if(astSP instanceof ASTPrimarySuffix) {
						if( astSP.getImage()==null||! astSP.getImage().startsWith("get")) {
							//System.out.println(astSP.getImage());
							continue;
						}
					} else if( astSP.getImage()==null||astSP instanceof ASTPrimaryPrefix ){
						ASTName nm = (ASTName) astSP.jjtGetChild(0);
						if(nm != name) {
							if( ! astSP.getImage().startsWith("get")) {
								//System.out.println(astSP.getImage());
								continue;
							}
						}else {
							String nmimage = nm.getImage();
							String strs [] = nmimage.split("\\.");
							if(!strs[strs.length-1].startsWith("get")) {
								continue;
							}
						}
					}
				}
				// return xx.mX().m;
				else {
					
				}
			}
			
			//ndecl = name.getNameDeclaration();
			//�˴���name��һ��Ϊ������Ҫ���й��� return f(); xqing 2008-09-27
			if(! (ndecl instanceof VariableNameDeclaration)){
				//System.out.println(ndecl);
				continue;
			}
			
			/*if(name.getImage().contains(".")) {
				
				continue ;
			}*/
			
			//System.out.println(name.getImage());
			
			VariableNameDeclaration vndecl = (VariableNameDeclaration) ndecl;
			Scope scope = vndecl.getDeclareScope();
			if( ! (scope instanceof ClassScope)) {
				continue;
			}
			ASTMethodDeclaration astMD = getMethodDeclaration(name);
			// ����������ص���final����
			if(astMD.isFinal()) {
				continue;
			}
			// �����������public��
			if( ! astMD.isPublic()) {
				continue;
			}
			// ����������Ǹ��෶Χ�ڶ����
			if(astMD.getScope().getEnclosingClassScope() != scope) {
				continue;
			}
			ASTResultType resultType = astMD.getResultType();
			// ���������Ͳ��Ƕ������ͣ������
			if(resultType.jjtGetNumChildren() == 0 
			|| (! (resultType.jjtGetChild(0) instanceof ASTType))) {
				continue;
			}
			ASTType astType = (ASTType)resultType.jjtGetChild(0);
			// ���������Ͳ����������ͣ������������ͣ�����һ��
			if(0 == astType.jjtGetNumChildren()
			|| ! (astType.jjtGetChild(0) instanceof ASTReferenceType)) {
				continue;
			}
			ASTReferenceType refType = (ASTReferenceType)astType.jjtGetChild(0);
			// ���������Ͳ���ClassOrInterface���ͣ�����һ��ѭ��
			if(0 == refType.jjtGetNumChildren()
			|| ! (refType.jjtGetChild(0) instanceof ASTClassOrInterfaceType) ) {
				continue;
			}
			ASTClassOrInterfaceType coriType = (ASTClassOrInterfaceType)refType.jjtGetChild(0);
			
			if(clsnames.contains( coriType.getImage() )) {
				newFSM(fsm, fsms, "return object which can be changed", name);
			}
		}
	}
	
	private static void newFSM(FSMMachine fsm, List fsms, String result, SimpleJavaNode ast) {
		FSMMachineInstance fsminstance = fsm.creatInstance();
		fsminstance.setRelatedObject(new FSMRelatedCalculation(ast));
		fsminstance.setResultString(result);
		fsms.add(fsminstance);
	}
	
	
	public static void logc1(String str) {
		logc("createICEFSM(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("URLSStateMechine::" + str);
		}
	}
}
