package softtest.rules.java.sensdt;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Node;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;

import softtest.ast.java.*;


/**
	<Class Name="File" Package="java.io">
		<Constructor  Transitive="true">
			<Args Argc="2">
				<And>
					<Arg_0  NeedSensitive="true"  Type="String" >
					</Arg_0>
					<Arg_1  NeedSensitive="false"  Type="String" />
				</And>
			</Args>
			<Args  Argc="1" Sensitive="true"  Type="String" >
			</Args>
		</Constructor>
	</Class>
	<Class Name="System" Package="java.lang">
		<Method Name="gc" ReturnType="void" 
		        Sensitive="true">
		</Method>
		<Method Name="getProperty" ReturnType="String"
				Transitive="true">
			<Args   Argc="1" >
				<And>  <!-- using <or> tag is also right when there is only one arg  -->
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
	</Class>
*/
public class  SensClass {
	private  String            className = null;
	private  String            pkgName   = null;
	private  String            fullName = null;
		
	private  SensConstructor   sensConstructor = null;
	private  Hashtable<String, SensMethod>      sensMethods = new Hashtable<String, SensMethod>();
	
	public  SensClass(Node  clsNode) {
		className = clsNode.getAttributes().getNamedItem("Name").getNodeValue();
		pkgName   = clsNode.getAttributes().getNamedItem("Package").getNodeValue();
		fullName  = pkgName + "." + className;
		
		Node chnode = clsNode.getFirstChild();
		for(   ; chnode != null; chnode = chnode.getNextSibling() ) {
			if( chnode.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			}
			if( chnode.getNodeName().equals("Constructor") ) {
				if( null == sensConstructor ) {
					sensConstructor = new SensConstructor( chnode );
				} else {
					throw new RuntimeException("Only one Constructor-tag per class");
				}
			} else if( chnode.getNodeName().equals("Method")){
				SensMethod mthd = new SensMethod( chnode );
				sensMethods.put( mthd.getMethodName(), mthd);
			}
		}
	}
	
	/** argi of params which is found to be sensitive will appeared in args 
	 * **/
	public boolean  isSensitiveConstruction(SimpleJavaNode  node, int[] args) {
		//ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		//ASTAllocationExpression astAlloc = (ASTAllocationExpression) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		//ASTClassOrInterfaceType astType = (ASTClassOrInterfaceType) astAlloc.jjtGetChild(0);
		//String  name = astType.getImage();
		logc1("");
		
		if ( null == sensConstructor ) {
			return false;
		}
		return sensConstructor.isSensitive( node, args );
	}
	
	public boolean  isSensitiveOperation(SimpleJavaNode  node, int[] args) {
		logc2("");
		
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTName  astName = (ASTName) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		String  name 	 = astName.getImage();
		String  names[]	 = name.split("\\.");
		String  mthdName = names[ names.length - 1 ];
		SensMethod  sMthd = sensMethods.get( mthdName );
		if ( null == sMthd ) {
			return false;
		}
		isSens = sMthd.isSensitiveOperResult( node, args );
		return  isSens;
	}
	
	/** Xx.xx(var, "..") , xx = yy = zz ... = mm , "..", var  in new Xxx(..);
	 * **
	public static boolean  isSensitiveConstruct(SimpleJavaNode  node,SensClasses sclses, SensClass scls, ExtendAlias ealias) {
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTAllocationExpression astAlloc = (ASTAllocationExpression) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		ASTClassOrInterfaceType astType = (ASTClassOrInterfaceType) astAlloc.jjtGetChild(0);
		String  name = astType.getImage();
		
		if ( null == scls.sensConstructor ) {
			return false;
		}
		ASTArguments astArgs = (ASTArguments) astAlloc.jjtGetChild(1);
		ASTArgumentList astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
		for(int argi = astArgs.getArgumentCount(); argi >= 0; argi--) {
			ASTExpression astExpr = (ASTExpression) astArgList.jjtGetChild(argi);
			/**  ÷ª¥¶¿Ì    **
			if( ! (astExpr.jjtGetChild(0) instanceof ASTPrimaryExpression )) {
				continue;
			}
			/**  Xx.xx(var, "..")  **
			if( astExpr.jjtGetChild(0).jjtGetNumChildren() == 2 && astExpr.jjtGetChild(0).jjtGetChild(1) instanceof ASTPrimarySuffix) {
				ASTPrimarySuffix astSuffix = (ASTPrimarySuffix)astExpr.jjtGetChild(1);
				ASTArguments     astArgsInner = (ASTArguments) astSuffix.jjtGetChild(0);
				ASTArgumentList  astListInner = (ASTArgumentList) astArgsInner.jjtGetChild(0);
				if( sclses.isSensitive((SimpleJavaNode)astExpr.jjtGetChild(0), sclses, ealias) ) {
					 
				}
			}
			/**  ( a = b = c ... z, .. )  **
			if( astExpr.getSingleChildofType( ASTAssignmentOperator.class ) != null ) {
				
			}
			/**    
			
		}
		return scls.sensConstructor.isSens(node, sclses, scls, scls.sensConstructor, ealias);
	}**/
	
	/** Xx.xx(var, "..") , xx = yy = zz ... = mm , "..", var  in new Xxx(..);
	 * 
	public static boolean  isSensitiveOperate(SimpleJavaNode  node,SensClasses sclses, SensClass scls, ExtendAlias ealias) {
		boolean  isSens = false;
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTName  astName = (ASTName) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
		String  name 	 = astName.getImage();
		String  names[]	 = name.split("\\.");
		String  mthdName = names[ names.length - 1 ];
		SensMethod  sMthd = scls.sensMethods.get( mthdName );
		if ( null == sMthd ) {
			return false;
		}
		isSens = SensMethod.isSensitiveResult(node, sclses, scls, sMthd, ealias);
		return  isSens;
	}**/
	
	public String getClassName() {
		return className;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void dump() {
		logc("------------------ [ Class] -----------------[ Begin ]--" + className);
		if( sensConstructor == null) {
			logc( "No Constructor" );
		} else {
			sensConstructor.dump();
		}
		for( Enumeration<SensMethod>  e = sensMethods.elements(); e.hasMoreElements(); ) {
			SensMethod  mthd = e.nextElement();
			mthd.dump();
		}
		logc("------------------ [ Class] -----------------[ End ]--");
	}
	
	public static void logc1(String str) {
		logc("isSensitiveConstruction(..) - " + str);
	}
	public static void logc2(String str) {
		logc("isSensitiveOperation(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("SensClass::" + str);
		}
	}

	public String getFullName() {
		return fullName;
	}
	
	@Override
	public String toString() {
		//StringBuffer sb = new StringBuffer();
		//sb.append(className);
		//sb.append(" pkg:");		sb.append( pkgName );
		return  className;//fullName;//sb.toString();
	}
}
