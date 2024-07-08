package softtest.dts.java;


import java.io.*;
import java.util.*;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleNode;

import softtest.ast.java.*;

class Relations implements Comparable<Relations> {
	public int beginline, endline, begincolumn, endcolumn;
	public String filename="";

	public int compareTo(Relations r) {
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

	public Relations(int beginline, int endline, int begincolumn, int endcolumn,String filename) {
		this.beginline = beginline;
		this.endline = endline;
		this.begincolumn = begincolumn;
		this.endcolumn = endcolumn;
		this.filename=filename;
	}

	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Relations)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		Relations x = (Relations) o;
		if (this.compareTo(x) == 0) {
			return true;
		}
		return false;
	}
}

public class GatherRelationNum extends JavaParserVisitorAdapter {
	static int GR, GE, LS, LE, EQ, NE,CONST;

	static Hashtable<String, TreeSet<Relations>> files = new Hashtable<String, TreeSet<Relations>>();

	static String file = "";

	static int count;

	static void printRelations() {
		LineNumberReader is = null;
		String line = null;
		StringBuffer buff = new StringBuffer();
		PrintWriter os = null;
		System.out.println("GR:"+GR);
		System.out.println("GE:"+GE);
		System.out.println("LS:"+LS);
		System.out.println("LE:"+LE);
		System.out.println("EQ:"+EQ);
		System.out.println("NE:"+NE);
		System.out.println("CONST:"+CONST);
		System.out.println("All:"+(GR+GE+LS+LE+EQ+NE));
		try {
			os = new PrintWriter(new FileWriter("xq.txt"));
			Set<Map.Entry<String, TreeSet<Relations>>> entryset = files
					.entrySet();
			Iterator<Map.Entry<String, TreeSet<Relations>>> i = entryset
					.iterator();
			while (i.hasNext()) {
				Map.Entry<String, TreeSet<Relations>> entry = i.next();
				String file = entry.getKey();
				int beginline = 0, endline = 0, begincolumn = 0, endcolumn = 0;
				String filename="";
				try {
					is = new LineNumberReader(new FileReader(file));

					for (Relations r : entry.getValue()) {
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
							buff.append("\n");
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
				buff.append("\n");
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

	@Override
	public Object visit(ASTRelationalExpression node, Object data) {
		// < > <= >=

		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException(
					"This is not a legal RelationalExpression");
		}
		String[] operators = image.split("#");
		String op = operators[0];

		if (op.equals(">")) {
			GR++;
		} else if (op.equals(">=")) {
			GE++;
		} else if (op.equals("<")) {
			LS++;
		} else if (op.equals("<=")) {
			LE++;
		}
		
		if(((SimpleNode)node.jjtGetChild(0)).getSingleChildofType(ASTLiteral.class)!=null){
			CONST++;
		}else if(((SimpleNode)node.jjtGetChild(1)).getSingleChildofType(ASTLiteral.class)!=null){
			CONST++;
		}
		
		TreeSet<Relations> set = files.get(file);
		Relations r = new Relations(node.getBeginLine(), node.getEndLine(),
				node.getBeginColumn(), node.getEndColumn(),file);
		if (set.contains(r)) {
			System.out.println("recursive relational expression: "+file+" "+node.getBeginLine()+":"+node.getBeginColumn());
		} else {
			set.add(r);
		}
		return null;
	}

	@Override
	public Object visit(ASTEqualityExpression node, Object data) {
		// == ！=

		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}

		if (operators.length != 1) {
			return null;
		}
		String op = operators[0];

		if (op.equals("==")) {
			EQ++;
		} else if (op.equals("!=")) {
			NE++;
		}
		
		if(((SimpleNode)node.jjtGetChild(0)).getSingleChildofType(ASTLiteral.class)!=null){
			CONST++;
		}else if(((SimpleNode)node.jjtGetChild(1)).getSingleChildofType(ASTLiteral.class)!=null){
			CONST++;
		}		
		
		TreeSet<Relations> set = files.get(file);
		Relations r = new Relations(node.getBeginLine(), node.getEndLine(),
				node.getBeginColumn(), node.getEndColumn(),file);
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
			System.out.println("用法：java GatherRelationNum 待处理目录或文件");
			return;
		}
		GatherRelationNum test = new GatherRelationNum();
		test.fsl.clear();
		File f = new File(args[0]);
		if (f.isDirectory()) {
			test.check(f);
		} else if (f.isFile()) {
			test.fsl.add(f);
		}

		test.analysis();
		GatherRelationNum.printRelations();
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
			TreeSet<Relations> set = new TreeSet<Relations>();
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
