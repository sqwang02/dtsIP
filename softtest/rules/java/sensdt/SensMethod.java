package softtest.rules.java.sensdt;

import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import softtest.ast.java.*;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;

/**
<Method Name="gc" ReturnType="void" Sensitive="true">
</Method>
<Method Name="getProperty" ReturnType="String"	Transitive="true">
	<Args   Argc="1" >
		<And>
			<Arg_0  NeedSensitive="true"  Type="String" > <!-- Arg -->
				<Equal>web.root</Equal>
				<Equal>root</Equal>
				<Equal>web_root</Equal>
				<Equal>webroot</Equal>
				<Equal>password</Equal>
				<Matches>*web(.)?root*</Matches>
			</Arg_0>
		</And>
	</Args>
	<Args  Argc="2">
		<Or>
			<Arg_0  NeedSensitive="true"  Type="String" >
				<Equal>password</Equal>
				<Matches>*web(.)?root*</Matches>
			</Arg_0>
			<Arg_1  NeedSensitive="true"  Type="String" >
				<Equal>web.root</Equal>
				<Equal>web_root</Equal>
				<Matches>*web(.)?root*</Matches>
			</Arg_1>
		</Or>
	</Args>
	<Args  Argc="3">
		<And>
			<Arg_0   Type="String" >
				<equal>web.root</equal>
				<equal>webroot</equal>
				<matches>*web(.)?root*</matches>
			</Arg_0>
			<Arg_1   Type="String" >
				<equal>webroot</equal>
				<matches>*web(.)?root*</matches>
			</Arg_1>
		</And>
	</Args>
</Method>
*/
public class  SensMethod {
	
	private String  	methodName;
	private boolean   beSensitive  = false;
	private boolean   beTransitive = false;
	private  List<Signature>    signs = new LinkedList<Signature>();
	
	
	public SensMethod(Node  method) {
		NamedNodeMap attrs = method.getAttributes();
		methodName = attrs.getNamedItem("Name").getNodeValue();
		String  retType  = attrs.getNamedItem("ReturnType").getNodeValue();
		String  bsens  = null;
		String  btrans = null;
		if( attrs.getNamedItem("Sensitive") != null ){
			bsens    = attrs.getNamedItem("Sensitive").getNodeValue();
		}
		if( attrs.getNamedItem("Transitive") != null) {
			btrans   = attrs.getNamedItem("Transitive").getNodeValue();
		}
		if( bsens != null && bsens.equals("true") ) {
			beSensitive = true;
		}
		if( btrans != null && btrans.equals("true") ) {
			beTransitive = true; 
		}
		/**  process  <Args>  tag,  generates the signatures **/
		for( Node args = method.getFirstChild(); args != null; args = args.getNextSibling() ) {
			if( args.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			}
			String  strArgc = args.getAttributes().getNamedItem("Argc").getNodeValue();
			int     argc = Integer.parseInt(strArgc);
			Signature sign = new Signature( args );
			signs.add( sign );
		}
	}
	
	/**  **/
	boolean  isSensitiveOperResult(SimpleJavaNode  node, int [] args) {
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTName  astName = (ASTName) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		ASTArguments astArgs = (ASTArguments) astPrimExpr.jjtGetChild(1).jjtGetChild(0);
		if( astArgs.getArgumentCount() > 0 ) {
			if( ! beTransitive ) {
				return false;
			}
			/**  # 1   signature match   **/
			/**  In this stage, just compare the argc, without comparing the
			 * type of every argument, for getting Type-info is hard from the
			 * AST  **/
			ASTArgumentList  astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
			int i = 0;
			int len = signs.size();
			for( ; i < len; i++) {
				if( astArgList.jjtGetNumChildren() == signs.get(i).getArgc() ) {
					break;
				}
			}
			if( i >= len) {
				throw new RuntimeException("Error signature.");
			}
			Signature sig = signs.get(i);
			/**  # 2   arguments evaluate(match)   **/
			if( sig.isSensitiveArgs(args) ) {
				isSens = true;
			}
			
		} else {
			/**  true if the method or constructor  is sensitive  **/
			/**  If the method has no arguments, the result is sensitive when
			 *   beSensitive is true  **/
			if( beSensitive ) {
				isSens = true;
			}
		}
		return isSens;
	}
	
	/**  (var, ".."); (var) **
	public static boolean  isSensitiveResult(SimpleJavaNode  node,SensClasses sclses, SensClass scls, SensMethod mthd, ExtendAlias ealias) {
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTArguments astArgs = (ASTArguments) astPrimExpr.jjtGetChild(1).jjtGetChild(0);
		if( astArgs.getArgumentCount() > 0 ) {
			if( ! mthd.beSensitive ) {
				return false;
			}
			/**  Step 1   signature match   **
			/**  In this stage, just compare the argc, without comparing the
			 * type of every argument, for getting Type-info is hard from the
			 * AST  **
			ASTArgumentList  astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
			int i = 0;
			List<Signature> signs = mthd.signs;
			int len = signs.size();
			for( ; i < len; i++) {
				if( astArgList.jjtGetNumChildren() == signs.get(i).getArgc() ) {
					break;
				}
			}
			if( i >= len ) {
				throw new RuntimeException("Error signature.");
			}
			
			//ASTExpression astExpr = (ASTExpression) astArgList.jjtGetChild(i);
			Signature sign = signs.get(i);
			
			/**  Step 2   arguments evaluate(match)   **
			/*if( Signature.isSensitive(node, sclses, scls, sign, ealias) ) {
				isSens = true;
			}*
			
		} else {
			/**  true if the method or constructor  is sensitive  *
			/**  If the method has no arguments, the result is sensitive when
			 *   beSensitive is true  **
			if( mthd.beSensitive ) {
				isSens = true;
			}
		}
		return isSens;
	}*/
	
	public String getMethodName() {
		return methodName;
	}

	public  void dump() {
		logc("-------------- [ Method ] -------------[ Begin ]--");
		logc("name:" + methodName);
		for( int i = 0; i < signs.size(); i++ ) {
			(signs.get(i)).dump();
		}
		logc("-------------- [ Method ] -------------[ End ]--");
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("SensMethod:: - " + str);
		}
	}
}
	
	
	
