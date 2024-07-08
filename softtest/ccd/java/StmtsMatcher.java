package softtest.ccd.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPackageDeclaration;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.ast.java.*;

public class StmtsMatcher {

	public static int P = 59;
	public static int lastMod = 1;
	public static int lastHash = 0;
	public static int minTiles = 3;

	public static StmtMatchCollector collector = new StmtMatchCollector();

	public static List<Statement> statements = new ArrayList();


	public static void jjtGenHashCode(SimpleJavaNode cur, Map group) {//, List stmts) {
		int hscode = cur.getId();
		
		/// 只将ASTBlockStatment，以及复合语句下面单个的Statement节点
		//  加入statements链表中
		if ( ! inSkipList(cur)) {
			//ASTBlockStatement bstst = (ASTBlockStatement) cur;
			Statement  stmt = new Statement(cur);
			stmt.setIndex(statements.size());
			statements.add(stmt);
		}
		
		
		// 首先计算各个子节点的 hashCode
		for (int i = 0; i < cur.jjtGetNumChildren(); i++) {
			jjtGenHashCode((SimpleJavaNode) cur.jjtGetChild(i), group);
		}
		for (int i = 0; i < cur.jjtGetNumChildren(); i++) {
			// 如果是if, while节点，则使用if下面的 expression 节点和"if"作为if
			// 节点hashCode计算素材
			// 如果是do, for节点，则需要取child(1)作为计算素材
			if(cur instanceof ASTIfStatement
			|| cur instanceof ASTWhileStatement) {
				hscode = ((SimpleJavaNode) cur.jjtGetChild(0)).getHashCode();
				break;
			} else
			if(cur instanceof ASTDoStatement || cur instanceof ASTForStatement) {
				hscode = ((SimpleJavaNode) cur.jjtGetChild(1)).getHashCode();
				break;
			}
			else {
				hscode = P * hscode	+ ((SimpleNode) cur.jjtGetChild(i)).getHashCode();
			}
		}
		setHashCode(cur, hscode);
		// 只保留BlockStatement类型的节点
		if (inSkipList(cur)) {
			return;
		}

		Object o = group.get(hscode);
		if (o == null) {
			group.put(hscode, cur);
		} else if (o instanceof SimpleJavaNode) {
			List l = new ArrayList();
			l.add(o);
			l.add(cur);
			group.put(hscode, l);
		} else {
			List l = (List) o;
			l.add(cur);
		}
	}
	/*
	public static Map  findNStatementMatches() {
		Map m = new HashMap();
		for (int i = statements.size() - 1; i >= 0; i--) {
			ASTBlockStatement bst = (ASTBlockStatement) statements.get(i);
			if (i != statements.size()-1) {
				//int last = tokenAt(min, token).getIdentifier();
				int last = ((ASTBlockStatement)statements.get(i+minTiles)).getHashCode();
				lastHash = P * lastHash + bst.getHashCode() - lastMod * last;
				bst.setKRHashcode(lastHash);
				Object o = m.get(lastHash);
				System.out.println("|:" + lastHash);
				// Note that this insertion method is worthwhile since the vast majority
				// markGroup keys will have only one value.
				if (o == null) {
					m.put(lastHash, bst);
				} else if (o instanceof ASTBlockStatement) {
					List l = new ArrayList();
					l.add(o);
					l.add(bst);
					m.put(lastHash, l);
				} else {
					List l = (List) o;
					l.add(bst);
				}
			} else {
				lastHash = 0;
				for (int end = Math.max(0, i - minTiles + 1); i > end; i--) {
					bst = (ASTBlockStatement) statements.get(i - 1);
					lastHash = P * lastHash + bst.getHashCode();
					//if (token == TokenEntry.EOF) {  break; }  ??????
				}
			}
		}
		return m;
	}*/

	public static Map findNStatementMatches2() {
		System.out.println("[ statements ]");
		for (int i = 0; i < statements.size(); i++) {
			Statement bst = statements.get(i);
			//System.out.print(" " + bst.getBeginLine());
			if (i % 10 == 9) {
				//System.out.println();
			}
		}
		System.out.println();
		
		Map m = new HashMap();
		lastHash = 0;
		Statement bst = null;
		int i = 0;
		for (; i < minTiles && i < statements.size(); i++) {
			bst = statements.get(i);
			lastHash = P * lastHash + bst.getHashCode();
		}
		//m.put(lastHash, bst);
		Statement fst = statements.get(i-minTiles>=0? i-minTiles:0);
		m.put(lastHash, fst);
		fst.setKRHashcode(lastHash);
		//System.out.println("|:" + lastHash + " " + fst.getBeginLine());////////////
		for (int j = minTiles; j < statements.size(); j++) {
			//bst = (ASTBlockStatement) statements.get(j);
			bst = statements.get(j);
			Statement lastBS = statements.get(j-minTiles);
			//int last = tokenAt(min, token).getIdentifier();
			int lastKR = lastBS.getHashCode();
			lastHash = P * lastHash + bst.getHashCode() - lastMod * lastKR;
			//bst.setKRHashcode(lastHash);
			
			//lastBS.setKRHashcode(lastHash);
			Statement sec = statements.get(j-minTiles+1);
			sec.setKRHashcode(lastHash);

			Object o = m.get(lastHash);

			//System.out.println("|:" + lastHash + "  " + sec.getBeginLine());///////////////////////////

			if (o == null) {
				//m.put(lastHash, bst);//m.put(lastHash, lastBS);
				m.put(lastHash, sec);
			} else if (o instanceof Statement) {
				List l = new ArrayList();
				l.add(o);
				//l.add(bst);//l.add(lastBS);
				l.add(sec);
				m.put(lastHash, l);
			} else {
				List l = (List) o;
				//l.add(bst);//l.add(lastBS);
				l.add(sec);
			}
		}
		return m;
	}

	/**
	 * 只对BlockStatement返回false 
	 */
	private static boolean inSkipList(SimpleJavaNode sk) {
		if (sk.jjtGetNumChildren() == 0) {
			return true;
		} else if (sk instanceof ASTImportDeclaration) {
			return true;
		} else if (sk instanceof ASTPackageDeclaration) {
			return true;
		}
		// 只对可能重复的语句进行检测
		if (sk instanceof ASTBlockStatement
		//|| sk instanceof ASTBlock
		//|| sk instanceof ASTMethodDeclaration
		//|| sk instanceof ASTClassOrInterfaceBodyDeclaration
		) {
			return false;
		}
		
		if(sk instanceof ASTStatement && sk.jjtGetNumChildren()>0
		&& ! (sk.jjtGetChild(0) instanceof ASTBlock)) {
			if(sk.jjtGetParent() instanceof ASTIfStatement
			|| sk.jjtGetParent() instanceof ASTForStatement
			|| sk.jjtGetParent() instanceof ASTWhileStatement
			|| sk.jjtGetParent() instanceof ASTDoStatement) {
				return false;
			}
		}
		return true;
	}

	//  根据节点的类型，设置
	private static void setHashCode(SimpleJavaNode sk, int code) {
		// 如果是ASTName  没有孩子的 ASTClassOrInterfaceType 
		// 等叶节点，则其Hash值又再加上其文本Hash的结果。
		String txt = null;
		if (sk instanceof ASTName) {
			txt = sk.getImage();
		} else if (sk instanceof ASTClassOrInterfaceType) {
			if (sk.jjtGetNumChildren() == 0) {
				txt = sk.getImage();
			}
		} else if (sk instanceof ASTPrimitiveType) {
			txt = sk.getImage();
		} else if (sk instanceof ASTVariableDeclaratorId) {
			txt = sk.getImage();
		} else if (sk instanceof ASTLiteral) {
			//  NullLiteral
			if (sk.jjtGetNumChildren() > 0
			&& sk.jjtGetChild(0) instanceof ASTBooleanLiteral) {
				if (((ASTBooleanLiteral) sk.jjtGetChild(0)).isTrue()) {
					txt = "true";
				} else {
					txt = "false";
				}
			} else if (sk.jjtGetNumChildren() > 0
			&& sk.jjtGetChild(0) instanceof ASTNullLiteral) {
				txt = "null";
			}
			txt = sk.getImage();
		} else if (sk instanceof ASTIfStatement) {
			txt = "if";
		} else if (sk instanceof ASTWhileStatement) {
			txt = "while";
		} else if (sk instanceof ASTDoStatement) {
			txt = "do";
		} else if (sk instanceof ASTForStatement) {
			txt = "for";
		}
		
		for (int i = 0; txt!=null && i < txt.length(); i++) {
			code = code * P + txt.charAt(i);
		}
		sk.setHashCode(code);
	}

	public static void collect(Map krMatches) {
		for (Iterator i = krMatches.values().iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof List) {
				o = reverse((List) o);
				collector.collect((List) o);
			}
			i.remove();
		}
	}

	private static List reverse(List list) {
		Object sts[] = list.toArray();
		for (int i = 0; i < sts.length; i++) {
			for (int j = 1; j < sts.length - i; j++) {
				if (((Statement) sts[j]).getIndex() < ((Statement) sts[j - 1]).getIndex()) {
					Object tmp = sts[j];
					sts[j] = sts[j - 1];
					sts[j - 1] = tmp;
				}
			}
		}
		List ret = new ArrayList();
		//System.out.println("------- my reverse --------");
		for (Object s : sts) {
			ret.add(s);
			//System.out.print(" " + ((ASTBlockStatement) s).getIndex());
		}
		System.out.println();
		return ret;
	}

	public static List getMatches() {
		List matches = collector.getMatches();
		
		for (Iterator i = matches.iterator(); i.hasNext();) {
			OneMatch match = (OneMatch) i.next();
			System.out.println(match.toString());// ///////////////////////////
			for (Iterator occurrences = match.iterator(); occurrences.hasNext();) {
				Statement mark = (Statement) occurrences.next();
				// do nothing now
			}
		}
		return matches;
	}

	public static Statement stmtAt(int offset, Statement cur) {
		return statements.get(offset + cur.getIndex());
	}
}

