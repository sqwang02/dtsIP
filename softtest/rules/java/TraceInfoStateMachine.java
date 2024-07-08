package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
7．作为密码的字符串可被跟踪问题          undone
密码字符串可以通过对从文件存储器或者网络进行跟踪。
【例2-11】 下列程序
   1   public static void main(String[] args)throws SQLException,    2   FileNotFoundException,IOException {
   3   			Properties info = new Properties();
   4   			final FileInputStream  st = new FileInputStream("config.ini");
   5   			info.load(st);
   6   			st.close();
   7   			DriverManager.getConnection("jdbc:mysql://localhost:3307", info);
   8   }

 */

public class TraceInfoStateMachine {

	public static List<FSMMachineInstance> createTraceInfoStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//PrimaryExpression[./PrimaryPrefix/Name[@Image='DriverManager.getConnection'] ]";
		
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if( evalRlts == null || evalRlts.size() == 0 ) {
			return list;
		}
		Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
		for(int i = 0; i < evalRlts.size(); i++) {
			ASTPrimaryExpression  astPrimExpr = ( ASTPrimaryExpression ) evalRlts.get(i);
			ASTArgumentList astArglst = (ASTArgumentList) astPrimExpr.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
			if( astArglst.jjtGetNumChildren() != 2 ) {
				continue;
			}
			ASTExpression astArg_1 = (ASTExpression) astArglst.jjtGetChild(1);
			ASTName  astNameInfo   = (ASTName) astArg_1.getSingleChildofType(ASTName.class);
			if( astNameInfo == null) {
				continue;
			}
			NameDeclaration decl = astNameInfo.getNameDeclaration();
			if ( ! (decl instanceof VariableNameDeclaration )) {
				continue;
			}
			VariableNameDeclaration infoDecl = (VariableNameDeclaration) decl;
			Graph  g = getGraph(astPrimExpr);
			
			SimpleJavaNode srch = astNameInfo;
			while( true ) {
				SimpleJavaNode  result = analyzeAsgnTo(srch, g);
				if( result == null ) {
					break;
				}
				//  xx.load(  new Xxx("..")  )
				if( result instanceof ASTLiteral ) {
					// just create the fsm
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(result));
					table.put(infoDecl, fsmInst);		
					break;
				}
				//  xx=..=zz;  ..=xx=..=zz;  xx.load(zz);
				else if (result instanceof ASTName) {
					logc1("-------> " + result.printNode(ProjectAnalysis.getCurrent_file()));
					srch = result;
				}
				//  Xxx xx = [yy|".."|new Xx(..)]
				else if (result instanceof ASTVariableDeclaratorId ) {
					
				}
			}
			//logc1("getScope:" + vDecl.getScope());
			//logc1("getDeclareScope:" + vDecl.getDeclareScope());

		}
		for( Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements(); ) {
			list.add( e.nextElement() );
		}

		return list;
	}

	/**
	 * This method is used to match :DriverManager.getConnection("", user, "pwd"); 
	 * this method may do nothing except return true.
	 */
	public static boolean checkTraceInfo(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		
		logc2("+-----------------+");
		logc2("| InfoCanBeTraced |  :" + fsmInst.getRelatedObject().getTagTreeNode().getImage() );
		logc2("+-----------------+");
		found = true;
		fsmInst.setResultString("pwd text :" + fsmInst.getRelatedObject().getTagTreeNode().getImage());
		
		return found;
	}
	
	/**  new Xxx()
	 *   xx = new Xxx()    xx = xx
	 *   if the info can be traced, return true.
	 */
	private static boolean analyzeSuffix(SimpleJavaNode node) {
		boolean  found = false;
		ASTExpression astExpr = (ASTExpression)((SimpleJavaNode) node.jjtGetChild(1)).getSingleChildofType(ASTExpression.class);
		// xx = new Xxx
		if( astExpr.jjtGetNumChildren() == 3 ) {
			ASTAllocationExpression astAloc = null;
			astAloc = (ASTAllocationExpression)((SimpleJavaNode)astExpr.jjtGetChild(2)).getSingleChildofType(ASTAllocationExpression.class);
			//  xx = new Xxx(..)
			if( null !=  astAloc) {
				ASTArgumentList astArgl = (ASTArgumentList) astAloc.jjtGetChild(1).jjtGetChild(0);
				ASTExpression astArg_0 = (ASTExpression)astArgl.jjtGetChild(0);
				//  xx = new Xxx("..")
				if( null != astArg_0.getSingleChildofType( ASTLiteral.class )) {
					return true;
				}
				//  xx = new Xxx(xx)
				else {
					ASTName astxx = (ASTName)astArg_0.getSingleChildofType(ASTName.class);
					//if( analyzeAsgnTo( astxx ) ) {
						return true;
					}
				}
			}
			//  xx = xx
			else 
			{
				
			}

		return found;
	}
	
	/**  Analyze the assignment to xx, to find the case :
	 *   xx=yy=..=zz;    \
	 *   xx=zz;           +---> return the ASTName of zz
	 *   cc=..=xx=..=zz; /
	 *   -------------------------------------------------------
	 *   xx=..=new Xx([zz|".."])  +----> return the ASTName of zz or ASTLiteral
	 *   -------------------------------------------------------
	 *   xx.load( new FileInputStream("...") );    \
	 *   xx.load( new FileInputStream( zz ) );      +--> return the ASTName or ASTLiteral
	 *   xx.load( fis=new FileInputStream("..") );  |
	 *   xx.load( fis=new FileInputStream( zz ) ); /
	 *   -------------------------------------------------------
	 *   xx.load( fis=fis_a=..=fis_z ); +----> return the ASTName of fis_z
	 *   xx.load( fis );                +----> return the ASTName of fis
	 *   
	 *   node : ASTName.
	 *   return the ASTName node of zz  or ASTLiteral
	 */
	private static SimpleJavaNode analyzeAsgnTo(SimpleJavaNode node, Graph  g) {
		ASTName zzName = null;
		ASTName astxx = (ASTName) node;
		logc3("=======>" + node +"  " + node.printNode(ProjectAnalysis.getCurrent_file()) + "  " + node.getBeginLine());
		if( !(astxx.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return null;
		}
		VariableNameDeclaration xxVdecl = (VariableNameDeclaration)astxx.getNameDeclaration();
		ArrayList xxOccs = (ArrayList)xxVdecl.getDeclareScope().getVariableDeclarations().get(xxVdecl);
		int size = xxOccs.size();
		int i = size-1;
		for( ; i >= 0; i--) {
			NameOccurrence xxOcc = (NameOccurrence) xxOccs.get(i);
			if( xxOcc.isOnLeftHandSide()) {
				// 如果赋值语句最右边还是其本身，则跳过
				SimpleJavaNode p = (SimpleJavaNode)xxOcc.getLocation();
				if(p.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetNumChildren() == 3) {
					ASTExpression right = (ASTExpression)p.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(2);
					while( right.jjtGetNumChildren() == 3 ) {
						right = (ASTExpression)right.jjtGetChild(2);
					}
					if(null != right.getSingleChildofType(ASTName.class)) {
						ASTName  mostRight = (ASTName)right.getSingleChildofType(ASTName.class);
						if( mostRight.getNameDeclaration() instanceof VariableNameDeclaration ) {
							VariableNameDeclaration vdecl = (VariableNameDeclaration)mostRight.getNameDeclaration();
							if( vdecl == astxx.getNameDeclaration()) {
								continue;
							}
						}
					}
				}
			}
			// 首先要确保xxOcc是位于node之前
			if( node != xxOcc.getLocation() && xxOcc.getLocation().getBeginLine() <= node.getBeginLine() ) {
				// 然后确认从xxOcc所在的控制流节点能够到达node所在的控制流节点
				VexNode xxVex = xxOcc.getLocation().getCurrentVexNode();
				VexNode toVex = node.getCurrentVexNode();
				if( existPath(xxVex, toVex, g) ) {
					SimpleJavaNode xxNode = (SimpleJavaNode)xxOcc.getLocation();
					if( xxNode instanceof ASTName ) {
						ASTName  xxName = (ASTName) xxNode;
						String   str = xxName.getImage();
						String  strs[] = str.split("\\.");
						// xx.load(..)
						if( strs.length > 1 ) {
							if( strs[1].equals("load") ) {
								ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression)xxName.jjtGetParent().jjtGetParent();
								// xx.load(zz);
								if(null != ((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTName.class) ) {
									zzName = (ASTName)((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTName.class);
									break;
								}
								else
								// xx.load(new Xxx([".."|zz]))
								if(null != ((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTAllocationExpression.class)) {
									ASTAllocationExpression paramNew = (ASTAllocationExpression)((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTAllocationExpression.class);
									ASTLiteral astLit = (ASTLiteral)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTLiteral.class);
									if(null != astLit) {
										 return astLit;
									}
									zzName = (ASTName)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTName.class);
									break;
								}
								else
								// x.load( xx=...... )  ... could be:
								//    xx=..=zz;  xx=..=new Xxx(..);  
								if( null != ((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTExpression.class) ) {
									ASTExpression astExpr = (ASTExpression)((SimpleJavaNode)astPrimExpr.jjtGetChild(1)).getSingleChildofType(ASTExpression.class);
									while( astExpr.jjtGetNumChildren() == 3 && astExpr.jjtGetChild(1) instanceof ASTAssignmentOperator) {
										astExpr = (ASTExpression)astExpr.jjtGetChild(2);
									}
									// astExpr could be [new Xxx(..)|zz]
									ASTAllocationExpression alloc = (ASTAllocationExpression)((SimpleJavaNode)astExpr.jjtGetChild(0)).getSingleChildofType(ASTAllocationExpression.class);
									if( null != alloc ) {
										ASTLiteral astLit = (ASTLiteral)((SimpleJavaNode)(alloc.jjtGetChild(1))).getSingleChildofType(ASTLiteral.class);
										if(null != astLit) {
											 return astLit;
										}
										zzName = (ASTName)((SimpleJavaNode)(alloc.jjtGetChild(1))).getSingleChildofType(ASTName.class);
										break;	
									}
									zzName =(ASTName)((SimpleJavaNode)astExpr.jjtGetChild(0)).getSingleChildofType(ASTName.class);
									break;
								} else {
									logc3("???  Very Strange, can't process :" + astPrimExpr.printNode(ProjectAnalysis.getCurrent_file()));
								}
							}
						}
						// xx=..;  xx
						else
						//	xx=yy=..zz;  xx=..=zz=new Xx([xx|".."]);  xx=..="..";
						if( xxNode.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTStatementExpression ) {
							SimpleJavaNode statement = (SimpleJavaNode) xxNode.jjtGetParent().jjtGetParent().jjtGetParent();
							while( statement.jjtGetNumChildren() == 3 && statement.jjtGetChild(1) instanceof ASTAssignmentOperator ) {
								statement = (SimpleJavaNode) statement.jjtGetChild(2);
							}
							if(null != statement.getSingleChildofType(ASTName.class)) {
								zzName = (ASTName) statement.getSingleChildofType(ASTName.class);
								break;
							}
							else
							if(null != statement.getSingleChildofType(ASTAllocationExpression.class)){
								ASTAllocationExpression paramNew = (ASTAllocationExpression)((SimpleJavaNode)statement.jjtGetChild(1)).getSingleChildofType(ASTAllocationExpression.class);
								ASTLiteral astLit = (ASTLiteral)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTLiteral.class);
								if(null != astLit) {
									 return astLit;
								}
								zzName = (ASTName)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTName.class);
								break;
							}else {
								logc3("????? unexpected :" + statement + "  " + statement.printNode(ProjectAnalysis.getCurrent_file()));
							}
						}
						// cc=..=xx=..=zz;  xx
						else if( xxNode.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTExpression ) {
							SimpleJavaNode right = (SimpleJavaNode) xxNode.jjtGetParent().jjtGetParent().jjtGetParent();
							while( right.jjtGetNumChildren() == 3 && right.jjtGetChild(1) instanceof ASTAssignmentOperator ) {
								right = (SimpleJavaNode) right.jjtGetChild(2);
							}
							if(null != right.getSingleChildofType(ASTName.class)) {
								zzName = (ASTName) right.getSingleChildofType(ASTName.class);
								break;
							}
							else 
							if(null != right.getSingleChildofType(ASTAllocationExpression.class)){
								ASTAllocationExpression paramNew = (ASTAllocationExpression)((SimpleJavaNode)right.jjtGetChild(1)).getSingleChildofType(ASTAllocationExpression.class);
								ASTLiteral astLit = (ASTLiteral)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTLiteral.class);
								if(null != astLit) {
									 return astLit;
								}
								zzName = (ASTName)((SimpleJavaNode)(paramNew.jjtGetChild(1))).getSingleChildofType(ASTName.class);
								break;
							}
							else{
								logc3("????? unexpected :" + right + "  " + right.printNode(ProjectAnalysis.getCurrent_file()));
							}
						}
						
					} else {
						logc3("???? " + xxNode);
					}
					
				} else {
					logc3("no path");
				}
			}
		}
		//  Case of  Xxx xx = [zz|""|new Xx([xx|""])|null];
		if( i == -1 && zzName == null ) {
			VariableNameDeclaration vdecl = (VariableNameDeclaration)astxx.getNameDeclaration();
			SimpleJavaNode xx = (SimpleJavaNode)vdecl.getNode();
			logc3("_" +xx + "  " + xx.getBeginLine() + "  " + xx.getImage() + "  " + xxOccs.size());
			while( xx != null && !(xx instanceof ASTVariableDeclarator) ) {
				xx = (SimpleJavaNode)xx.jjtGetParent();
			}
			if( xx.jjtGetNumChildren() == 2 ) {
				ASTVariableInitializer ini = (ASTVariableInitializer)xx.jjtGetChild(1);
				if(null != ini.getSingleChildofType(ASTName.class)) {
					zzName = (ASTName)ini.getSingleChildofType(ASTName.class);
				}
				else
				if(null != ini.getSingleChildofType(ASTLiteral.class)) {
					return (ASTLiteral)ini.getSingleChildofType(ASTLiteral.class);
				}
				else
				if(null != ini.getSingleChildofType(ASTAllocationExpression.class)) {
					ASTAllocationExpression alloc = (ASTAllocationExpression) ini.getSingleChildofType(ASTAllocationExpression.class);
					ASTLiteral astLit = (ASTLiteral)((SimpleJavaNode)(alloc.jjtGetChild(1))).getSingleChildofType(ASTLiteral.class);
					if(null != astLit) {
						 return astLit;
					}
					zzName = (ASTName)((SimpleJavaNode)(alloc.jjtGetChild(1))).getSingleChildofType(ASTName.class);
				}
				else {
					logc3("Initializer unresolved : " + xx + " " + xx.printNode(ProjectAnalysis.getCurrent_file()));
				}
			}
		}
		logc3("return :" + zzName);
		return zzName;
	}
	
	private static boolean  existPath(VexNode from, VexNode to, Graph  g) {
		if( from == to ) {
			return true;
		}
		boolean  exist = false;
		Stack<VexNode> stack = new Stack<VexNode>();
		from.setVisited(true);
		stack.push( from );
		while( ! stack.isEmpty() ) {
			VexNode newNode = g.getAdjUnvisitedVertex(stack.peek());
			if( newNode == null ) {
				stack.pop();
			}else {
				if( newNode == to ) {
					exist = true;
					break;
				}
				newNode.setVisited(true);
				stack.push(newNode);
			}
		}
		g.clearVisited();
		if( exist ) {
			logc4(from.getName() + "  to " + to.getName() + "  T");
		} else {
			logc4(from.getName() + "  to " + to.getName() + "  F");
		}
		return  exist;
	}
	
	private static Graph  getGraph(SimpleJavaNode node) {
		Graph  g = null;
		SimpleJavaNode  parent = node;
		while( !(parent instanceof ASTMethodDeclaration )) {
			parent = (SimpleJavaNode)parent.jjtGetParent();
		}
		if( parent != null) {
			g = ((ASTMethodDeclaration) parent).getGraph();
		}
		return g;
	}
	


	

	public static void logc1(String str) {
		logc("createTraceInfoStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkTraceInfo(..) - " + str);
	}
	public static void logc3(String str) {
		logc("analyzeAsgnTo(..) - " + str);
	}
	public static void logc4(String str) {
		logc("existPath(..) - " + str);
	}
	public static void logc5(String str) {
		logc("checkTraceInfo(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("TraceInfoStateMachine::" + str);
		}
	}
}
