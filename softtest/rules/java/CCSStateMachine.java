package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTClassOrInterfaceBody;
import softtest.ast.java.ASTClassOrInterfaceBodyDeclaration;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExclusiveOrExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTFormalParameters;
import softtest.ast.java.ASTInclusiveOrExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPostfixExpression;
import softtest.ast.java.ASTPreDecrementExpression;
import softtest.ast.java.ASTPreIncrementExpression;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTShiftExpression;
import softtest.ast.java.ASTUnaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**  �� ������ �������ʽ���ҡ���ģ��
��������������                                                   procIndent
    ���������еĴ���û����Ը��������еĴ�������һ����λ��
    ������λ��һ�£����������е��������е������٣�
    ������λ̫��������λ����8���ַ�����
    ������λ̫С��������λС�������ַ�����
�����Ͽո��ʹ�ù���                                           procWhiteSpace
    Ӧ������ո�ĵط�û�пո������֮�䣬���������֮��ȣ�
    ��Ӧ�ô��ڿո�ĵط������˿ո�������м䣬�����м䣻
    �ո�̫�࣬�����֮��ʹ�����ĸ����ϵĿո�
���������ŵ�ʹ��ԭ��                                           procBracket  undone
    �ϳ��ı��ʽӦ����ʹ��������ȷ���������ȼ���
    �������ŵĳ����ʽ��������Ч��ʹ�����Ŷ��ǲ�����ġ�
�����ϵ���ֻ���õ���������                                procStatementLine
    ���з�������������ϣ����¿ɶ����½���
�����Ϲ��������з��ù���                                  procStatementTooLong
    ����������һ���ڣ����¿ɶ����½���
�����Ͽ��зָ���������                                    procIntervalLine
    ��������֮�䡢�������ܵĴ����֮�䡢�Լ����ڷ�װ����
    ͬ�ĺ��������֮��û�в�����У����¿ɶ����½���
����������������������ع���                             do not process here
    ���г�Ա��������Ա�����Ķ���û�а�public, protected, private��˳����������
    ��������ȫ���ǹ����򱣻��ģ�
    ����ȫ��˽�г�Ա���⽫�����಻�ܱ�ʹ�á�


2008-3-29
 */

public class CCSStateMachine {
	
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
	
	public static List<FSMMachineInstance> createCCSStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List evalRlts = new ArrayList();
		// ////////////          ��������������       //////////////
		procIndent(node, fsm, list);

		//  /////////        �����Ͽո��ʹ�ù���     /////////////////
		procWhiteSpace(node, fsm, list);
		
		//  ////////    �����ϵ���ֻ���õ���������  /////////////////
		procStatementLine(node, fsm, list);
		
		//  ////////    �����Ϲ��������з��ù���    /////////////////
		procStatementTooLong(node, fsm, list);
		
		//  //////////      �����Ͽ��зָ���������      //////////////
		procIntervalLine(node, fsm, list);
		
		//  //////////      ���������ŵ�ʹ��ԭ��         //////////////
		procBracket(node, fsm, list);
		return list;
	}
	
	
	public static boolean checkCCS(VexNode vex,FSMMachineInstance fsmInst) {
		
		return true;
	}
	
	
	private static String  getClassName(SimpleJavaNode cur) {
		SimpleJavaNode  parent = cur;
		while( parent != null && !(parent instanceof ASTClassOrInterfaceDeclaration) ) {
			parent = (SimpleJavaNode)parent.jjtGetParent();
		}
		if(parent == null) {
			throw  new RuntimeException(cur + " has no class contained");
		}
		return ((ASTClassOrInterfaceDeclaration)parent).getImage();
	}
	
	/** Block���ӽڵ��Ƿ�ӵ��һ�µ���������һ�µĽ�����nts�С�
	 * ������ȡÿ��ĵ�һ��������������������ids�У����в�ͬ������������ids��
	 * ��С��������
	*/
	private static void  isConsistentIndent(SimpleJavaNode root, Set<SimpleJavaNode> nts, Map<Integer, Integer> ids) {
		SimpleJavaNode  child = null;
		int last = 0;
		if(root instanceof ASTBlock && root.getBeginLine()!=root.getEndLine() 
		|| root instanceof ASTClassOrInterfaceBody) {
			boolean  lastAdded = false;
			for(int i = 1; i < root.jjtGetNumChildren(); i++) {
				SimpleJavaNode lastNode = (SimpleJavaNode)root.jjtGetChild(i-1);
				SimpleJavaNode curNode = (SimpleJavaNode)root.jjtGetChild(i);
				if(lastNode.getBeginColumn() != curNode.getBeginColumn()
				&& lastNode.getBeginLine() != curNode.getBeginLine()) {
					if( ! lastAdded) {
						nts.add(curNode);
						lastAdded = true;
					} else {
						lastAdded = false;
					}
				} else {
					lastAdded = false;
				}
				/*
				if(i == 0) {
					SimpleJavaNode cur = (SimpleJavaNode)root.jjtGetChild(i);
					last = cur.getBeginColumn();
					if(cur.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTForStatement
					|| cur.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTIfStatement
					|| cur.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTDoStatement
					|| cur.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTWhileStatement
					|| cur.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTTypeDeclaration) {
						int pcol = ((SimpleJavaNode)cur.jjtGetParent().jjtGetParent().jjtGetParent()).getBeginColumn();
						int ident = cur.getBeginColumn() - pcol;
						Integer cnt = ids.get(ident);
						if(cnt == null) {
							cnt = new Integer(1);
						}
						ids.put(ident, cnt);
						if(ident == 0) {
							//logc(" "+cur.getBeginLine());
						}
					}else
					if(cur.jjtGetParent().jjtGetParent() instanceof ASTMethodDeclaration) {
						**  get the ClassOrInterfaceDeclaration, not MethodDeclaration  **
						int pcol = ((SimpleJavaNode)cur.jjtGetParent().jjtGetParent().jjtGetParent()).getBeginColumn();
						int ident = cur.getBeginColumn() - pcol;
						Integer cnt = ids.get(ident);
						if(cnt == null) {
							cnt = new Integer(1);
						}
						ids.put(ident, cnt);
						if(ident == 0) {
							logc(""+cur.getBeginLine());
						}
					}
					
				}
				
				int curColm = ((SimpleJavaNode)root.jjtGetChild(i)).getBeginColumn();
				** ���ò�һ�µ�������Ԫ��ӽ�nts�� *
				if(i > 0 && last != curColm) {
					nts.add((SimpleJavaNode)root.jjtGetChild(i));
				}
				*/
				
			}
		}
		for(int i = 0; i < root.jjtGetNumChildren(); i++) {
			isConsistentIndent((SimpleJavaNode)root.jjtGetChild(i), nts, ids);
		}
	}
	
	//  if the indent is consistent this result is meaningful
	private static int  getIndent(int [] columns) {
		int i = columns.length-1;
		while(i>1 && columns[--i]==0)  ;
		int j = i;
		while(j>1 && columns[--j]==0)  ;
		if( i != j ) {
			return  i - j;
		}
		return 0;
	}
	
	//  ���ʹ�õ�����������������
	private static int  getMostOfIndent(int [] columns) {
		int diffs [] = new int[33];
		for(int i = columns.length-1; i > 1; i--) {
			if( columns[i] == 0 ) {
				continue;
			}
			int j = i-1;
			while(j > 0 && columns[--j] == 0)  ;
			if(j > 0) {
				if(i - j < 33) {
					diffs[i-j] += columns[i];
				}
			}
		}
		int  most  = diffs.length - 1;
		for(int j = most - 1; j > 0; --j) {
			if( diffs[j] > diffs[most] ) {
				most = j;
			}
		}
		logc3("number of most of the lines with the same indent is " + most);
		return most;
	}
	
	/*private static int  getMostOfIndent(SimpleJavaNode root) {
		int most = 0;
		int idents [] = new int[33];
		getIndentsCount(root,idents);
		return most;
	}*/
	
	private static int  getNearestIndent(Set<Integer> ids) {
		if(ids.size() == 0) {
			return 4;
		}
		int ident = 0;
		int  ary[] = new int[ids.size()];
		int i = 0;
		for(Iterator<Integer> it = ids.iterator(); it.hasNext(); i++) {
			ary[i] = it.next();
		}
		int dist = ary[0];
		for(i = 1; i < ary.length; i++) {
			
		}
		return  ident;
	}
	
	// ����ϲ�������Ŀ�ʼ��
	private static int  getParentBeginColumn(SimpleJavaNode  bs) {
		SimpleJavaNode  parent = (SimpleJavaNode)bs.jjtGetParent();
		while( parent != null ) {
			if( parent instanceof ASTBlockStatement ) {
				break;
			} else 
			if(parent instanceof ASTClassOrInterfaceBodyDeclaration) {
				break;
			}
			parent = (SimpleJavaNode) parent.jjtGetParent();
		}
		if( parent != null ) {
			return  parent.getBeginColumn();
		}
		return 0;
	}
	
	//	 ////////////          ��������������       //////////////
	//  ���������еĴ���û����Ը��������еĴ�������һ����λ��
	//  ������λ��һ�£����������е��������е������٣�
	//  ������λ̫��������λ����8���ַ�����
	//  ������λ̫С��������λС�������ַ����� 
	private static void  procIndent(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
		
		String xclsStr = ".//BlockStatement";
		//List evalRlts = getTreeNode(whole, xclsStr);
		Iterator iter;
		
		/*
		if(! isConsistentIndent(beginColumns)) {
			beConsistentIndent = false;
			newFSM(fsm, fsms, "Indent is not consistent", whole);
		}*/
		Set<SimpleJavaNode> nts = new HashSet<SimpleJavaNode>();
		Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
		isConsistentIndent(whole, nts, ids); 
		for(Iterator<SimpleJavaNode> ints = nts.iterator(); ints.hasNext(); ) {
			newFSM(fsm, fsms, "Indent is not consistent", ints.next());
		}
		
		int indent = 0;
		for(Iterator<Integer> it = ids.keySet().iterator(); it.hasNext(); ) {
			logc1("::ident:" + it.next());
		}
		// ����һ��
		if( nts.size() == 0 && ids.size() == 1 ) {
			indent = ids.keySet().iterator().next();
			logc1("define the consistent indent is " + indent + "  " + ids.size());
		} else {
			/**  ������һ��ʱ��ֱ�ӷ��أ������к�������  **/
			return ;
		}
		//  �������Ϊ0,��ֱ�ӷ���
		if( indent == 0 ) {
			logc1("indent is 0, do not process more");
			return  ;
		}
		//  ������λ̫С��������λС�������ַ���
		if( indent < 2 ) {
			newFSM(fsm, fsms, "Indent is too small(<2): " + indent, whole);
		}
		//  ������λ̫��������λ����8���ַ���
		else if( indent > 8 ) {
			newFSM(fsm, fsms, "Indent is too large(>8): " + indent, whole);
		}
		
		//  ��������Ĵ���û������������������һ�£�һ����λ��
		//iter = evalRlts.iterator();
		/*while (iter.hasNext()) {
			ASTBlockStatement astBS = (ASTBlockStatement) iter.next();
			int pcolm = getParentBeginColumn( astBS );
			// no indent
			if( astBS.getBeginColumn() == pcolm ) {
				newFSM(fsm, fsms, "Subblock has no indent : " + astBS, astBS);
			} else 
			if( astBS.getBeginColumn() != pcolm + indent ) {
				newFSM(fsm, fsms, "Indent of subblock is not one unit: " + astBS, astBS);
			}
		}*/
	}
	
	//  /////////        �����Ͽո��ʹ�ù���     /////////////////
    //  Ӧ������ո�ĵط�û�пո������֮�䣬���������֮��ȣ�
    //  ��Ӧ�ô��ڿո�ĵط������˿ո�������м䣬�����м䣻    ?????????
    //  �ո�̫�࣬�����֮��ʹ�����ĸ����ϵĿո�
	private static void  procWhiteSpace(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
		String xArgs = ".//Arguments[@ArgumentCount > '1' ]";
		List eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			int cnt = ((ASTArguments)eval.get(i)).getArgumentCount();
			int lstBegColmn = -1, lstBegLine = -1;
			int lstEndColmn = -1, lstEndLine = -1;
			for(int j = 0; j < cnt; j++) {
				ASTExpression astExpr = (ASTExpression)((SimpleJavaNode)eval.get(i)).jjtGetChild(0).jjtGetChild(j);
				/**  ����������֮�䲻���пո�  **/
				if(j == 0) {
					if(astExpr.getBeginLine() == ((SimpleJavaNode)eval.get(i)).getBeginLine()) {
						if(astExpr.getBeginColumn() - 2 >= ((SimpleJavaNode)eval.get(i)).getBeginColumn()) {
							newFSM(fsm, fsms, "There should be no white space between '(' and arg-0", astExpr);
						}
					}
				}
				if(j == cnt - 1 && cnt > 1) {
					if(astExpr.getEndLine() == ((SimpleJavaNode)eval.get(i)).getEndLine()) {
						if(astExpr.getEndColumn() <= ((SimpleJavaNode)eval.get(i)).getEndColumn() - 2) {
							newFSM(fsm, fsms, "There should be no white space between arg-"+j+" and ')'", astExpr);
						}
					}
				}
				
				if(j == 0) {
					lstBegColmn = astExpr.getBeginColumn();
					lstBegLine  = astExpr.getBeginLine();
					lstEndColmn = astExpr.getEndColumn();
					lstEndLine  = astExpr.getEndLine();
					continue;
				}
				int begColmn = astExpr.getBeginColumn(), begLine = astExpr.getBeginLine();
				int endColmn = astExpr.getEndColumn(), endLine = astExpr.getEndLine();
				// no white space between arguments
				if(lstEndLine == begLine && lstEndColmn >= begColmn - 2) {
					newFSM(fsm, fsms, "There should be one white space between arg-"+(j-1)+" and arg-"+j, astExpr);
				} else 
				// too much white space between arguments
				if(lstEndLine == begLine && lstEndColmn + 6 < begColmn) {
					newFSM(fsm, fsms, "There is too much white space between arg-"+(j-1)+" and arg-"+j, astExpr);
				}
				lstBegColmn = begColmn;
				lstBegLine  = begLine;
				lstEndColmn = endColmn;
				lstEndLine  = endLine;
			}
		}
		String formalArgs = ".//FormalParameters[ ./FormalParameter ]";
		eval = getTreeNode(whole, formalArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTFormalParameters astFps = (ASTFormalParameters)eval.get(i);
			int cnt = astFps.jjtGetNumChildren();
			int lstBegColmn = -1, lstBegLine = -1;
			int lstEndColmn = -1, lstEndLine = -1;
			for(int j = 0; j < cnt; j++) {
				/**  ��ʽ����������(,)֮�䲻Ӧ���ڿո�  **/
				ASTFormalParameter astFp = (ASTFormalParameter)astFps.jjtGetChild(j);
				if(j == 0) {
					if(astFps.getBeginLine() == astFp.getBeginLine()) {
						if(astFp.getBeginColumn() -2 >= astFps.getBeginColumn()){
							newFSM(fsm, fsms, "There should be no white space between '(' and farg-0", astFp);
						}
					}
				}
				if(j == cnt - 1 && cnt > 1) {
					if(astFps.getEndLine() == astFp.getEndLine()) {
						if(astFp.getEndColumn() <= astFps.getEndColumn() - 2) {
							newFSM(fsm, fsms, "There should be no white space between farg-"+j+" and ')'", astFp);
						}
					}
				}
				if(j == 0) {
					lstBegColmn = astFp.getBeginColumn();
					lstBegLine  = astFp.getBeginLine();
					lstEndColmn = astFp.getEndColumn();
					lstEndLine  = astFp.getEndLine();
					continue;
				}
				int begColmn = astFp.getBeginColumn(), begLine = astFp.getBeginLine();
				int endColmn = astFp.getEndColumn(), endLine = astFp.getEndLine();
				// no white space between arguments
				if(lstEndLine == begLine && lstEndColmn >= begColmn - 2) {
					logc("--" + lstEndColmn + "   " + begColmn);
					newFSM(fsm, fsms, "There should be one white space between farg-"+(j-1)+" and farg-"+j, astFp);
				} else 
				// too much white space between arguments
				if(lstEndLine == begLine && lstEndColmn + 6 < begColmn) {
					newFSM(fsm, fsms, "There is too much white space between farg-"+(j-1)+" and farg-"+j, astFp);
				}
				lstBegColmn = begColmn;
				lstBegLine  = begLine;
				lstEndColmn = endColmn;
				lstEndLine  = endLine;
			}
		}
	}
	
	//   ///////   �����ϵ���ֻ���õ���������     /////////
	//   ���з�������������ϣ����¿ɶ����½���
	private static void  procStatementLine(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
		String xArgs = ".//BlockStatement";
		List eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			int cnt = ((SimpleJavaNode)eval.get(i)).jjtGetParent().jjtGetNumChildren();
			int curline = ((SimpleJavaNode)eval.get(i)).getBeginLine();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode parent = (SimpleJavaNode)((SimpleJavaNode)eval.get(i)).jjtGetParent();
				if( parent.jjtGetChild(j) instanceof ASTBlockStatement ) {
					if( eval.get(i) == parent.jjtGetChild(j) ) {
						break;
					}
					if(curline == ((SimpleJavaNode)parent.jjtGetChild(j)).getEndLine() ) {
						newFSM(fsm, fsms, "One line only one statement", (SimpleJavaNode)parent.jjtGetChild(j));
					}
				} else {
					logc4("?????????  unexpected type :" + parent.jjtGetChild(j));
				}
			}
		}
	}
	
	//  ////////////   �����Ϲ��������з��ù���  ////////////// 
	//  ����������һ���ڣ����¿ɶ����½���
	private static void  procStatementTooLong(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
		String xArgs = ".//BlockStatement";
		List eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			SimpleJavaNode astBS = (SimpleJavaNode)eval.get(i);
			if( detectLongStatement(astBS) ) {
				newFSM(fsm, fsms, "Statement is too lang", astBS);
			}
		}
	}
	
	
	//  /////////////      �����Ͽ��зָ���������      //////////////
    //  ��������֮�䡢�������ܵĴ����֮�䡢�Լ����ڷ�װ����
    //  ͬ�ĺ��������֮��û�в�����У����¿ɶ����½���
    private static void  procIntervalLine(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
    	String  xArgs = ".//ClassOrInterfaceBodyDeclaration";
    	List eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			SimpleJavaNode astCB = (SimpleJavaNode)eval.get(i);
			SimpleJavaNode parent = (SimpleJavaNode)astCB.jjtGetParent();
			int cnt = parent.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode sibling = (SimpleJavaNode)parent.jjtGetChild(j);
				if( astCB == sibling ) {
					break;
				}
				int endline = sibling.getEndLine();
				if( endline + 1 >= astCB.getBeginLine() ) {
					newFSM(fsm, fsms, "There should be one interal line between methods or inner class declaration", astCB);
				}
			}
		}
    }
    
    //  ////////////////      ���������ŵ�ʹ��ԭ��      ////////////////
    //  �ϳ��ı��ʽӦ����ʹ��������ȷ���������ȼ���
    //  �������ŵĳ����ʽ��������Ч��ʹ�����Ŷ��ǲ�����ġ�
    // -> ��� ��λ���� ����������ֱ���Ǳ������ȼ��ߵı��ʽ����Ҫ����ʽ��������
    // -> ��� λ����   ����������ֱ���Ǳ������ȼ��ߵı��ʽ����Ҫ����ʽ��������
    private static void  procBracket(SimpleJavaNode whole, FSMMachine fsm, List fsms) {
    	
    	//		<< >> >>>
    	String  xArgs = ".//ShiftExpression";
    	List eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTShiftExpression astShift = (ASTShiftExpression)eval.get(i);
			int cnt = astShift.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode childs = (SimpleJavaNode)astShift.jjtGetChild(j);
				//  + -
				if(childs instanceof ASTAdditiveExpression) {
					newFSM(fsm, fsms, "May be shift operation on pos:[" + childs.getBeginLine()+","+childs.getBeginColumn()+"]", astShift);
				}
				//  * % /
				else if(childs instanceof ASTMultiplicativeExpression) {
					newFSM(fsm, fsms, "May be shift operation on pos:[" + childs.getBeginLine()+","+childs.getBeginColumn()+"]", astShift);
				}
			}
		}
		
		//		&
		xArgs = ".//AndExpression";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTAndExpression astAnd = (ASTAndExpression)eval.get(i);
			int cnt = astAnd.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode child = (SimpleJavaNode)astAnd.jjtGetChild(j);
				//  * % /
				if(child instanceof ASTMultiplicativeExpression) {
					newFSM(fsm, fsms, "May be And operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astAnd);
				}
				//  + -
				else if(child instanceof ASTAdditiveExpression) {
					newFSM(fsm, fsms, "May be And operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astAnd);
				}		
				//  << >> >>>
				else if(child instanceof ASTShiftExpression) {
					newFSM(fsm, fsms, "May be And operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astAnd);
				}
				//  == !=
				else if(child instanceof ASTEqualityExpression) {
					newFSM(fsm, fsms, "May be And operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astAnd);
				}
			}
		}
		
		//	 	^
		xArgs = ".//ExclusiveOrExpression";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTExclusiveOrExpression astExclu = (ASTExclusiveOrExpression)eval.get(i);
			int cnt = astExclu.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode child = (SimpleJavaNode)astExclu.jjtGetChild(j);
				//  * % /
				if(child instanceof ASTMultiplicativeExpression) {
					newFSM(fsm, fsms, "May be ExclusiveOr operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astExclu);
				}
				//  + -
				else if(child instanceof ASTAdditiveExpression) {
					newFSM(fsm, fsms, "May be ExclusiveOr operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astExclu);
				}		
				//  << >> >>>
				else if(child instanceof ASTShiftExpression) {
					newFSM(fsm, fsms, "May be ExclusiveOr operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astExclu);
				}
				//  == !=
				else if(child instanceof ASTEqualityExpression) {
					newFSM(fsm, fsms, "May be ExclusiveOr operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astExclu);
				}
				//  &
				else if(child instanceof ASTAndExpression) {
					newFSM(fsm, fsms, "May be ExclusiveOr operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astExclu);
				}
			}
		}
		
		//     |
		xArgs = ".//InclusiveOrExpression";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTInclusiveOrExpression astInclusiveOr = (ASTInclusiveOrExpression)eval.get(i);
			int cnt = astInclusiveOr.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode child = (SimpleJavaNode)astInclusiveOr.jjtGetChild(j);
				//  * % /
				if(child instanceof ASTMultiplicativeExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}
				//  + -
				else if(child instanceof ASTAdditiveExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}		
				//  << >> >>>
				else if(child instanceof ASTShiftExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}
				//  == !=
				else if(child instanceof ASTEqualityExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}
				//  &
				else if(child instanceof ASTAndExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}
				//  ^
				else if(child instanceof ASTExclusiveOrExpression) {
					newFSM(fsm, fsms, "May be Or operation on pos:[" + child.getBeginLine()+","+child.getBeginColumn()+"]", astInclusiveOr);
				}
			}
		}
		
		//  �� % �� / �� * ����ʱ��Ӧ�ö� % ��������
		xArgs = ".//MultiplicativeExpression[ matches(@Image, '%') and matches(@Image, '\\*')  or matches(@Image, '%') and matches(@Image, '/') ]";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTMultiplicativeExpression astMult = (ASTMultiplicativeExpression)eval.get(i);
			newFSM(fsm, fsms, "May add ( ) to % operation is better", astMult);
		}
		
		//  ������������
		//  ((...))
		xArgs = ".//PrimaryExpression[ ./PrimaryPrefix/Expression/PrimaryExpression ]";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTPrimaryExpression astPrimaryExpr = (ASTPrimaryExpression)eval.get(i);
			newFSM(fsm, fsms, "\"( )\" may be unnecessary.", astPrimaryExpr);
		}
		
		//  xx op xx op (xx op xx op xx) op xx     op is +
		xArgs = ".//AdditiveExpression";  // [@Image='+']
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTAdditiveExpression astAdd = (ASTAdditiveExpression)eval.get(i);
			String  img = astAdd.getImage();
			String  ops[] = img.split("#");
			
			int cnt = astAdd.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode child = (SimpleJavaNode)astAdd.jjtGetChild(j);
				
				//  [+|-]..  +-������һԪ��������ʽ����������
				if(child instanceof ASTUnaryExpression) {
					int  pluscnt = 0;
					int  minuscnt = 0;
					while( child instanceof ASTUnaryExpression ) {
						if( child.getImage().equals("-") ) {
							minuscnt ++;
						}
						else 
						if( child.getImage().equals("+") ) {
							pluscnt ++;
						}
						child = (SimpleJavaNode) child.jjtGetChild(0);
					}
					if( minuscnt == 0 && pluscnt > 1 ) {
						newFSM(fsm, fsms, "Too much '+' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						// ��Ϊ����+�ţ����Բ�continue,���������������ڵĴ���
					} else if( pluscnt == 0 && minuscnt > 1 ) {
						newFSM(fsm, fsms, "Too much '-' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						continue;
					} else if( pluscnt > 0 && minuscnt > 0 ) {
						newFSM(fsm, fsms, "Strange : both '-' and '+' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						continue;
					}
				}
				
				//  (...)  ����������ֶ�Ԫ����+-*/%�Լ�һԪ����++--����������ֶ���
				if( child instanceof ASTPrimaryExpression ) {
					
					ASTAdditiveExpression cadd = (ASTAdditiveExpression) child.getSingleChildofType(ASTAdditiveExpression.class);
					if( cadd != null ) {
						if( j == 0 || ops[j-1].equals("+") ) {
							newFSM(fsm, fsms, "Not necessary ( ) on + operation pos:["+child.getBeginLine()+"," + child.getBeginColumn()+"]", cadd);
						}
						continue;
					}
					ASTMultiplicativeExpression cmul = (ASTMultiplicativeExpression) child.getSingleChildofType(ASTMultiplicativeExpression.class);
					if( cmul != null ) {
						// ������ǻ����ı��ʽ������Ա���
						// ����������ӱ��ʽ���򲻱�
						boolean isAllName = true;
						for(int k = 0; k < cmul.jjtGetNumChildren(); k++) {
							if( cmul.jjtGetChild(k) instanceof ASTPrimaryExpression ) {
								if(((SimpleJavaNode)cmul.jjtGetChild(k)).getSingleChildofType(ASTName.class) == null ) {
									isAllName = false;									
								}
							}
						}
						if( isAllName ) {
							newFSM(fsm, fsms, "Not necessary ( ) on * operation pos:["+child.getBeginLine()+"," + child.getBeginColumn()+"]", cmul);
						}
						continue;
					}
					ASTPreIncrementExpression pinc = (ASTPreIncrementExpression)child.getSingleChildofType(ASTPreIncrementExpression.class);
					if( pinc != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on ++X operation pos:["+child.getBeginLine()+"," + child.getBeginColumn()+"]", pinc);
						continue;
					}
					ASTPreDecrementExpression pdec = (ASTPreDecrementExpression)child.getSingleChildofType(ASTPreDecrementExpression.class);
					if( pdec != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on --X operation pos:["+child.getBeginLine()+"," + child.getBeginColumn()+"]", pdec);
						continue;
					}
					ASTPostfixExpression pstfix = (ASTPostfixExpression)child.getSingleChildofType(ASTPostfixExpression.class);
					if( pstfix != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on X-- or X++ operation pos:["+child.getBeginLine()+"," + child.getBeginColumn()+"]", pstfix);
						continue;
					}
				}
			}
		}
		
		//	  xx op xx op (xx op xx op xx) op xx     op is *%/
		xArgs = ".//MultiplicativeExpression";
		eval = getTreeNode(whole, xArgs);
		for(int i = 0; i < eval.size(); i++) {
			ASTMultiplicativeExpression astMul = (ASTMultiplicativeExpression)eval.get(i);
			String  img = astMul.getImage();
			String  ops[] = img.split("#");
			
			int cnt = astMul.jjtGetNumChildren();
			for(int j = 0; j < cnt; j++) {
				SimpleJavaNode child = (SimpleJavaNode)astMul.jjtGetChild(j);
				
				//  [+|-]..  +-������һԪ��������ʽ����������
				if(child instanceof ASTUnaryExpression) {
					int  pluscnt = 0;
					int  minuscnt = 0;
					while( child instanceof ASTUnaryExpression ) {
						if( child.getImage().equals("-") ) {
							minuscnt ++;
						}
						else 
						if( child.getImage().equals("+") ) {
							pluscnt ++;
						}
						child = (SimpleJavaNode) child.jjtGetChild(0);
					}
					if( minuscnt == 0 && pluscnt > 1 ) {
						newFSM(fsm, fsms, "Too much '+' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						// ��Ϊ����+�ţ����Բ�continue,���������������ڵĴ���
					} else if( pluscnt == 0 && minuscnt > 1 ) {
						newFSM(fsm, fsms, "Too much '-' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						continue;
					} else if( pluscnt > 0 && minuscnt > 0 ) {
						newFSM(fsm, fsms, "Strange : both '-' and '+' ahead of pos:[" + child.getBeginLine()
								+ "," + child.getBeginColumn() + "]", child);
						continue;
					}
					
				}
				
				//  (...)   ����������ֶ�Ԫ�����*/%��һԪ�����++--������Ϊ�Ƕ���
				//  ���ڶ�Ԫ���㣬����������ǰΪ*������������ȫ����*�������Ƕ���
				if( child instanceof ASTPrimaryExpression ) {
					
					ASTMultiplicativeExpression cmul = (ASTMultiplicativeExpression) child.getSingleChildofType(ASTMultiplicativeExpression.class);
					if( cmul != null ) {
						
						if(j > 0 && ! ops[j-1].equals("*") ) {
							continue;
						}
						String mimg = cmul.getImage();
						if( mimg.contains("%") ) {
							continue;
						}
						// �����cmul���ӱ��ʽ�г����˳������֣���򵥵���Ϊ���Ƕ���
						// ����Ƶ����ʽ�����ͣ�����Ը���ȷ�ط��ֶ�������
						// ����������ȫ����ASTName���͵��ӽڵ�ű���
						int  chdcnt = cmul.jjtGetNumChildren();
						boolean  hasLiteral = false;
						for(int k = 0; k < chdcnt; k++) {
							SimpleJavaNode cchld = (SimpleJavaNode)cmul.jjtGetChild(k);
							if( cchld.getSingleChildofType(ASTLiteral.class) != null ) {
								hasLiteral = true;
								break;
							}
						}
						if( hasLiteral ) {
							continue;
						}
						newFSM(fsm, fsms, "Not necessary ( ) on * operation pos:["+cmul.getBeginLine()+"," + cmul.getBeginColumn()+"]", cmul);
						continue;
					}
					ASTPreIncrementExpression pinc = (ASTPreIncrementExpression)child.getSingleChildofType(ASTPreIncrementExpression.class);
					if( pinc != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on ++X operation pos:["+cmul.getBeginLine()+"," + cmul.getBeginColumn()+"]", cmul);
						continue;
					}
					ASTPreDecrementExpression pdec = (ASTPreDecrementExpression)child.getSingleChildofType(ASTPreDecrementExpression.class);
					if( pdec != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on --X operation pos:["+cmul.getBeginLine()+"," + cmul.getBeginColumn()+"]", cmul);
						continue;
					}
					ASTPostfixExpression pstfix = (ASTPostfixExpression)child.getSingleChildofType(ASTPostfixExpression.class);
					if( pstfix != null ) {
						newFSM(fsm, fsms, "Not necessary ( ) on X-- or X++ operation pos:["+cmul.getBeginLine()+"," + cmul.getBeginColumn()+"]", cmul);
						continue;
					}
				}
			}
		}
    }
	
	private static void newFSM(FSMMachine fsm, List fsms, String result, SimpleJavaNode ast) {
		FSMMachineInstance fsminstance = fsm.creatInstance();
		fsminstance.setRelatedObject(new FSMRelatedCalculation(ast));
		fsminstance.setResultString(result);
		fsms.add(fsminstance);
	}
	
	//  ���  ĳ�����Ӧռ���� < ���볤��/��ռ����  �򷵻�true
	private static boolean detectLongStatement(SimpleJavaNode astBS) {
		int begLine = astBS.getBeginLine();
		int endLine = astBS.getEndLine();
		int begColmn = astBS.getBeginColumn();
		int endColmn = astBS.getEndColumn();
		int len = 0;
		if(begLine != endLine) {
			len = (int) (astBS.printNode(ProjectAnalysis.getCurrent_file()).length() * 1.2); // ����ֵ
		} else {
			len = endColmn - begColmn;
		}
		//logc("beg: " + begLine + "  len:" + len + "   lines:" + (endLine - begLine + 1));
		int shouldLine = len / 80 + 1;
		if( (endLine - begLine + 1) < shouldLine ) {
			return true;
		}
		return false;
	}
	
	public static void logc1(String str) {
		logc("createCCS-SM(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkCCS(..) - " + str);
	}
	public static void logc3(String str) {
		logc("isConsistentIndent(..) - " + str);
	}
	public static void logc4(String str) {
		logc("procStatementLine(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("CCSStateMechine::" + str);
		}
	}
}
