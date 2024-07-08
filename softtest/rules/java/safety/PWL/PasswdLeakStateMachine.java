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
 * 硬编码串加密问题，源码不是对私密的好的存储。首先，这允许其他开发者可读。字符串密码可以轻易的
 * 从java字节码中读取，这使得有字节码的用户可以读该密码。同时，如果不升级软件则不可以修改密码。
 * 
 * 数据库访问：
 * class DBconn {
 *   // 定义数据库驱动 String
 *   String DBDriver = "com.mysql.jdbc.Driver"; *   
 *   // 定义连接
 *   ConnStr = "jdbc:mysql://56.123.34.45/db?useUnicode=true";
 *   // 用户名   
 *   String USERNAME = "uname";
 *   // 密码   
 *   String PASSWORD = "pwdtext";
 *
 *   Connection conn = null;
 *
 *   ResultSet rs = null;
 *
 *   public DBconn() {
 *     try {
 *       Class.forName(DBDriver); // 装载数据库驱动
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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("安全特性缺陷模式: %d 行,明码字符串或者空字符串作为密码。明码和空码作为密码，很容易被攻击者破解。", errorline);
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
     * PasswdLeakStateMachine针对下面这种调用进行检测:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, PASSWORD);
     * 其中PASSWORD是String类的对象，可以是全局变量或局部变量。
     * 如果程序对PASSWORD的赋值中包含明文字符串，状态机将其视为一个安全缺陷。
     * 
     * 对应的状态机行为描述文件是PWL-0.1.xml。
     * 相关的检测函数有：
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
         * table-用于去重的hash表
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
     * 用于判断字符串明文赋值语句的左值是否是状态机实例的相关变量。
     * 
     * 用于选取结点的XPATH：
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
     * 上面的XPATH选取赋值语句结点，选出的赋值语句结点应满足：
     * 1. 左值是变量名；
     * 2. 右值是
     *       字符串"abc"、
     *       创建字符串实例new String("abc")
     *       或字符串"abc"与new String("abc")连接而得的字符串。
     * 我们把符合上述特征的字符串赋值语句视为字符串明文赋值语句。
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
         * 按路径获取ASTName
         * ./PrimaryExpression/PrimaryPrefix/Name
         */
        ASTName vName = (ASTName) expr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        if (vName.getNameDeclaration().equals(fsmInst.getRelatedVariable())) {
            return true;
        }
        return false;

    }

    /**
     * 用于判断字符串明文初始化语句的变量是否和状态机实例的相关变量相同。
     * 
     * 用于选取字符串明文初始化语句结点的XPATH：
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
     * //FieldDeclaration/VariableDeclarator[......(与上半部分的条件相同)]
     * 
     * 我们把符合以下特征的字符串初始化语句视为明文初始化：
     * 1. 右值为字符串"abc";
     * 2. 右值为用明文创建字符串实例new String("abc");
     * 3. 用"+"连接的符合条件1、2的语句。
     * 
     * 逻辑或符号"|"前的xpath用于匹配局部变量，"|"后的用于匹配类的成员属性。
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
     * 用于判断字符串非明文赋值语句的左值是否是状态机实例的相关变量。
     * 
     * 用于选取结点的XPATH，与字符串明文赋值语句的XPATH相比，仅在右值判断增加not()：
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
     * 上面的XPATH选取赋值语句结点，选出的赋值语句结点应满足：
     * 1. 左值是变量名；
     * 2. 右值不是是
     *       字符串"abc"、
     *       创建字符串实例new String("abc")
     *       或字符串"abc"与new String("abc")连接而得的字符串。
     * 我们把符合上述特征的字符串赋值语句视为字符串非明文赋值语句。
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
         * 按路径获取ASTName
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
     * 用于检查DriverManager.getConnection的第三个参数是否与状态机实例的相关变量相同。
     * 
     * 用于选取DriverManager.getConnection的XPATH:
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
     * PasswdLeakAStateMachine针对下面这种调用进行检测:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, "PASSWORD");
     * 其中"PASSWORD"是字符串常量。
     * 如果程序包含明文字符串，状态机将其视为一个安全缺陷。
     * 
     * 对应的状态机行为描述文件是PWL-0.1A.xml。
     * 相关的检测函数有：
     *   checkPasswdLeak()
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakAStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配具有3个参数的DriverManager.getConnection调用，且要求第三个参
         * 数是一个字符串常量，返回结点是第三个参数对应的ASTLiteral对象。例如：
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
     * PasswdLeakBStateMachine针对下面这种调用进行检测:
     * info.setProperty("password", "abc");
     * 
     * 对应的状态机行为描述文件是PWL-0.1B.xml。
     * 相关的检测函数有：
     *   checkPasswdLeak();
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakBStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        /*
         * xpathStr用于匹配具有2个参数的Properties.setProperty调用，且要求第一个参数是字
         * 符串"password"，第二个参数为字符串，返回结点是第二个参数对应的ASTLiteral对象。
         * 例如：
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
     * PasswdLeakCStateMachine针对下面这种调用进行检测:
     * info.load(fis);
     * connection = DriverManager.getConnection(ConnStr, info);
     * info是Properties类的对象，fis是FileInputStream类的对象。
     * 
     * 对应的状态机行为描述文件是PWL-0.1C.xml。
     * 相关的检测函数有：
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
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配具有1个参数的Properties.load调用，返回结点是PrimaryExpression。
         * 例如：
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
     * 用于检查DriverManager.getConnection的第二个参数是否与状态机实例的相关变量相同。
     * DriverManager.getConnection(url, info);
     * 
     * 用于选取结点的XPATH：
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
     * 用于判断*.load的对象*是否与状态机实例的相关变量相同。
     * 
     * 用于选取结点的XPATH：
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
     * PasswdLeakDStateMachine针对下面这种调用进行检测:
     * print("password"+password); print(password);
     * 即打印输出密码信息。
     * 
     * 对应的状态机行为描述文件是PWL-0.1D.xml。
     * 
     * @param node
     * @param fsm
     * @return
     */
    public static List<FSMMachineInstance> createPasswdLeakDStateMachines(
            SimpleJavaNode node, FSMMachine fsm) {
        /*
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
        
        /*
         * xpathStr用于匹配打印语句。例如：
         * print("password"+password);
         */
        String xpathStr =
            ".//Block/BlockStatement/Statement/StatementExpression/PrimaryExpression[" +
            "  ./PrimaryPrefix/Name[matches(@Image,'(?i)(print|log|debug)')]" +
            "]";
        
        /*
         * 用于判断打印语句是否包含字符串password。
         */
        String xpathStr1 =
            "./PrimarySuffix//Name[matches(@Image,'(?i)passw(or)?d']";
        /*
         * 用于选出打印语句输出的所有变量。
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
                //此处的name不一定为变量，要进行过滤 f(g()); xqing 2008-09-27
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
     * 与PasswdLeakStateMachine对应，现在PasswdLeakStateMachine已经可以检测全局变量
     * PasswdLeakEStateMachine针对下面这种调用进行检测:
     * connection = DriverManager.getConnection(ConnStr, USERNAME, PASSWORD);
     * 其中PASSWORD是String类的对象，而且是全局变量。
     * 如果程序对PASSWORD的赋值中包含明文字符串，状态机将其视为一个安全缺陷。
     * 
     * 对应的状态机行为描述文件是PWL-0.1E.xml。
     * 相关的检测函数有：
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
         * list-存储状态机实例的线性表
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
     * PasswdLeakFStateMachine针对下面这种调用进行检测:
     * str = "abc"+new String("def");
     * info.setProperty("password", str); 
     * 其中info是Properties类对象，str是String类对象。
     * 
     * 对应的状态机行为描述文件是PWL-0.1F.xml。
     * 相关的检测函数有：
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
         * list-存储状态机实例的线性表
         */
        List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

        /*
         * xpathStr用于匹配*.setProperty。例如：
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
         * table-用于去重的hash表
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
     * 与PasswdLeakFStateMachine对应
     * PasswdLeakGStateMachine针对下面这种调用进行检测:
     * info.setProperty("password", str);
     * connection = DriverManager.getConnection(ConnStr, info);
     * 其中inf是Properties类对象，str是String类对象。
     * str是全局变量。
     * 
     * 对应的状态机行为描述文件是PWL-0.1G.xml。
     * 相关的检测函数有：
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
