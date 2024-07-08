package softtest.dts.java;

import java.io.*;
import java.util.*;

import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConditionalAndExpression;
import softtest.ast.java.ASTConditionalExpression;
import softtest.ast.java.ASTConditionalOrExpression;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExclusiveOrExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTInclusiveOrExpression;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTUnaryExpressionNotPlusMinus;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleNode;

import softtest.ast.java.*;

class Conditions implements Comparable<Conditions> {
	public int beginline, endline, begincolumn, endcolumn;
	public String filename="";

	public int compareTo(Conditions r) {
		if (endline < r.beginline) {
			return -1;
		} else if (beginline > r.endline) {
			return 1;
		} else {
			if(endline==r.beginline&&endcolumn<r.begincolumn){
				return -1;
			}
			if(beginline==r.endline&&begincolumn>r.endcolumn){
				return 1;
			}
			return 0;
		}
	}

	public Conditions(int beginline, int endline, int begincolumn, int endcolumn,String filename) {
		this.beginline = beginline;
		this.endline = endline;
		this.begincolumn = begincolumn;
		this.endcolumn = endcolumn;
		this.filename=filename;
	}

	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Conditions)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		Conditions x = (Conditions) o;
		if (this.compareTo(x) == 0) {
			return true;
		}
		return false;
	}
}

public class GatherConditionNum extends JavaParserVisitorAdapter {
	static int GR, GE, LS, LE, EQ, NE,AND,OR,NOT,IOR,IAND,INOT,EOR,INSTANCEOF,NON;
	static String[] OPERATOR={">",">=","<","<=","==","!=","&&","||","!","|","&","~","^","instanceof","NONE",};

	static Hashtable<String, TreeSet<Conditions>> files = new Hashtable<String, TreeSet<Conditions>>();

	static String file = "";

	static int count;

	static void printConditions() {
		LineNumberReader is = null;
		String line = null;
		StringBuffer buff = new StringBuffer();
		PrintWriter os = null;
		int inall=GR+GE+LS+LE+EQ+NE+AND+OR+NOT+IOR+IAND+INOT+EOR+INSTANCEOF+NON;
		System.out.printf("%s : %d %.4f\n", OPERATOR[0],GR,(GR/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[1],GE,(GE/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[2],LS,(LS/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[3],LE,(LE/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[4],EQ,(EQ/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[5],NE,(NE/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[6],AND,(AND/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[7],OR,(OR/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[8],NOT,(NOT/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[9],IOR,(IOR/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[10],IAND,(IAND/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[11],INOT,(INOT/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[12],EOR,(EOR/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[13],INSTANCEOF,(INSTANCEOF/(double)inall));
		System.out.printf("%s : %d %.4f\n", OPERATOR[14],NON,(NON/(double)inall));
		
		System.out.println("ALL"+":"+inall);	
		try {
			os = new PrintWriter(new FileWriter("xq.txt"));
			Set<Map.Entry<String, TreeSet<Conditions>>> entryset = files
					.entrySet();
			Iterator<Map.Entry<String, TreeSet<Conditions>>> i = entryset
					.iterator();
			while (i.hasNext()) {
				Map.Entry<String, TreeSet<Conditions>> entry = i.next();
				String file = entry.getKey();
				int beginline = 0, endline = 0, begincolumn = 0, endcolumn = 0;
				String filename="";
				try {
					is = new LineNumberReader(new FileReader(file));

					for (Conditions r : entry.getValue()) {
						beginline = r.beginline;
						endline = r.endline;
						begincolumn = r.begincolumn;
						endcolumn = r.endcolumn;
						filename=r.filename;

						// 跳到起始行处
						do {
							if(is.getLineNumber() < beginline){
								line = is.readLine();
							}
						} while (line != null && is.getLineNumber() < beginline);

						// 添加起始行上的源码到buff
						if (line != null) {
							if (beginline == endline) {
								buff.append(line.substring(begincolumn - 1,
										(line.length()-1>endcolumn)?endcolumn:(line.length()-1)));
							} else {
								buff.append(line.substring(begincolumn - 1));
							}
						}

						// 如果起始行和终止行不在同一行则继续读
						while (line != null && is.getLineNumber() < endline) {
							line = is.readLine();
							buff.append("\r\n");
							buff.append(line.substring(0, (line.length()-1>endcolumn)?endcolumn:(line.length()-1)));
						}
						os.println("" + count + ":" + buff.toString());
						count++;
						buff = new StringBuffer();
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(filename+":"+beginline+":"+begincolumn);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

	/**
	 * 根据参数读取源码信息，文件路径，起始行号，起始列号，终止行号，终止列号（都是从1开始）。 如果要读的行上，列号不合法将抛出越界异常
	 */
	static String getSouceCode(String path, int beginline, int begincolumn,
			int endline, int endcolumn) {
		LineNumberReader os = null;
		String line = null;
		StringBuffer buff = new StringBuffer();
		// 判断参数合法性
		if ((beginline > endline)
				|| (beginline == endline && begincolumn > endcolumn)) {
			return "";
		}
		try {
			os = new LineNumberReader(new FileReader(path));
			// 跳到起始行处
			do {
				line = os.readLine();
			} while (line != null && os.getLineNumber() < beginline);

			// 添加起始行上的源码到buff
			if (line != null) {
				if (beginline == endline) {
					buff.append(line.substring(begincolumn - 1, endcolumn));
				} else {
					buff.append(line.substring(begincolumn - 1));
				}
			}

			// 如果起始行和终止行不在同一行则继续读
			while (line != null && os.getLineNumber() < endline) {
				line = os.readLine();
				buff.append("\r\n");
				if (os.getLineNumber() == endline) {
					buff.append(line.substring(0, endcolumn));
				}else{
					buff.append(line);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buff.toString();
	}
	
	private void CalNums(SimpleNode node){
		//{">",">=","<","<=","==","!=","&&","||","!","|","&","~","^","instanceof","NONE",};
		// GR, GE, LS, LE, EQ, NE,AND,OR,NOT,IOR,IAND,INOT,EOR,INSTANCEOF,NON;
		if(node instanceof ASTExpression){
			node=(SimpleNode)node.jjtGetChild(0);
		}
		if (node instanceof ASTRelationalExpression) {
			if (node.getImage().equals(">")) {
				GR++;
			} else if (node.getImage().equals(">=")) {
				GE++;
			} else if (node.getImage().equals("<")) {
				LS++;
			} else if (node.getImage().equals("<=")) {
				LE++;
			}
		} else if (node instanceof ASTEqualityExpression) {
			if (node.getImage().equals("==")) {
				EQ++;
			} else if (node.getImage().equals("!=")) {
				NE++;
			}
		}else if (node instanceof ASTConditionalAndExpression) {
			AND++;
		}else if (node instanceof ASTConditionalOrExpression) {
			OR++;
		}else if (node instanceof ASTUnaryExpressionNotPlusMinus) {
			if (node.getImage().equals("!")) {
				NOT++;
			} else if (node.getImage().equals("~")) {
				INOT++;
			}
		}else if (node instanceof ASTInclusiveOrExpression) {
			IOR++;
		}else if (node instanceof ASTAndExpression) {
			IAND++;
		}else if (node instanceof ASTExclusiveOrExpression) {
			EOR++;
		}else if (node instanceof ASTInstanceOfExpression) {
			INSTANCEOF++;
		}else {
			NON++;
		}
		
	}

	@Override
	public Object visit(ASTIfStatement node, Object data) {
		ASTExpression expr=(ASTExpression)node.jjtGetChild(0);
		CalNums(expr);
		
		TreeSet<Conditions> set = files.get(file);
		Conditions r = new Conditions(expr.getBeginLine(), expr.getEndLine(),
				expr.getBeginColumn(), expr.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}		
		return null;
	}
	
	@Override
	public Object visit(ASTWhileStatement node, Object data) {
		ASTExpression expr=(ASTExpression)node.jjtGetChild(0);
		CalNums(expr);
		
		TreeSet<Conditions> set = files.get(file);
		Conditions r = new Conditions(expr.getBeginLine(), expr.getEndLine(),
				expr.getBeginColumn(), expr.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}		
		return null;
	}	

	@Override
	public Object visit(ASTDoStatement node, Object data) {
		ASTExpression expr=(ASTExpression)node.jjtGetChild(1);
		CalNums(expr);
		
		TreeSet<Conditions> set = files.get(file);
		Conditions r = new Conditions(expr.getBeginLine(), expr.getEndLine(),
				expr.getBeginColumn(), expr.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}		
		return null;
	}
	
	@Override
	public Object visit(ASTForStatement node, Object data) {
		ASTExpression expr=(ASTExpression)node.getFirstDirectChildOfType(ASTExpression.class);
		if(expr==null){
			return null;
		}
		CalNums(expr);
		
		TreeSet<Conditions> set = files.get(file);
		Conditions r = new Conditions(expr.getBeginLine(), expr.getEndLine(),
				expr.getBeginColumn(), expr.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}		
		return null;
	}		
	
	@Override
	public Object visit(ASTConditionalExpression node, Object data) {
		if(!node.isTernary()){
			return null;
		}
		SimpleNode expr=(SimpleNode)node.jjtGetChild(0);
		CalNums(expr);
		
		TreeSet<Conditions> set = files.get(file);
		Conditions r = new Conditions(expr.getBeginLine(), expr.getEndLine(),
				expr.getBeginColumn(), expr.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}		
		return null;
	}
	

	static String FTYPE = ".java";

	public boolean chksubdir = true;

	private List<File> fsl = new LinkedList<File>();

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("用法：java GatherConditionNum 待处理目录或文件");
			return;
		}
		GatherConditionNum test = new GatherConditionNum();
		test.fsl.clear();
		File f = new File(args[0]);
		if (f.isDirectory()) {
			test.check(f);
		} else if (f.isFile()) {
			test.fsl.add(f);
		}

		test.analysis();
		GatherConditionNum.printConditions();
	}

	private void check(File path) {
		File[] ffs = path.listFiles();
		for (int i = 0; i < ffs.length; i++) {
			if (ffs[i].getName().endsWith(FTYPE)) {
				fsl.add(ffs[i]);
			} else if (chksubdir && ffs[i].isDirectory()) {
				check(ffs[i]);
			}
		}
	}

	static int linenumber = 0;

	private int fileAnalysis(String parsefilename) throws IOException {
		file=parsefilename;
		JavaParser parser = new JavaParser(new JavaCharStream(
				new FileInputStream(parsefilename)));
		parser.setJDK15();
		if(files.get(parsefilename)==null){
			TreeSet<Conditions> set = new TreeSet<Conditions>();
			files.put(parsefilename, set);
		}
		ASTCompilationUnit astroot = parser.CompilationUnit();
		astroot.jjtAccept(this, null);
		return astroot.getEndLine();
	}

	private void analysis() {
		Iterator<File> i = fsl.iterator();
		while (i.hasNext()) {
			File f = i.next();
			try {
				linenumber += fileAnalysis(f.getPath());
				System.out.println("分析"+f.getPath());
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println(f.getPath() + "碰到一个分析错误！");
			}

		}
		System.out.println("总共文件数：" + fsl.size());
		System.out.println("总共行数：" + linenumber);
	}
}
