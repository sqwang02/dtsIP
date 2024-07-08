package softtest.ccd.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;

import softtest.ast.java.*;
import softtest.symboltable.java.*;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPackageDeclaration;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTShiftExpression;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.symboltable.java.NameOccurrence;

 class StmtsSliceMatcher4 {

	public static int P = 59;
	public static int lastMod = 1;
	public static int lastHash = 0;
	public static int minTiles = 5;
	public static int deep = 1;// 如果为 1, 则 A + B * C中只有进入B/C分析时才会取B/C的运算符作为哈希值来源,即为1时，会考虑最高层的两个运算符
	public static int curDeep = 0; // 记录当前深入表达式的深度
	public static boolean doExtend = true; // 控制是否进行拓展匹配的开关

	//public static StmtMatchCollector2 collector = new StmtMatchCollector2();
	public static StmtMatchCollector3 collector = new StmtMatchCollector3();

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
			// 如果是需要参数化匹配的表达式节点，则记录当前所在表达式的深度
			if( isParamedExpression(cur) ) {
				curDeep ++;
			}
			jjtGenHashCode((SimpleJavaNode) cur.jjtGetChild(i), group);
			if( isParamedExpression(cur) ) {
				curDeep --;
			}
		}
		for (int i = 0; i < cur.jjtGetNumChildren(); i++) {
			// 如果是if, while节点，则使用if/while下面的 expression 节点和 "if"/"while"
			// 作为if/while 节点 hashCode 计算素材
			// 如果是 do 节点，则需要取child(1)作为计算素材
			// 如果是 for 节点，则需要取child(1)作为计算素材
			if(cur instanceof ASTIfStatement
			|| cur instanceof ASTWhileStatement) {
				hscode = ((SimpleJavaNode) cur.jjtGetChild(0)).getHashCode();
				break;
			} else
			if(cur instanceof ASTDoStatement ) {
				hscode = ((SimpleJavaNode) cur.jjtGetChild(1)).getHashCode();
				break;
			} else
			if( cur instanceof ASTForStatement) {
				// for 可能有 0 - 4 个子节点
				if( cur.jjtGetNumChildren() > 1 && cur.jjtGetChild(1) instanceof ASTExpression) {
					hscode = ((SimpleJavaNode) cur.jjtGetChild(1)).getHashCode();
				} else {
					hscode = 0;
				}
				break;
			}
			// 实现赋值运算的参数化匹配，左边变量名将被忽略
			else if(cur instanceof ASTStatementExpression && cur.jjtGetNumChildren() == 3 && cur.jjtGetChild(1) instanceof ASTAssignmentOperator) {
				hscode = ((SimpleJavaNode) cur.jjtGetChild(2)).getHashCode();
				break;
			}
			else if(cur instanceof ASTAdditiveExpression || cur instanceof ASTMultiplicativeExpression) {//curDeep
				// 实现定深参数化忽略
				if( curDeep > deep ) {
					hscode = genHashCodeForOperator(cur.getImage());//超出一定深度的表达式只使用其操作符作为哈希运算值来源
					break;
				} else {
					hscode = P * hscode + ((SimpleNode) cur.jjtGetChild(i)).getHashCode();
				}
			}
			else if(cur instanceof ASTMethodDeclaration) {
				hscode = cur.getId();
			}
			else if(cur instanceof ASTFieldDeclaration) { // 只取类型作为哈希来源
				hscode = ((SimpleJavaNode) cur.jjtGetChild(0)).getHashCode();
			}
			else if(cur instanceof ASTLocalVariableDeclaration) {// 只取类型作为哈希来源
				hscode = ((SimpleJavaNode) cur.jjtGetChild(0)).getHashCode();
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
	
	public static Map findNMatchedStatements() {
		System.out.println("[ statements ]");
		
		Map m = new HashMap();
		if(statements.size() <= 1) {
			return m;
		}
		
		lastHash = 0;
		Statement bst = null;
		int i = 0;
		// 计算初始KR哈希值
		for (; i < minTiles && i < statements.size(); i++) {
			bst = statements.get(i);
			lastHash = P * lastHash + bst.getHashCode();
		}
		Statement fst = statements.get(i-minTiles>=0? i-minTiles:0);
		m.put(lastHash, fst);
		fst.setKRHashcode(lastHash);
		
		// 对每个语句计算其KR哈希值
		for (int j = minTiles; j < statements.size(); j++) {
			bst = statements.get(j);
			Statement lastBS = statements.get(j-minTiles);
			
			int lastKR = lastBS.getHashCode();
			lastHash = P * lastHash + bst.getHashCode() - lastMod * lastKR;
			
			Statement sec = statements.get(j-minTiles+1);
			sec.setKRHashcode(lastHash);

			Object o = m.get(lastHash);

			//log("|:" + lastHash + "  " + sec.getBeginLine());///////////////////////////

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
	 * 只对BlockStatement, 除switch的复合语句，MethodDeclaration 
	 * FieldDeclaration, MethodDeclaration
	 * 返回false 
	 */
	private static boolean inSkipList(SimpleJavaNode sk) {
		if (sk.jjtGetNumChildren() == 0) {
			return true;
		} else if (sk instanceof ASTImportDeclaration) {
			return true;
		} else if (sk instanceof ASTPackageDeclaration) {
			return true;
		}
		else
		if (sk instanceof ASTBlockStatement
		//|| sk instanceof ASTBlock || sk instanceof ASTMethodDeclaration || sk instanceof ASTClassOrInterfaceBodyDeclaration
		) {
			return false;
		}
		else
		if(sk instanceof ASTStatement && sk.jjtGetNumChildren()>0
		&& ! (sk.jjtGetChild(0) instanceof ASTBlock)) {
			if(sk.jjtGetParent() instanceof ASTIfStatement
			|| sk.jjtGetParent() instanceof ASTForStatement
			|| sk.jjtGetParent() instanceof ASTWhileStatement
			|| sk.jjtGetParent() instanceof ASTDoStatement) {
				return false;
			}
		}
		else
		if (sk instanceof ASTMethodDeclaration || sk instanceof ASTFieldDeclaration) {
			return false;
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

	// 
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
		
		for (Object s : sts) {
			ret.add(s);
			//log(" " + ((ASTBlockStatement) s).getIndex());
		}
		//log("");
		return ret;
	}

	public static List getMatches() {
		List matches = collector.getMatches();
		List rmv     = new LinkedList();
		List adds = new LinkedList(); 
		
		if( ! doExtend) { // 控制是否进行拓展匹配的开关
			return matches;
		}
		//  在原有两两相似的基础上，再进一步利用Occurences进行检查。
		
		for (Iterator i = matches.iterator(); i.hasNext();) {
			OneMatch match = (OneMatch) i.next();
			//log(match.toString());// ///////////////////////////
			for (Iterator iterMtch = match.iterator(); iterMtch.hasNext();) {
				Statement mark = (Statement) iterMtch.next();
			}
		}
		// 对于每个匹配
		for (Iterator i = matches.iterator(); i.hasNext();) {
			//log("====================== OneMatch ======================");
			
			OneMatch match = (OneMatch) i.next();
			//log(match.toString());// ///////////////////////////
			Statement stmt1 = match.getFirstMark();
			Statement stmt2 = match.getSecondMark();
			int     len = match.getStmtCount();
			int     begin1 = stmt1.getIndex();
			int     begin2 = stmt2.getIndex();
			
			// 这些变量的所有不在匹配范围内的出现所在的语句在 statements 中的索引
			Set<Integer>  idxsOfOccs1 = getOutRangeIdxsOfOccs(begin1, len);
			Set<Integer>  idxsOfOccs2 = getOutRangeIdxsOfOccs(begin2, len);
			/*
			//log("++++ begin: " + begin1 + "  end:" + (begin1+len-1));
			for(Integer it : idxsOfOccs1) {
				//log("--|--" + it + "  (" + statements.get(it).getBeginLine() + ")");
			}
			//log("++++ begin: " + begin2 + "  end:" + (begin2+len-1));
			for(Integer it : idxsOfOccs2) {
				//log("--|--" + it + "  (" + statements.get(it).getBeginLine() + ")");
			}
			*/
			// 进行拓展匹配
			ExtendMatch  extMatch = doExtendMatch(match, idxsOfOccs1, idxsOfOccs2);
			/**  如果拓展匹配比较理想，则替代原匹配，加入返回列表中  **/
			if(extMatch.getArounds1().size() > 0) {
				rmv.add(match);
				adds.add(extMatch);
			}
		}
		matches.removeAll(rmv);
		matches.addAll(adds);
		return matches;
	}

	// loc为某个变量出现对应的抽象语法树节点
	// 返回该节点对应的 ASTBlockStatement 或者 ASTStatement（if/while/do/for语句下面的单语句）
	// 若返回-1,则表明这是个错误
	public static int getIndexInStatements(SimpleJavaNode  loc) {
		SimpleJavaNode parent = (SimpleJavaNode)loc.jjtGetParent();
		while(parent != null) {
			if (parent instanceof ASTBlockStatement) {
				return parent.getStmt().getIndex();
			}
			else
			if (parent instanceof ASTStatement) {
				if(parent.jjtGetParent() instanceof ASTIfStatement
				|| parent.jjtGetParent() instanceof ASTDoStatement
				|| parent.jjtGetParent() instanceof ASTWhileStatement
				|| parent.jjtGetParent() instanceof ASTForStatement) {
					return parent.getStmt().getIndex();
				}
			}
			parent = (SimpleJavaNode) parent.jjtGetParent();
		}
		return -1;
	}
	
	/** 由于 node 对应的抽象语法树节点为 ASTBlockStatement 或 ASTStatement, 
	 * 而ASTBlockStatement没有关联的控制流节点，只有通过其子节点查找控制流节点
	 * ASTBlockStatement.child[0].child[0]
	 * ASTStatement.child[0]
	 * **/
	private static List<VexNode> getVexNodeList(SimpleJavaNode  node) {
		if(node instanceof ASTBlockStatement) {
			return ((SimpleJavaNode)node.jjtGetChild(0).jjtGetChild(0)).getVexNode();
		} else 
		if(node instanceof ASTStatement) {
			return ((SimpleJavaNode)node.jjtGetChild(0)).getVexNode();
		}
		return null;
	}
	/**
	 * 获取在语句串statements中匹配部分[begin, begin+len-1]中所有出现相关联的
	 * 在范围[begin, begin+len-1]之外的语句的索引（在statements中的索引） 
	 */
	private static Set<Integer> getOutRangeIdxsOfOccs(int begin, int len) {
		Statement stmt1 = statements.get(begin);
		Set<Integer>        locOfOccs = new HashSet<Integer>();
		for(int idx = 0; idx < len; idx++) {
			// 当前的匹配语句所包含变量的所有出现
			List<NameOccurrence> allOccs = new LinkedList<NameOccurrence>();
			stmt1 = statements.get(begin + idx);
			SimpleJavaNode astNode1 = stmt1.astNode;
			//log("AST: " + astNode1 + " (" + astNode1.getBeginLine() + ") ");//  AST2:" + astNode2);
			
			List<VexNode> vexList = getVexNodeList(astNode1);//astNode1.getCurrentVexList();
			if(vexList != null) {
				for(VexNode v : vexList) {
					allOccs.addAll(v.getOccurrences());
				}
			} else {
				//log("List<VexNode> is null");
			}
			
			// 对于匹配语句
			for (NameOccurrence occ : allOccs) {
				//log("occ of Occs: " + occ);
				SimpleJavaNode loc = (SimpleJavaNode)occ.getLocation();
				// 使用出现
				if(occ.getOccurrenceType() == NameOccurrence.OccurrenceType.USE) {
					List<NameOccurrence> defOccs = occ.getUseDefList();
					for(NameOccurrence  defOcc : defOccs) {
						SimpleJavaNode locOfdef = (SimpleJavaNode)defOcc.getLocation();
						// 获得该使用出现对应的定义出现所属语句在statements中的位置
						int defIdxOfStmts = getIndexInStatements(locOfdef);
						if (defIdxOfStmts < begin && defIdxOfStmts != -1) {
							//log("0==add:" + locOfdef.printNode());
							locOfOccs.add(defIdxOfStmts);
						}
					}
				}
				// 定义出现
				else {
					// 定义-取消定义链
					List<NameOccurrence> defUndefOccs = occ.getDefUndefList();
					for(NameOccurrence  duOcc : defUndefOccs) {
						SimpleJavaNode locOfdef = (SimpleJavaNode)duOcc.getLocation();
						// 获得取消当前定义的定义出现所属语句在statements中的位置,若在范围之外，则加入
						int defIdxOfStmts = getIndexInStatements(locOfdef);
						if (defIdxOfStmts >= begin + len) {
							//log("1==add:" + locOfdef);
							locOfOccs.add(defIdxOfStmts);
						}
					}
					// 定义-使用链
					List<NameOccurrence> defUseOccs = occ.getDefUseList();
					for(NameOccurrence  duOcc : defUseOccs) {
						SimpleJavaNode locOfdef = (SimpleJavaNode)duOcc.getLocation();
						// 获得该定义出现后面的使用出现所属语句在statements中的位置
						int defIdxOfStmts = getIndexInStatements(locOfdef);
						if (defIdxOfStmts >= begin + len) {
							//log("2==add:" + locOfdef);
							locOfOccs.add(defIdxOfStmts);
						}
					}
					// 取消定义-定义链
					List<NameOccurrence> udefdefOccs = occ.getUndefDefList();
					for(NameOccurrence  duOcc : udefdefOccs) {
						SimpleJavaNode locOfdef = (SimpleJavaNode)duOcc.getLocation();
						// 获得当前定义所取消的定义出现所属语句在statements中的位置
						int defIdxOfStmts = getIndexInStatements(locOfdef);
						if (defIdxOfStmts < begin && defIdxOfStmts != -1) {
							//log("3==add:" + locOfdef);
							locOfOccs.add(defIdxOfStmts);
						}
					}
				}
			}// for(NameOccurrence:Occs)
		}// for(int idx = 0; idx < len; idx++)
		return locOfOccs;
	}

	/** 以匹配双方的前后关联的语句的前、后端为边界寻找新的匹配点
	 * idxsOfOccs1/2 是不在match范围内的语句的索引集合
	 */
	public static ExtendMatch doExtendMatch(OneMatch match, Set<Integer> idxsOfOccs1, Set<Integer> idxsOfOccs2) {
		ExtendMatch ematch = new ExtendMatch(match);
		List<Integer> idxs1 = new ArrayList<Integer>(idxsOfOccs1.size());
		List<Integer> idxs2 = new ArrayList<Integer>(idxsOfOccs2.size());
		idxs1.addAll(idxsOfOccs1);
		idxs2.addAll(idxsOfOccs2);
		Collections.sort(idxs1);
		Collections.sort(idxs2);
		
		/** 对处于最前、最后语句之间的这段语句按照哈希值排序，找出两部分中具有
		 *  相同哈希值的语句。 排序使用抽象语法树节点内的哈希值 */
		/** stmts1/2的Statement已经排序  **/
		List<Statement>  stmts1 = getSortedStatements(idxs1, match.getFirstMark().getIndex(), match.getStmtCount());//new ArrayList<Statement>(idxsOfOccs1.size());
		List<Statement>  stmts2 = getSortedStatements(idxs2, match.getSecondMark().getIndex(), match.getStmtCount());
		
		/** 将匹配的点添加入ematch中 */
		fillMatchPoints(ematch, stmts1, stmts2);
		//log("******************\n" + ematch.toString());
		return ematch;
	}
	public static void fillMatchPoints(ExtendMatch ematch, List<Statement> stmts1, List<Statement> stmts2) {
		for(int i = 0, j = 0; i < stmts1.size() && j < stmts2.size(); ) {
			if(stmts1.get(i).getHashCode() < stmts2.get(j).getHashCode()) {
				i++;
			} else if(stmts1.get(i).getHashCode() > stmts2.get(j).getHashCode()) {
				j++;
			}
			// 将可能的具有多个相同的哈希值的节点加入 Arounds
			while(i < stmts1.size() && j < stmts2.size()
			&&    stmts1.get(i).getHashCode() == stmts2.get(j).getHashCode()) {
				ematch.addArounds1(stmts1.get(i).getIndex());
				ematch.addArounds2(stmts2.get(j).getIndex());
				if( i + 1 < stmts1.size()
				&&  stmts1.get(i+1).getHashCode() == stmts2.get(j).getHashCode()) {
					i++;
				}
				else
				if( j + 1 < stmts2.size()
				&&  stmts1.get(i).getHashCode() == stmts2.get(j+1).getHashCode()) {
					j++;
				} else {
					i++;
					j++;
				}
			}
		}
	}
	/**
	 * @param idxs   经过按语句串中顺序排序的语句序号列表
	 * @param begin  相关的匹配的核的开始索引
	 * @param len    相关的匹配的核的长度
	 * @return       使用语句的哈希值作为比较键值
	 */
	public static List<Statement> getSortedStatements(List<Integer> idxs, int begin, int len) {
		List<Statement>  rets = new ArrayList<Statement>();
		if(idxs.size() == 0) {
			return rets;
		}
		/**  [A, B][match.mark1.begin, match.mark1.end][C, D]  **/
		int A = idxs.get(0);
		int B = begin - 1;
		int C = begin + len;
		int D = idxs.get(idxs.size()-1);
		//log("()()" + A +" " + B +" " + C +" " + D);
		
		for(int i = A; i <= B; i++) {
			rets.add(statements.get(i));
		}
		for(int i = C; i <= D; i++) {
			rets.add(statements.get(i));
		}
		class HashCompare implements Comparator {
			public int compare(Object o1, Object o2) {
				if(!(o1 instanceof Statement)
				|| !(o2 instanceof Statement)) {
					return 0;
				}
				Statement st1 = (Statement)o1;
				Statement st2 = (Statement)o2;
				long l = new Long(st1.getHashCode()) - new Long(st2.getHashCode());
				if(l < 0)
					return -1;
				else if(l == 0)
					return 0;
				else 
					return 1;
			}
		}
		Collections.sort(rets, new HashCompare());
		for(int i = 0; i < rets.size(); i ++) {
			//log("HashCode:  " + rets.get(i).getHashCode());
		}
		return rets;
	}
	
	public static boolean isParamedExpression(SimpleJavaNode cur) {
		if(cur instanceof ASTAdditiveExpression
		|| cur instanceof ASTMultiplicativeExpression
		|| cur instanceof ASTShiftExpression
		|| cur instanceof ASTRelationalExpression) {
			return true;
		}
		else {
			return false;
		}
	}
	public static int    genHashCodeForOperator(String img) {
		if(img.length() == 1) {
			return img.charAt(0);
		}
		else {
			String ops [] = img.split("#");
			int hs = 0;
			for(int i = 0; i < ops.length; i++) {
				hs += ops[i].charAt(0);
			}
			return  hs;
		}
	}
	public static Statement stmtAt(int offset, Statement cur) {
		return statements.get(offset + cur.getIndex());
	}
	
	
	public static void logc(String str) {
		logc("StmtsSliceMatcher::" + str);
	}
	
	public static void log(String str) {
		if( Config.DEBUG ) {
			System.out.println(str);
		}
	}
}


