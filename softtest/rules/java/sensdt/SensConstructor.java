package softtest.rules.java.sensdt;

import java.util.List;
import java.util.LinkedList;


import org.w3c.dom.Node;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;
import softtest.rules.java.sensdt.Signature;

/**
<Class Name="File" Package="java.io">
<Constructor  Transitive="true">
	<Args Types="String,String">
		<And>
			<Arg_0  NeedSensitive="true">
			</Arg_0>
			<Arg_1  NeedSensitive="false" />
		</And>
	</Args>
	<Args  type="String" Sensitive="true">
	</Args>
</Constructor>
</Class>
*/
public class  SensConstructor {
	
	private boolean   beTransitive = false;
	
	private  List<Signature>    signs = new LinkedList<Signature>();
	
	
	public SensConstructor(Node  consNode) {
		String strTrans = consNode.getAttributes().getNamedItem("Transitive").getNodeValue();
		if( strTrans.equals("true") ) {
			beTransitive = true;
		}
		for( Node args = consNode.getFirstChild(); args != null; args = args.getNextSibling() ) {
			if( args.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			}
			signs.add( new Signature( args ) );
		}
	}
	

	boolean  isSensitive(SimpleJavaNode  node, int [] args) {
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTAllocationExpression astAlloc = (ASTAllocationExpression) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		ASTArguments astArgs = (ASTArguments) astAlloc.jjtGetChild(1);
		if( astArgs.getArgumentCount() > 0 ) {
			if( ! beTransitive ) {
				return false;
			}
			/**  # 1   signature match   
			/**  In this stage, just compare the argc, without comparing the
			 * type of every argument, for getting Type-info is hard from the
			 * AST   */
			ASTArgumentList  astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
			int i = 0;
			int len = signs.size();
			for( ; i < len; i++) {
				if( astArgList.jjtGetNumChildren() == signs.get(i).getArgc() ) {
					break;
				}
			}
			if( i >= len ) {
				throw new RuntimeException("Error signature.");
			}
			Signature sign = signs.get(i);
			/**  # 2   arguments evaluate(match)   */
			if( sign.isSensitiveArgs( args) ) {
				isSens = true;
			}
		}
		return isSens;
	}
	
	/**    **
	public static boolean  isSens(SimpleJavaNode  node, SensClasses sclses, 
			SensClass scls, SensConstructor constructor, ExtendAlias ealias) {
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTAllocationExpression astAlloc = (ASTAllocationExpression) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		ASTArguments astArgs = (ASTArguments) astAlloc.jjtGetChild(1);
		if( astArgs.getArgumentCount() > 0 ) {
			if( ! constructor.beTransitive ) {
				return false;
			}
			/**  # 1   signature match   **
			/**  In this stage, just compare the argc, without comparing the
			 * type of every argument, for getting Type-info is hard from the
			 * AST  **
			ASTArgumentList  astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
			int i = 0;
			int len = constructor.signs.size();
			for( ; i < len; i++) {
				if( astArgList.jjtGetNumChildren() == constructor.signs.get(i).getArgc() ) {
					break;
				}
			}
			if( i >= len ) {
				throw new RuntimeException("Error signature.");
			}
			//ASTExpression astExpr = (ASTExpression) astArgList.jjtGetChild(arg_i);
			Signature sign = constructor.signs.get(i);
			/**  # 2   arguments evaluate(match)   **
			if( sign.isSensitiveArgs(args ) ){  // .isSensitive(node, sclses, scls, sign, ealias) ) {
				isSens = true;
			}
		}
		return isSens;
	} */
	
	public  void dump() {
		logc("-------------- [ Constructor ] -------------[ Begin ]--");
		for( int i = 0; i < signs.size(); i++ ) {
			(signs.get(i)).dump();
		}
		logc("-------------- [ Constructor ] -------------[ End ]--");
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("SensConstructor:: - " + str);
		}
	}
}