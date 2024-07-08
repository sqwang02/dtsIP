package softtest.rules.java.safety.PWL;

import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;

/**
 * Ӳ���봮�������⣬Դ�벻�Ƕ�˽�ܵĺõĴ洢�����ȣ����������������߿ɶ����ַ�������������׵�
 * ��java�ֽ����ж�ȡ����ʹ�����ֽ�����û����Զ������롣ͬʱ���������������򲻿����޸����롣
 * 
 * ���ݿ���ʣ�
 * class DBconn {
 *   // �������ݿ����� String
 *   String DBDriver = "com.mysql.jdbc.Driver"; *   
 *   // ��������
 *   ConnStr = "jdbc:mysql://56.123.34.45/db?useUnicode=true";
 *   // �û���   
 *   String USERNAME = "uname";
 *   // ����   
 *   String PASSWORD = "pwdtext";
 *
 *   Connection conn = null;
 *
 *   ResultSet rs = null;
 *
 *   public DBconn() {
 *     try {
 *       Class.forName(DBDriver); // װ�����ݿ�����
 *       try {
 *         conn = DriverManager.getConnection(ConnStr, USERNAME, PASSWORD);
 *         conn = DriverManager.getConnection(ConnStr, USERNAME, "pwds");
 *       } catch (Exception e) { }
 *     } catch (ClassNotFoundException e) {
 *       System.err.print("DBconn():" + e.getMessage());
 *     }
 *   }
 * }
 */

public class PasswdLeakStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ȫ����ȱ��ģʽ: %d ��,�����ַ������߿��ַ�����Ϊ���롣����Ϳ�����Ϊ���룬�����ױ��������ƽ⡣", errorline);
		}else{
			f.format("Password Leak: Password Leak on line %d",errorline);
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
     * PasswdLeakStateMachine����������ֵ��ý��м��:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, PASSWORD);
     * ����PASSWORD��String��Ķ��󣬿�����ȫ�ֱ�����ֲ�������
     * ��������PASSWORD�ĸ�ֵ�а��������ַ�����״̬��������Ϊһ����ȫȱ�ݡ�
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1.xml��
     * ��صļ�⺯���У�
     *   checkLiteralAssign();
     *   checkNonLiteralAssign();
     *   checkLiteralInit();
     *   checkConnStatement();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr����ƥ�����3��������DriverManager.getConnection���ã���Ҫ���������
         * ����һ�����������ؽ���ǵ�����������Ӧ��ASTName�������磺
         * connection = DriverManager.getConnection(a, b, c);
         * connection = DriverManager.getConnection(a, b, pass);
         */
        String xpathStr = 
            ".//PrimaryExpression[./PrimaryPrefix/Name[@Image='DriverManager.getConnection']]" +
            "/PrimarySuffix/Arguments[@ArgumentCount=3]/ArgumentList/Expression[3]" +
            "/PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }

        if (evalRlts == null || evalRlts.size() <= 0) {
            return list;
        }

        /*
         * table-����ȥ�ص�hash��
         */
        Hashtable<NameDeclaration, FSMMachineInstance> table = 
            new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            if (!(obj instanceof ASTName)) {
                continue;
            }

            ASTName pwdName = (ASTName) obj;
            if (!(pwdName.getNameDeclaration() instanceof VariableNameDeclaration)) {
                continue;
            }

            VariableNameDeclaration vDecl = (VariableNameDeclaration) pwdName.getNameDeclaration();
            if (vDecl.getTypeImage().equals("String") && !table.containsKey(vDecl)) {
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
     * �����ж��ַ������ĸ�ֵ������ֵ�Ƿ���״̬��ʵ������ر�����
     * 
     * ����ѡȡ����XPATH��
     * .//AssignmentOperator[
     *   ../AssignmentOperator and
     *   ../PrimaryExpression/PrimaryPrefix/Name and
     *   ../Expression[
     *     ./PrimaryExpression/PrimaryPrefix
     *     /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *     /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     or
     *     ./PrimaryExpression/PrimaryPrefix/Literal[@Image!='']
     *     or
     *     ./AdditiveExpression[
     *       ./PrimaryExpression/PrimaryPrefix/Literal
     *       or
     *       ./PrimaryExpression/PrimaryPrefix
     *       /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *       /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     ]
     *   ]
     * ]
     * /..
     * 
     * �����XPATHѡȡ��ֵ����㣬ѡ���ĸ�ֵ�����Ӧ���㣺
     * 1. ��ֵ�Ǳ�������
     * 2. ��ֵ��
     *       �ַ���"abc"��
     *       �����ַ���ʵ��new String("abc")
     *       ���ַ���"abc"��new String("abc")���Ӷ��õ��ַ�����
     * ���ǰѷ��������������ַ�����ֵ�����Ϊ�ַ������ĸ�ֵ��䡣
     * 
     * @param nodes
     * @param fsmInst
     * @return
     */
    public static boolean checkLiteralAssign(List nodes, FSMMachineInstance fsmInst) {
        if (nodes == null) {
            System.out.println("nodes is null, return");
            return false;
        }
        if (1 != nodes.size()) {
            return false;
        }
        
        SimpleJavaNode expr = (SimpleJavaNode) nodes.get(0);

        /*
         * ��·����ȡASTName
         * ./PrimaryExpression/PrimaryPrefix/Name
         */
        ASTName vName = (ASTName) expr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        if (vName.getNameDeclaration().equals(fsmInst.getRelatedVariable())) {
            return true;
        }
        return false;

    }

    /**
     * �����ж��ַ������ĳ�ʼ�����ı����Ƿ��״̬��ʵ������ر�����ͬ��
     * 
     * ����ѡȡ�ַ������ĳ�ʼ��������XPATH��
     * .//VariableDeclarator[
     *   ../Type/ReferenceType/ClassOrInterfaceType[@Image='String']
     *   and
     *   ./VariableInitializer/Expression[
     *     ./PrimaryExpression/PrimaryPrefix
     *     /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *     /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     or
     *     ./PrimaryExpression/PrimaryPrefix/Literal[@Image!='']
     *     or
     *     ./AdditiveExpression[
     *       ./PrimaryExpression/PrimaryPrefix/Literal
     *       or
     *       ./PrimaryExpression/PrimaryPrefix
     *       /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *       /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     ]
     *   ]
     * ]
     * |
     * //FieldDeclaration/VariableDeclarator[......(���ϰ벿�ֵ�������ͬ)]
     * 
     * ���ǰѷ��������������ַ�����ʼ�������Ϊ���ĳ�ʼ����
     * 1. ��ֵΪ�ַ���"abc";
     * 2. ��ֵΪ�����Ĵ����ַ���ʵ��new String("abc");
     * 3. ��"+"���ӵķ�������1��2����䡣
     * 
     * �߼������"|"ǰ��xpath����ƥ��ֲ�������"|"�������ƥ����ĳ�Ա���ԡ�
     * 
     * @param nodes
     * @param fsmInst
     * @return
     */
    public static boolean checkLiteralInit(List nodes,
            FSMMachineInstance fsmInst) {
        if (nodes == null) {
            System.out.println("nodes is null, return");
            return false;
        }
        if (1 != nodes.size()) {
            return false;
        }
        
        ASTVariableDeclarator astVDecltor = (ASTVariableDeclarator) nodes.get(0);
        
        /*
         * ./VariableDeclaratorId
         */
        ASTVariableDeclaratorId astId = (ASTVariableDeclaratorId) astVDecltor.jjtGetChild(0);
        if (astId.getNameDeclaration().equals(fsmInst.getRelatedVariable())) {
            return true;
        }
        return false;
    }

    /**
     * �����ж��ַ��������ĸ�ֵ������ֵ�Ƿ���״̬��ʵ������ر�����
     * 
     * ����ѡȡ����XPATH�����ַ������ĸ�ֵ����XPATH��ȣ�������ֵ�ж�����not()��
     * .//AssignmentOperator[
     *   ../AssignmentOperator and 
     *   ../PrimaryExpression/PrimaryPrefix/Name and 
     *   ../Expression[not(
     *     ./PrimaryExpression/PrimaryPrefix
     *     /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *     /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     or
     *     ./PrimaryExpression/PrimaryPrefix/Literal[@Image!='']
     *     or
     *     ./AdditiveExpression[
     *       ./PrimaryExpression/PrimaryPrefix/Literal
     *       or
     *       ./PrimaryExpression/PrimaryPrefix
     *       /AllocationExpression[./ClassOrInterfaceType[@Image='String']]
     *       /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal
     *     ]
     *   )
     *   ]
     * ]
     * /..
     * 
     * �����XPATHѡȡ��ֵ����㣬ѡ���ĸ�ֵ�����Ӧ���㣺
     * 1. ��ֵ�Ǳ�������
     * 2. ��ֵ������
     *       �ַ���"abc"��
     *       �����ַ���ʵ��new String("abc")
     *       ���ַ���"abc"��new String("abc")���Ӷ��õ��ַ�����
     * ���ǰѷ��������������ַ�����ֵ�����Ϊ�ַ��������ĸ�ֵ��䡣
     * 
     * @param nodes
     * @param fsmInst
     * @return
     */
    public static boolean checkNonLiteralAssign(List nodes, FSMMachineInstance fsmInst) {
        if (nodes == null) {
            System.out.println("nodes is null, return");
            return false;
        }
        if (1 != nodes.size()) {
            return false;
        }

        SimpleJavaNode expr = (SimpleJavaNode) nodes.get(0);
        
        /*
         * ��·����ȡASTName
         * ./PrimaryExpression/PrimaryPrefix/Name
         */
        ASTName vName = (ASTName) expr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        //jdh
        if(vName==null)
        	return false;
        
        if (vName.getNameDeclaration().equals(fsmInst.getRelatedVariable())) {
            return true;
        }
        return false;
    }

/*
    public static boolean checkNonLiteralInit(List nodes,
            FSMMachineInstance fsmInst) {
        if (nodes == null) {
            System.out.println("nodes is null, return");
        }
        if (1 != nodes.size()) {
            logc2("nodes.size = " + nodes.size() + " should be wrong. ????????");
        }
        VariableNameDeclaration vdecl = fsmInst.getRelatedVariable();
        ASTVariableDeclarator astVDecltor = (ASTVariableDeclarator) nodes
                .get(0);
        ASTVariableDeclaratorId astId = (ASTVariableDeclaratorId) astVDecltor
                .jjtGetChild(0);
        if (astId.getNameDeclaration().equals(vdecl)) {
            return true;
        }
        return false;
    }
*/

    /**
     * ���ڼ��DriverManager.getConnection�ĵ����������Ƿ���״̬��ʵ������ر�����ͬ��
     * 
     * ����ѡȡDriverManager.getConnection��XPATH:
     * .//PrimaryExpression[./PrimaryPrefix/Name[@Image='DriverManager.getConnection']]
     * /PrimarySuffix/Arguments[@ArgumentCount=3]/ArgumentList/Expression[3]
     * /PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name
     * 
     * @param nodes
     * @param fsmInst
     * @return
     */
    public static boolean checkConnStatement(List nodes, FSMMachineInstance fsmInst) {
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("nodes is null or empty, return false");
            return false;
        }
        
        for (Object obj : nodes) {
            if (obj instanceof ASTName) {
                ASTName an = (ASTName) obj;
                if (an.getNameDeclaration().equals(fsmInst.getRelatedVariable())) {
                    fsmInst.setResultString("var "+fsmInst.getRelatedVariable().getImage()
                            +" is readable text");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * PasswdLeakAStateMachine����������ֵ��ý��м��:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, "PASSWORD");
     * ����"PASSWORD"���ַ���������
     * ���������������ַ�����״̬��������Ϊһ����ȫȱ�ݡ�
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1A.xml��
     * ��صļ�⺯���У�
     *   checkPasswdLeak()
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakAStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr����ƥ�����3��������DriverManager.getConnection���ã���Ҫ���������
         * ����һ���ַ������������ؽ���ǵ�����������Ӧ��ASTLiteral�������磺
         * connection = DriverManager.getConnection(a, b, "c");
         * connection = DriverManager.getConnection(a, b, "pass");
         */
        String xpathStr =
            ".//PrimaryExpression[./PrimaryPrefix/Name[@Image='DriverManager.getConnection']]" +
        	"/PrimarySuffix/Arguments/ArgumentList/Expression[3]/PrimaryExpression/PrimaryPrefix/Literal";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() <= 0 ) {
            return list;
        }
        
        for (Object obj : evalRlts) {
            if (obj instanceof ASTLiteral) {
                ASTLiteral lit = (ASTLiteral) obj;
                if ( lit.getImage() == null || lit.getImage().length() <= 0 ) {
                    continue;
                }
                FSMMachineInstance fsmInst = fsm.creatInstance();
                FSMRelatedCalculation rel = new FSMRelatedCalculation(lit);
                fsmInst.setRelatedObject(rel);
                fsmInst.setResultString(lit.getImage());
                list.add(fsmInst);
            }
        }
        
        return list;
    }

    /**
     * For PasswdLeakAStateMachine
     * This method may do nothing except return true.
     */
    public static boolean checkPasswdLeak(List nodes, FSMMachineInstance fsmInst) {        
        if (nodes == null || nodes.size() <= 0) {
            System.out.println("nodes is null, return");
            return false;
        }
        /*
        logc4("+-----------------+");
        logc4("| checkPasswdLeak | pwd_MingWen :" + fsmInst.getRelatedObject());
        logc4("+-----------------+");
        */
        fsmInst.setResultString("pwd_text:"+fsmInst.getRelatedObject().getTagTreeNode().getImage());

        return true;
    }

    /**
     * PasswdLeakBStateMachine����������ֵ��ý��м��:
     * info.setProperty("password", "abc");
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1B.xml��
     * ��صļ�⺯���У�
     *   checkPasswdLeak();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakBStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        /*
         * xpathStr����ƥ�����2��������Properties.setProperty���ã���Ҫ���һ����������
         * ����"password"���ڶ�������Ϊ�ַ��������ؽ���ǵڶ���������Ӧ��ASTLiteral����
         * ���磺
         * info.setProperty("password", "c");
         */
        String xpathStr =
            ".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,'\\.setProperty$')]]" +
            "/PrimarySuffix/Arguments" +
            "/ArgumentList[" +
            "  ./Expression[1]/PrimaryExpression/PrimaryPrefix" +
            "  /Literal[matches(@Image,'\"(?i)password\"')]" +
            "]" +
            "/Expression[2]/PrimaryExpression/PrimaryPrefix/Literal";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() <= 0 ) {
            return list;
        }
        
        for (Object obj : evalRlts) {
            if (obj instanceof ASTLiteral) {
                ASTLiteral lit = (ASTLiteral) obj;
                if ( lit.getImage() == null || lit.getImage().length() <= 0 ) {
                    continue;
                }
                FSMMachineInstance fsmInst = fsm.creatInstance();
                FSMRelatedCalculation rel = new FSMRelatedCalculation(lit);
                fsmInst.setRelatedObject(rel);
                fsmInst.setResultString(lit.getImage());
                list.add(fsmInst);
            }
        }
        
        return list;
    }

    /**
     * PasswdLeakCStateMachine����������ֵ��ý��м��:
     * info.load(fis);
     * connection = DriverManager.getConnection(ConnStr, info);
     * info��Properties��Ķ���fis��FileInputStream��Ķ���
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1C.xml��
     * ��صļ�⺯���У�
     *   checkPasswdGet()
     *   checkPasswdLoad()
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakCStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr����ƥ�����1��������Properties.load���ã����ؽ����PrimaryExpression��
         * ���磺
         * info.load(fis);
         */
        String xpathStr =
            "//Block/BlockStatement/Statement/StatementExpression/PrimaryExpression[" +
            "  ./PrimaryPrefix/Name[matches(@Image,'^[A-Za-z_0-9]+\\.load$')]" +
            "  and" +
            "  ./PrimarySuffix/Arguments[@ArgumentCount=1]" +
            "]";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() == 0) {
            return list;
        }

        for (Object obj : evalRlts) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
            ASTName n = (ASTName) (pe.jjtGetChild(0).jjtGetChild(0));
            //added by yang 2011-04-07
            if(!(n.getNameDeclaration() instanceof VariableNameDeclaration)){
            	continue;
            }
            
            VariableNameDeclaration nameDecl = (VariableNameDeclaration) n.getNameDeclaration();
            if ( nameDecl == null || nameDecl.getTypeImage() == null 
                    || !nameDecl.getTypeImage().equalsIgnoreCase("Properties")) {
                continue;
            }
            FSMMachineInstance fsmi = fsm.creatInstance();
            fsmi.setRelatedVariable(nameDecl);
            list.add(fsmi);
        }

        return list;
    }

    /**
     * ���ڼ��DriverManager.getConnection�ĵڶ��������Ƿ���״̬��ʵ������ر�����ͬ��
     * DriverManager.getConnection(url, info);
     * 
     * ����ѡȡ����XPATH��
     * .//PrimaryExpression [./PrimaryPrefix/Name[@Image='DriverManager.getConnection']]
     * /PrimarySuffix/Arguments[@ArgumentCount=2]/ArgumentList
     * /Expression[2]/PrimaryExpression/PrimaryPrefix/Name
     *
     */
    public static boolean checkPasswdGet(List nodes, FSMMachineInstance fsmInst) {
        if (nodes == null || fsmInst == null) {
            System.out.println("nodes or fsmInst is null, return");
            return false;
        }

        for (Object obj : nodes) {
            ASTName nm = (ASTName) obj;
            NameDeclaration nameDecl = nm.getNameDeclaration();
            if (nameDecl.equals(fsmInst.getRelatedVariable())) {                
                return true;
            }
        }
        return false;
    }

    /**
     * �����ж�*.load�Ķ���*�Ƿ���״̬��ʵ������ر�����ͬ��
     * 
     * ����ѡȡ����XPATH��
     * .//Block/BlockStatement/Statement/StatementExpression/PrimaryExpression["+
     *   ./PrimaryPrefix/Name[matches(@Image,'^[A-Za-z_0-9]\\.load$')] "+
     *   and
     *   ./PrimarySuffix/Arguments[@ArgumentCount=1]
     * ]
     *
     * @param nodes
     * @param fsmInst
     * @return
     */
    public static boolean checkPasswdLoad(List nodes, FSMMachineInstance fsmInst) {
        if (nodes == null || fsmInst == null) {
            System.out.println("nodes or fsmInst is null, return");
            return false;
        }

        for (Object obj : nodes) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
            ASTName n = (ASTName) pe.jjtGetChild(0).jjtGetChild(0);
            NameDeclaration nameDecl = n.getNameDeclaration();
            if (nameDecl.equals(fsmInst.getRelatedVariable())) {
                return true;
            }
        }
        return false;
    }

    /**
     * PasswdLeakDStateMachine����������ֵ��ý��м��:
     * print("password"+password); print(password);
     * ����ӡ���������Ϣ��
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1D.xml��
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakDStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr����ƥ���ӡ��䡣���磺
         * print("password"+password);
         */
        String xpathStr =
            ".//Block/BlockStatement/Statement/StatementExpression/PrimaryExpression[" +
            "  ./PrimaryPrefix/Name[matches(@Image,'(?i)(print|log|debug)')]" +
            "]";
        
        /*
         * �����жϴ�ӡ����Ƿ�����ַ���password��
         */
        String xpathStr1 =
            "./PrimarySuffix//Name[matches(@Image,'(?i)passw(or)?d']";
        /*
         * ����ѡ����ӡ�����������б�����
         */
        String xpathStr2 =
            "./PrimarySuffix/Arguments/ArgumentList/Expression//PrimaryExpression/PrimaryPrefix/Name";

        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() == 0) {
            return list;
        }

        for (Object obj : evalRlts) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
            
            List evalNames = null;
            try {
                evalNames = pe.findChildNodesWithXPath(xpathStr1);
            } catch (JaxenException e) {
                e.printStackTrace();
                throw new RuntimeException("xpath error",e);
            }
            
            if (evalNames != null && evalNames.size() > 0) {
                FSMMachineInstance fsmi = fsm.creatInstance();
                fsmi.setRelatedObject(new FSMRelatedCalculation(pe));
                fsmi.setResultString("print password[" + pe.printNode(ProjectAnalysis.getCurrent_file()) + "]");
                list.add(fsmi);
                continue;
            }

            try {
                evalNames = pe.findChildNodesWithXPath(xpathStr2);
            } catch (JaxenException e) {
                e.printStackTrace();
                throw new RuntimeException("xpath error",e);
            }

            for (Object objn : evalNames) {
                ASTName name = (ASTName) objn;
                //�˴���name��һ��Ϊ������Ҫ���й��� f(g()); xqing 2008-09-27
                if(!(name.getNameDeclaration() instanceof VariableNameDeclaration)){
                	continue;
                }
                VariableNameDeclaration var = (VariableNameDeclaration) name.getNameDeclaration();
                
                if (!var.getTypeImage().equals("String")) {
                    continue;
                }
                String varname = var.getImage();
                String xpath = getXpathD(varname);
                List eval = null;
                try {
                    eval = pe.findChildNodesWithXPath(xpath);
                } catch (JaxenException e) {
                    e.printStackTrace();
                    throw new RuntimeException("xpath error",e);
                }
                if (eval == null || eval.size() <= 0) {
                    continue;
                }

                boolean flag = false;
                for (Object objpe : eval) {
                    SimpleJavaNode sjn = (SimpleJavaNode) objpe;
                    NameDeclaration vnd = null;
                    SimpleJavaNode img = null;
                    if (sjn.jjtGetChild(0) instanceof ASTVariableDeclaratorId) {
                        img = (SimpleJavaNode) sjn.jjtGetChild(0);
                        vnd = ((ASTVariableDeclaratorId) img).getNameDeclaration();
                    }
                    else {
                        img = (SimpleJavaNode) sjn.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
                        vnd = ((ASTName) img).getNameDeclaration();
                    }
                    if (!vnd.equals(var)) {
                        continue;
                    }
                    FSMMachineInstance fsmi = fsm.creatInstance();
                    fsmi.setRelatedObject(new FSMRelatedCalculation(pe));
                    fsmi.setResultString("print password(String " + img.getImage()
                            + " look at the line" + img.getBeginLine() + ") ");
                    list.add(fsmi);
                    flag = true;
                    break;

                }
                if (flag) {
                    break;
                }                
            }
        }
        return list;
    }

    /**
     * helper function
     * @param name
     * @return
     */
    private static String getXpathD(String name) {
        StringBuffer buf = new StringBuffer();
        buf.append("//AssignmentOperator/..[./PrimaryExpression/PrimaryPrefix/Name[@Image='");
        buf.append(name);
        buf.append("'] and ./Expression//Name[matches(@Image,'(?i)passw(or)?d']]");
        buf.append("| //VariableDeclarator[./VariableDeclaratorId[@Image='");
        buf.append(name);
        buf.append("'] and ./VariableInitializer//Name[matches(@Image,'(?i)passw(or)?d')]]");
        return buf.toString();
    }

    /**
     * ��PasswdLeakStateMachine��Ӧ������PasswdLeakStateMachine�Ѿ����Լ��ȫ�ֱ���
     * PasswdLeakEStateMachine����������ֵ��ý��м��:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, PASSWORD);
     * ����PASSWORD��String��Ķ��󣬶�����ȫ�ֱ�����
     * ��������PASSWORD�ĸ�ֵ�а��������ַ�����״̬��������Ϊһ����ȫȱ�ݡ�
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1E.xml��
     * ��صļ�⺯���У�
     *   checkLiteralAssign();
     *   checkNonLiteralAssign();
     *   checkConnStatement();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakEStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        String xpathStr = ".//PrimaryExpression[./PrimaryPrefix"
                + "/Name[@Image='DriverManager.getConnection']]"
                + "/PrimarySuffix/Arguments[@ArgumentCount=3]/ArgumentList/Expression[3]"
                + "/PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name";
        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() <= 0) {
            return list;
        }

        String xpathStr1 = "//FieldDeclaration/VariableDeclarator/VariableDeclaratorId["
                + "  ../../Type/ReferenceType/ClassOrInterfaceType[@Image='String']"
                + "  and"
                + "  ../VariableInitializer/Expression["
                + "    ./PrimaryExpression/PrimaryPrefix"
                + "    /AllocationExpression[./ClassOrInterfaceType[@Image='String']]"
                + "    /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal"
                + "    or"
                + "    ./PrimaryExpression/PrimaryPrefix/Literal[@Image!='']"
                + "    or"
                + "    ./AdditiveExpression["
                + "      ./PrimaryExpression/PrimaryPrefix/Literal"
                + "      or"
                + "      ./PrimaryExpression/PrimaryPrefix"
                + "      /AllocationExpression[./ClassOrInterfaceType[@Image='String']]"
                + "      /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal"
                + "    ]" + "  ]" + "]";
        List evalStringFields = null;
        try {
            evalStringFields = node.findChildNodesWithXPath(xpathStr1);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalStringFields == null || evalStringFields.size() <= 0) {
            return list;
        }
        HashSet<NameDeclaration> fields = new HashSet<NameDeclaration>();
        for (Object obj : evalStringFields) {
            if (obj instanceof ASTVariableDeclaratorId) {
                ASTVariableDeclaratorId vdid = (ASTVariableDeclaratorId) obj;
                fields.add(vdid.getNameDeclaration());
            }
        }

        Hashtable<NameDeclaration, FSMMachineInstance> table = new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            if (!(obj instanceof ASTName)) {
                continue;
            }

            ASTName pwdName = (ASTName) obj;
            if (!(pwdName.getNameDeclaration() instanceof VariableNameDeclaration)) {
                continue;
            }

            VariableNameDeclaration vDecl = (VariableNameDeclaration) pwdName
                    .getNameDeclaration();
            if (!vDecl.getTypeImage().equals("String")
                    || !fields.contains(vDecl) || table.containsKey(vDecl)) {
                continue;
            }

            FSMMachineInstance fsmInst = fsm.creatInstance();
            fsmInst.setRelatedVariable(vDecl);
            table.put(vDecl, fsmInst);
        }

        for (Enumeration<FSMMachineInstance> e = table.elements(); e
                .hasMoreElements();) {
            list.add(e.nextElement());
        }

        return list;
    }

    /**
     * PasswdLeakFStateMachine����������ֵ��ý��м��:
     * str = "abc"+new String("def");
     * info.setProperty("password", str); 
     * ����info��Properties�����str��String�����
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1F.xml��
     * ��صļ�⺯���У�
     *   checkLiteralAssign();
     *   checkNonLiteralAssign();
     *   checkLiteralInit();
     *   checkConnStatement();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakFStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-�洢״̬��ʵ�������Ա�
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        /*
         * xpathStr����ƥ��*.setProperty�����磺
         * info.setPropery("password",str);
         */
        String xpathStr =
            ".//StatementExpression/PrimaryExpression["+
            "  ./PrimaryPrefix/Name[matches(@Image,'^[A-Za-z0-9_]+\\.setProperty$')]"+
            "  and"+
            "  ./PrimarySuffix/Arguments[@ArgumentCount=2]"+
            "  /ArgumentList["+
            "    ./Expression[1]/PrimaryExpression/PrimaryPrefix/Literal[matches(@Image,'(?i)passw(or)?d')]"+
            "  ]"+
            "  /Expression[2]/PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name"+
            "]";
        
        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() == 0) {
            return list;
        }

        /*
         * table-����ȥ�ص�hash��
         */
        Hashtable<NameDeclaration, FSMMachineInstance> table = 
            new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
            ASTName n = (ASTName) (pe.jjtGetChild(0).jjtGetChild(0));
            VariableNameDeclaration nameDecl = (VariableNameDeclaration) n
                    .getNameDeclaration();
            if (nameDecl==null||!nameDecl.getTypeImage().equalsIgnoreCase("Properties")) {
                continue;
            }
            n = (ASTName) pe.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)
                    .jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)
                    .jjtGetChild(0);
            nameDecl = (VariableNameDeclaration) n.getNameDeclaration();
            if (nameDecl==null||!nameDecl.getTypeImage().equalsIgnoreCase("String")
                    || table.containsKey(nameDecl)) {
                continue;
            }
            FSMMachineInstance fsmi = fsm.creatInstance();
            fsmi.setRelatedVariable(nameDecl);
            table.put(nameDecl, fsmi);
        }

        for (Enumeration<FSMMachineInstance> e = table.elements(); e
                .hasMoreElements();) {
            list.add(e.nextElement());
        }

        return list;
    }

    /**
     * ��PasswdLeakFStateMachine��Ӧ
     * PasswdLeakGStateMachine����������ֵ��ý��м��:
     * info.setProperty("password", str);
     * connection = DriverManager.getConnection(ConnStr, info);
     * ����inf��Properties�����str��String�����
     * str��ȫ�ֱ�����
     * 
     * ��Ӧ��״̬����Ϊ�����ļ���PWL-0.1G.xml��
     * ��صļ�⺯���У�
     *   checkLiteralAssign();
     *   checkNonLiteralAssign();
     *   checkConnStatement();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakGStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        String xpathStr = ".//StatementExpression/PrimaryExpression["
                + "  ./PrimaryPrefix/Name[matches(@Image,'^[A-Za-z0-9_]+\\.setProperty$')]"
                + "  and"
                + "  ./PrimarySuffix/Arguments[@ArgumentCount=2]"
                + "  /ArgumentList["
                + "    ./Expression[1]/PrimaryExpression/PrimaryPrefix/Literal[matches(@Image,'(?i)passw(or)?d')]"
                + "  ]"
                + "  /Expression[2]/PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name"
                + "]";
        List evalRlts = null;
        try {
            evalRlts = node.findChildNodesWithXPath(xpathStr);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalRlts == null || evalRlts.size() == 0) {
            return list;
        }

        String xpathStr1 = "//FieldDeclaration/VariableDeclarator/VariableDeclaratorId["
                + "  ../../Type/ReferenceType/ClassOrInterfaceType[@Image='String']"
                + "  and"
                + "  ../VariableInitializer/Expression["
                + "    ./PrimaryExpression/PrimaryPrefix"
                + "    /AllocationExpression[./ClassOrInterfaceType[@Image='String']]"
                + "    /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal"
                + "    or"
                + "    ./PrimaryExpression/PrimaryPrefix/Literal[@Image!='']"
                + "    or"
                + "    ./AdditiveExpression["
                + "      ./PrimaryExpression/PrimaryPrefix/Literal"
                + "      or"
                + "      ./PrimaryExpression/PrimaryPrefix"
                + "      /AllocationExpression[./ClassOrInterfaceType[@Image='String']]"
                + "      /Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal"
                + "    ]" + "  ]" + "]";
        List evalStringFields = null;
        try {
            evalStringFields = node.findChildNodesWithXPath(xpathStr1);
        } catch (JaxenException e) {
            e.printStackTrace();
            throw new RuntimeException("xpath error",e);
        }
        if (evalStringFields == null || evalStringFields.size() <= 0) {
            return list;
        }
        HashSet<NameDeclaration> fields = new HashSet<NameDeclaration>();
        for (Object obj : evalStringFields) {
            if (obj instanceof ASTVariableDeclaratorId) {
                ASTVariableDeclaratorId vdid = (ASTVariableDeclaratorId) obj;
                fields.add(vdid.getNameDeclaration());
            }
        }

        Hashtable<NameDeclaration, FSMMachineInstance> table = new Hashtable<NameDeclaration, FSMMachineInstance>();
        for (Object obj : evalRlts) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression) obj;
            ASTName n = (ASTName) (pe.jjtGetChild(0).jjtGetChild(0));
            VariableNameDeclaration nameDecl = (VariableNameDeclaration) n
                    .getNameDeclaration();
            if (nameDecl != null) {
            	if (!"".equals(nameDecl.getTypeImage())) {
            		if (!nameDecl.getTypeImage().equalsIgnoreCase("Properties")) {
            		continue;
            		}
            	}
            }
            n = (ASTName) pe.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)
                    .jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)
                    .jjtGetChild(0);
            nameDecl = (VariableNameDeclaration) n.getNameDeclaration();
            if (!nameDecl.getTypeImage().equalsIgnoreCase("String")
                    || !fields.contains(nameDecl)
                    || table.containsKey(nameDecl)) {
                continue;
            }
            FSMMachineInstance fsmi = fsm.creatInstance();
            fsmi.setRelatedVariable(nameDecl);
            table.put(nameDecl, fsmi);
        }

        for (Enumeration<FSMMachineInstance> e = table.elements(); e
                .hasMoreElements();) {
            list.add(e.nextElement());
        }

        return list;
    }

}
