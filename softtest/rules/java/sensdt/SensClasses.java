package softtest.rules.java.sensdt;

import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;

import org.w3c.dom.Node;

import softtest.ast.java.*;
import softtest.config.java.Config;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.rules.java.sensdt.SensClass;
import softtest.rules.java.*;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.symboltable.java.*;



/**
<DsnInfoLeak-Data>
	<SensResource  Type="String" >
		<Item Name="designInfo_class">web.root</Item>
		<Item Name="designInfo_class">root</Item>
		<Item Name="mima">password</Item>
		<Item Name="mima">PASSWORD</Item>
	</SensResource>
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
</DsnInfoLeak-Data>
 */



public class SensClasses {
	
	private Hashtable<String, SensClass> fullNameClasses = new Hashtable<String, SensClass>();
	private Hashtable<String, SensClass> partNameClasses = new Hashtable<String, SensClass>();
	
	public void     addSensClass(Node clsNode) {
		SensClass  cls = new SensClass( clsNode );
		fullNameClasses.put( cls.getFullName(), cls);
		partNameClasses.put(cls.getClassName(), cls);
	}
	
	
	/**  判断 new Xxx(..);  Xxx.xx(..);  [xx.]xx()[.xx()]; 所得到的结果是否是敏感的。
	 * 基本前缀可能是:
	 *   a) AllocationExpression -- new Xxx
	 *   b) PrimaryPrefix        -- Xxx.xx   [xx.]xx
	 *     [xx.]xx()[.xx()]的情况还没有处理
	 * 参数部分可能是：
	 * Xx.xx(..)      new Xx()         ".."       xx   
	 * xx = xx(..)    xx = new Xx()    xx=".."    xx=yy=zz=ee  
	 * x = [xx.]xx()[.xx()]  ( Undone ) !
	 * **/
	public static boolean  isSensitive(SimpleJavaNode  node, SensClasses sclses, ExtendAlias ealias) {
		logc1("ealias : " + ealias.getResourceName());
		
		boolean  besens = false;
		if( ! (node instanceof ASTPrimaryExpression) ) {
			logc1( "?????????? " + node.printNode(ProjectAnalysis.getCurrent_file()) );
			throw  new RuntimeException("Arg0 of isSensitive(..) should be PrimaryExpression");
		}
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		LinkedList sensArgs = new LinkedList();
		
		ASTArguments astArgs = null;
		boolean     dealmore = false;
		boolean     dealNew  = false; //  new Xxx()
		boolean     dealXx   = false; //  Xxx.xx()
		boolean     dealxx   = false; //  [xxx.]xx()[.xx()]
		SensClass    sClass   = null;
		//  new Xxxx(...)
		if( astPrimExpr.jjtGetChild(0).jjtGetChild(0) instanceof ASTAllocationExpression ) {
			// Step 1: 查找 Xxx，如果没有找到则表明 Xxx 不是敏感类
			ASTAllocationExpression astAlloc = (ASTAllocationExpression) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
			ASTClassOrInterfaceType astType = (ASTClassOrInterfaceType) astAlloc.jjtGetChild(0);
			String  name = astType.getImage();
			/**  names[0] should be the XXXClass name  **/
			sClass = sclses.partNameClasses.get( name );
			if( sClass == null ) {
				sClass = sclses.fullNameClasses.get(name);
			}
			if( sClass == null ) {
				return false;
			}			
			astArgs = (ASTArguments) astAlloc.jjtGetChild(1);
			dealmore = true;
			dealNew  = true;
		}
		//  [xx.]xx()[(.xx)|(.xx())]+    may be processed in the future.
		else if(astPrimExpr.jjtGetNumChildren() > 2 && astPrimExpr.jjtGetChild(0).jjtGetChild(0) instanceof ASTName ) {
			logc1("\\-_-/  /*_*\\ \\+_+/  Case [xx.]xx()[(.xx)|(.xx())]+ not thinking out");
			return false;
		}
		//  Xxx.xx(..);  [xx.]xx(..);  but [xx.]xx(..); can not process now
		else if( astPrimExpr.jjtGetNumChildren() == 2 && astPrimExpr.jjtGetChild(0).jjtGetChild(0) instanceof ASTName ) {
			ASTName  astName = (ASTName) astPrimExpr.jjtGetChild(0).jjtGetChild(0);
			ASTPrimarySuffix astSuffix = (ASTPrimarySuffix) astPrimExpr.jjtGetChild(1);
			String  name = astName.getImage();
			String  names[] = name.split("\\.");
			/**  names[0] could be :
			 *      Xxx  :  class name
			 *      xx   :  variable name
			 *      xx   :  method name     **/
			{
				sClass = sclses.partNameClasses.get( names[0] );
				if( sClass == null ) {
					sClass = sclses.fullNameClasses.get(names[0]);
				}
			}
			//  Xxx.xx(..)  and Xxx is defined in the DsnInfo-Data.xml
			if( sClass != null ) {
				astArgs = (ASTArguments) astSuffix.jjtGetChild(0);
				dealmore = true;
				dealXx   = true;
			}
			else {
				//  Xx.xx(..) and Xx is a Class that not defined in the DsnInfo-Data.xml
				if( Character.isUpperCase( names[0].charAt(0) )) {
					return  false;
				}
				//  (xx.)+xx()([( .xx )|( .xx() )])+
				
				//  for now, just process the case :  xx.xx(); and .xx must be .toString
				List ndecls = astName.getNameDeclarationList();
				if( ndecls.size() > 0 && ndecls.get(0) instanceof VariableNameDeclaration ) {
					if( names.length > 1 ) {
						if( names[1].equals("toString") ) {
							if( ealias.getSensEntity().contains((VariableNameDeclaration)ndecls.get(0))
							 || ealias.getSensSource().contains((VariableNameDeclaration)ndecls.get(0))) {
		//  Error here!!!!!   no getSensSource ^  ????????????????????????????????
								return true;
							} else {
								return false;
							}
						}
						else
						if( names[names.length-1].equals("getProperty")){
							logc1("method process for temp :" + names[1] );
							ASTName astArgName = (ASTName)astSuffix.getSingleChildofType(ASTName.class);
							if(astArgName != null) {
								NameDeclaration ndecl = astArgName.getNameDeclaration();
								if( ndecl instanceof VariableNameDeclaration ) {
									if( ealias.getSensSource().contains((VariableNameDeclaration)ndecl)) {
										besens = true;
									}
									if( ealias.getSensEntity().contains((VariableNameDeclaration)ndecl)) {
										besens = true;
									}
								}
							}
							ASTLiteral astArgLit = (ASTLiteral)astSuffix.getSingleChildofType(ASTLiteral.class);
							if(astArgLit != null && astArgLit.isStringLiteral()) {
								String  lit = astArgLit.getImage();
								lit = lit.substring(1, lit.length()-1);
								if( ealias.getResourceName().equals(lit) ) {
									besens = true;
								}
							}
						}
					} else {
						logc1("names.length == 1 : " + names[0] );
					}
				}
				else {
					logc1("may be method:" + names[0]);
					//  If the method is [xx.]getProperty(".."|xx) and ".."|xx is in sensSource
					//  or sensEntity, it is sensitive
					if( (names.length == 1 && names[0].equals("getProperty"))
						||(names.length == 2 && names[1].equals("getProperty")) ) {
						ASTName astArgName = (ASTName)astSuffix.getSingleChildofType(ASTName.class);
						if(astArgName != null) {
							NameDeclaration ndecl = astArgName.getNameDeclaration();
							if( ndecl instanceof VariableNameDeclaration ) {
								if( ealias.getSensSource().contains((VariableNameDeclaration)ndecl)) {
									besens = true;
								}
								if( ealias.getSensEntity().contains((VariableNameDeclaration)ndecl)) {
									besens = true;
								}
							}
						}
						ASTLiteral astArgLit = (ASTLiteral)astSuffix.getSingleChildofType(ASTLiteral.class);
						if(astArgLit != null && astArgLit.isStringLiteral()) {
							String  lit = astArgLit.getImage();
							lit = lit.substring(1, lit.length()-1);
							if( ealias.getResourceName().equals(lit) ) {
								besens = true;
							}
						}
					}
				}
			}
			if( names.length > 2 ) {
				// Case :  Xxxx.xx.xx(..)  staticMethod  **/
			}
		}
		//  xx : ASTName     ( astPrimExpr = node )
		else if(null != astPrimExpr.getSingleChildofType(ASTName.class)) {
			ASTName xx = (ASTName) astPrimExpr.getSingleChildofType(ASTName.class);
			VariableNameDeclaration vdecl = (VariableNameDeclaration)xx.getNameDeclaration();
			if( ealias.getSensEntity().contains( vdecl ) ) {
				return true;
			}
		} else {
			logc1("???????? Unexpected Node Type : " + astPrimExpr + "  " + astPrimExpr.printNode(ProjectAnalysis.getCurrent_file()) + " which begins at " + astPrimExpr.getBeginLine());
		}
		
		//	 Step 2: 从左至右遍历各个参数，参数的形式可能为
		//   Xx.xx(..)     new Xx(..)          xx              "..."
		//   xx=yy=zz=ee   xx = new Xx(..)     xx = xx.(..)    xx = ".."
		//   参数为表达式(xx + yy + "..")(xx * yy) 的情况暂时不考虑
		// dealmore 是要求处理参数部分，dealXx是要求调用isSensOperation,dealNew
		// 是要求调用isSensConstructor
		if( dealmore ) {
			if( astArgs.getArgumentCount() == 0 ) {
				logc1("(void)");
				return false;
			}
			ASTArgumentList astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
			int  argc = astArgs.getArgumentCount();
			logc1("Args.printNode: " + astArgs.printNode(ProjectAnalysis.getCurrent_file()));
			for(int argi = 0; argi < argc; argi++) {
				ASTExpression astExpr = (ASTExpression) astArgList.jjtGetChild(argi);
				
				logc1("-----------------------------[bgn] " + argi + " " + astExpr.printNode(ProjectAnalysis.getCurrent_file()));
				
				/**  不处理 AddictiveExpression 等   **/
				if( ! (astExpr.jjtGetChild(0) instanceof ASTPrimaryExpression )) {
					logc1("-----------------------------[bgn] " + argi + " " + astExpr.printNode(ProjectAnalysis.getCurrent_file()));
					continue;
				}
				/**  xx="..";   xx=new Xx(..);   xx=yy=zz;   xx=xx(..);  **/
				if( astExpr.jjtGetNumChildren() > 1 && astExpr.jjtGetChild(1) instanceof ASTAssignmentOperator ) {
					ASTPrimaryExpression leftPrim = (ASTPrimaryExpression) astExpr.jjtGetChild(0);
					ASTExpression  rightExpr = (ASTExpression) astExpr.jjtGetChild(2);
					ASTName left = (ASTName)leftPrim.getSingleChildofType(ASTName.class);
					NameDeclaration decl = left.getNameDeclaration();
					if( ! (decl instanceof VariableNameDeclaration)) {
						logc1("-----------------------------[end] " + argi + " is not VarNameDecl :" + decl);
						continue;
					}
					//  xx="...";  xx = null;
					if( rightExpr.getSingleChildofType(ASTLiteral.class) != null) {
						ASTLiteral liter = (ASTLiteral) rightExpr.getSingleChildofType(ASTLiteral.class);
						// xx = null;
						if( liter.getSingleChildofType(ASTNullLiteral.class) != null) {
							ealias.removeSensEntity((VariableNameDeclaration)decl);
							ealias.removeSensSource((VariableNameDeclaration)decl);
							logc1("Case xx = null;  remove from sens[Entity|Source]");
						}
						// 
						else if( ! liter.isStringLiteral() ) {
							ealias.removeSensEntity((VariableNameDeclaration)decl);
							ealias.removeSensSource((VariableNameDeclaration)decl);
							logc1("-----------------------------[bgn] " + argi);
							continue;
						}
						// xx = "..";
						else {
							String withquote = liter.getImage();
							String noquote   = withquote.substring(1, withquote.length() - 1);
							if( noquote.equals(ealias.getResourceName()) ) {
								ealias.addSensSource((VariableNameDeclaration)decl);
								ealias.removeSensEntity((VariableNameDeclaration)decl);
								sensArgs.add(argi);
								logc1("Case xx = \"..\";  add to sensSource");
							} else {
								ealias.removeSensSource((VariableNameDeclaration)decl);
								ealias.removeSensEntity((VariableNameDeclaration)decl);
								logc1("Case xx = \"..\";  not sensSource");
							}
						}
					}
					//  xx=new Xx(..)
					else
					if( rightExpr.getSingleChildofType(ASTAllocationExpression.class) != null ) {
						ASTAllocationExpression alloc = (ASTAllocationExpression)rightExpr.getSingleChildofType(ASTAllocationExpression.class);
						ASTPrimaryExpression ppOfAlloc = (ASTPrimaryExpression)alloc.jjtGetParent().jjtGetParent();
						if( SensClasses.isSensitive(ppOfAlloc, sclses, ealias)) {
							ealias.addSensEntity( (VariableNameDeclaration)decl );
							sensArgs.add(argi);
							logc1("Case xx = new Xx(..);  add to sensEntity");
						} else {
							ealias.removeSensSource((VariableNameDeclaration)decl);
							ealias.removeSensEntity((VariableNameDeclaration)decl);
							logc1("Case xx = new Xx(..);  not sensEntity");
						}
					}
					//  xx=yy=..=zz    rightExpr is yy=..=zz
					else
					if( rightExpr.jjtGetNumChildren() == 3 && rightExpr.jjtGetChild(1) instanceof ASTAssignmentOperator) {
						ASTExpression ppzz = (ASTExpression) rightExpr.jjtGetChild(2);
						ASTExpression rightOfyy = ppzz;
						while( ppzz.jjtGetNumChildren() == 3 && ppzz.jjtGetChild(1) instanceof ASTAssignmentOperator ) {
							ppzz = (ASTExpression)ppzz.jjtGetChild(2);
						}
						ASTName  mostRight = (ASTName) ppzz.getSingleChildofType(ASTName.class);
						NameDeclaration  mostRightDecl = mostRight.getNameDeclaration();
						if( mostRightDecl instanceof VariableNameDeclaration ) {
							//  zz is sensSource
							if( ealias.getSensSource().contains((VariableNameDeclaration)mostRightDecl) ) {
								NameDeclaration ppDecl;
								ASTName ppName, yyName;
								while( ppzz != rightOfyy ) {
									ppzz = (ASTExpression) ppzz.jjtGetParent();
									ppName = (ASTName) ppzz.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
									ppDecl = ppName.getNameDeclaration();
									ealias.getSensSource().add((VariableNameDeclaration) ppDecl);
									ealias.removeSensEntity((VariableNameDeclaration)decl);
									logc1("Case xx=yy=..=zz;  add in sensSource");
								}
								yyName = (ASTName)rightExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
								ppDecl = yyName.getNameDeclaration();
								ealias.getSensSource().add((VariableNameDeclaration) ppDecl);
								ealias.removeSensEntity((VariableNameDeclaration)decl);
								sensArgs.add(argi);
								logc1("Case xx=yy=..=zz;  add in sensSource");
							}
							else
							if( ealias.getSensEntity().contains((VariableNameDeclaration)mostRightDecl) ) {
								NameDeclaration ppDecl;
								ASTName ppName, yyName;
								while( ppzz != rightOfyy ) {
									ppzz = (ASTExpression) ppzz.jjtGetParent();
									ppName = (ASTName) ppzz.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
									ppDecl = ppName.getNameDeclaration();
									ealias.getSensEntity().add((VariableNameDeclaration) ppDecl);
									ealias.removeSensSource((VariableNameDeclaration)decl);
									logc1("Case xx=yy=..=zz;  add in sensEntity");
								}
								yyName = (ASTName)rightExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
								ppDecl = yyName.getNameDeclaration();
								ealias.getSensEntity().add((VariableNameDeclaration) ppDecl);
								ealias.removeSensSource((VariableNameDeclaration)decl);
								sensArgs.add(argi);
								logc1("Case xx=yy=..=zz;  add in sensEntity");
							}
						}
					}
					//  xx=yy    rightExpr is yy   应该把 xx=yy=..=zz合并起来的。
					else
					if(rightExpr.jjtGetNumChildren() == 1 && rightExpr.getSingleChildofType(ASTName.class) != null) {
						ASTName yyName = (ASTName) rightExpr.getSingleChildofType(ASTName.class);
						VariableNameDeclaration  yyDecl = (VariableNameDeclaration)yyName.getNameDeclaration();
						if(ealias.getSensEntity().contains(yyDecl)) {
							ealias.getSensEntity().add(yyDecl);
							ealias.removeSensSource((VariableNameDeclaration)decl);							
							sensArgs.add(argi);
							logc1("Case xx=yy;  add in sensEntity");
						} else if(ealias.getSensSource().contains(yyDecl)){
							ealias.getSensSource().add(yyDecl);
							ealias.removeSensEntity((VariableNameDeclaration)decl);
							sensArgs.add(argi);
							logc1("Case xx=yy;  add in sensSource");
						}
					}
					//  xx=xx(..)
					else 
					if(rightExpr.jjtGetNumChildren() == 1 && rightExpr.jjtGetChild(0).jjtGetNumChildren() > 1) {
						ASTPrimaryExpression xxPrimExpr = (ASTPrimaryExpression) rightExpr.jjtGetChild(0);
						if( SensClasses.isSensitive(xxPrimExpr, sclses, ealias) ) {
							ealias.addSensEntity((VariableNameDeclaration)decl);
							ealias.removeSensSource((VariableNameDeclaration)decl);
							sensArgs.add(argi);
							logc1("Case xx=xx(..);  add in sensEntity");
						}
					}
				} // end of  if  xx = [yy=..=zz | ".." | xx(..) | new Xx(..) ]  
				else
				//  Xx.xx(var, "..")
				if( astExpr.jjtGetNumChildren() == 1 
					&& astExpr.jjtGetChild(0).jjtGetNumChildren() == 2 
					&& astExpr.jjtGetChild(0).jjtGetChild(1) instanceof ASTPrimarySuffix) {
					
					ASTPrimaryExpression xxPrimExpr = (ASTPrimaryExpression) astExpr.jjtGetChild(0);
					if( SensClasses.isSensitive(xxPrimExpr, sclses, ealias) ) {
						 sensArgs.add(argi);
					}
				}
				else
				//  new Xx(..)
				if( astExpr.getSingleChildofType(ASTAllocationExpression.class) != null ) {
					ASTPrimaryExpression prim = (ASTPrimaryExpression) astExpr.jjtGetChild(0);
					if( SensClasses.isSensitive(prim, sclses, ealias) ) {
						sensArgs.add(argi);
					}
				}
				else
				//  xx
				if( astExpr.getSingleChildofType(ASTName.class) != null ) {
					ASTName xx = (ASTName) astExpr.getSingleChildofType(ASTName.class);
					VariableNameDeclaration decl = (VariableNameDeclaration) xx.getNameDeclaration();
					if( ealias.getSensEntity().contains(decl) ) {
						sensArgs.add(argi);
						logc1("Case : xx   is SensEntity");
					} else if( ealias.getSensSource().contains(decl)) {
						sensArgs.add(argi);
						logc1("Case : xx   is SensSource");
					} else {
						logc1("Case : xx   not in Sens[Entity | Source]");
					}
				}
				else
				//  "..."
				if( astExpr.getSingleChildofType( ASTLiteral.class ) != null ) {
					ASTLiteral lit = (ASTLiteral) astExpr.getSingleChildofType( ASTLiteral.class );
					if( lit.isStringLiteral() ) {
						String withquote = lit.getImage();
						String noquote   = withquote.substring(1, withquote.length()-1);
						SensInfo sinfo = DsnInfoLeakStateMachine.sInfo;
						if( sinfo.isSensInfo(noquote) ) {
							if( ealias.getResourceName().equals(noquote) ) {
								sensArgs.add(argi);
								logc1("Case : \"..\"   is sensinfo");
							} else {
								logc1("Case \"..\" :  sensifno "+noquote+"but not equal to " + ealias.getResourceName());
							}
						} else {
							logc1("Case : \"..\"   not sensinfo");
						}
					}
				}
				
				logc1("-----------------------------[end] " + argi+ " " + astExpr.printNode(ProjectAnalysis.getCurrent_file()));

			}// end of  for(int argi = 0; argi < argc; argi++)
			int  args[] = new int[sensArgs.size()];
			for(int i = 0; i < args.length; i++) {
				args[i] = (Integer)sensArgs.get(i);
			}
			logary(args);
			if( dealNew ) {
				besens = sClass.isSensitiveConstruction(node, args);
			} else if( dealXx ){
				besens = sClass.isSensitiveOperation(node, args);
			}
			//besens = sClass.isSensitiveOperate( node, sclses, sClass, ealias);
			ealias.dump();
			logc1("[class] " + sClass.toString() + "  [result] " + besens );
		} else {
			//logc1("???????? Unexpected Node Type : " + astPrimExpr + "  " + astPrimExpr.printNode() + " which begins at " + astPrimExpr.getBeginLine());
			logc1("" + astPrimExpr + " has no (...) or xx.xx.xx() or xx(), and do not need deal more");
		}
		
		return besens;
	}
	
	public void dump() {
		logc("============================== [ Classes ] =================[ Begin ]==");
		for( Enumeration<SensClass>  e = partNameClasses.elements(); e.hasMoreElements(); ) {
			SensClass  cls = e.nextElement();
			cls.dump();
		}
		logc("============================== [ Classes ] =================[  End  ]==");
	}
	
	public static void   logary(int [] a) {
		if(Config.DEBUG) {
			System.out.print("int args[]:");
			for(int i = 0; i < a.length; i++ ) {
				System.out.print(" " + a[i]);
			}
			System.out.println();
		}
	}
	public static void   logc1(String str) {
		logc("isSensitive(..) - " + str);
	}

	public static void   logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("SensClasses::" + str);
		}
	}
}



