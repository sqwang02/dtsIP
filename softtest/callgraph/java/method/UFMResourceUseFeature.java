package softtest.callgraph.java.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * UFMResourceUseFeature用于描述一个方法是否具有不确定资源使用的特征。
 * 
 * @author pzq
 *
 */
public class UFMResourceUseFeature extends AbstractFeature{	
	/**
	 * 描述成员方法的返回值是否为不确定资源使用
	 */
	private boolean isUFMResourceFunction = false;
	
	/**
	 * 描述函数调用不确定资源使用的trace信息
	 */
	private List<String> traceinfo = null;
	
	public List<String> getTraceInfo() {
		return traceinfo;
	}
	
	/**
	 * 描述成员方法的返回值是否为不确定资源使用
	 * @return this.isAllocateFunction
	 */
	public boolean isUMFResourceFunction() {
		return this.isUFMResourceFunction;
	}
	
	private String current_func = null;
	
	@Override
	public void listen(SimpleJavaNode node, FeatureSet set) {
		if (node instanceof ASTMethodDeclaration) {
			current_func = ((ASTMethodDeclaration) node).getMethodName();
		} else if (node instanceof ASTConstructorDeclaration) {
			current_func = ((ASTConstructorDeclaration) node).getMethodName();
		}
		
		UFMResourceFeatureVisitor vsitor = new UFMResourceFeatureVisitor();
		node.jjtAccept(vsitor, null);
		if (isUFMResourceFunction) {
			set.addFeature(this);
		}
	}
	
	// 分配资源的方法
	private static String RES_STRINGS4[] = { "getConnection",
			"createStatement", "executeQuery", "getResultSet",
			"prepareStatement", "prepareCall", "accept","open",
			"openStream","getChannel" };
	private static String regex4 = null;
	
	// 生成RES_STRINGS4对应的正则表达式
	static {
		StringBuffer sb = new StringBuffer();
		sb.append("^(");
		for (String s : RES_STRINGS4) {
			sb.append("(.+\\." + s + ")|");
		}
		if (RES_STRINGS4.length > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")$");
		regex4 = sb.toString();
	}
	
	// 资源实例
	private static String RES_STRINGS[] = { "FileOutputStream",
			"PipedOutputStream", "FileWriter", "PipedWriter",
			"FileInputStream", "PipedInputStream", "FileReader",
			"RandomAccessFile", "ZipOutputStream", "GZIPOutputStream",
			"PrintWriter", "AudioInputStream", "GZIPInputStream",
			"ZipInputStream", "FileInputStream", "PipedInputStream",
			"PipedReader", "ZipFile", "JNLPRandomAccessFile", "Connection",
			"ResultSet", "Statement", "PooledConnection", "MidiDevice",
			"Receiver", "Transmitter", "DatagramSocketImpl", "ServerSocket",
			"Socket", "SocketImpl", "JMXConnector", "RMIConnection",
			"RMIConnectionImpl", "RMIConnectionImpl_Stub", "RMIConnector",
			"RMIServerImpl", "ConsoleHandler", "FileHandler", "Handler",
			"MemoryHandler", "SocketHandler", "StreamHandler", "Scanner",
			"StartTlsResponse", "PreparedStatement","ImageInputStream",
			"InitialLdapContext","LdapContext","DatagramChannel","URL", 
			"InputStream","InitialDirContext","DirContext","ReadableByteChannel","OutputStream","DirectoryDialog","MessageBox"};
	private static HashSet res_strings = new HashSet<String>(Arrays.asList(RES_STRINGS));
	
	
	
	
	
	/**
	 * 检查表达式是否不确定资源使用
	 *
	 * @param expr ASTExpression
	 * @return 是否不确定资源使用
	 */
	private boolean checkStatementExpression(ASTStatementExpression state) {
		if (state == null) {
			return false;
		}
		
		VexNode vexNode=state.getCurrentVexNode();
		if(null==vexNode){
			return false;
		}
		ArrayList<NameOccurrence> nameoccList=vexNode.getOccurrences();
		
		Iterator<NameOccurrence>iter=nameoccList.iterator();
		while(iter.hasNext()){
			NameOccurrence nameocc=iter.next();
			NameDeclaration nd=nameocc.getDeclaration();
			if (nd instanceof VariableNameDeclaration) {
				VariableNameDeclaration varnd=(VariableNameDeclaration)nd;
				String typeImage=varnd.getTypeImage();
				StringBuffer sb = new StringBuffer();
				sb.append("^(");
				for (String s : RES_STRINGS) {
					sb.append("(" + s + ")|");
				}
				if (RES_STRINGS.length > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append(")$");
				String reg = sb.toString();
				if (typeImage.matches(reg)) {
					
					
					List<NameOccurrence> udlist=nameocc.getUseDefList();
					if (null==udlist||udlist.size()==0) {
						List<String> newlist = new ArrayList<String>();
						if(softtest.config.java.Config.LANGUAGE==0){
							newlist.add("文件:"+ProjectAnalysis.getCurrent_file()+" 行:"+state.getBeginLine()+" 方法:"+current_func);
						}else{
							newlist.add("file:"+ProjectAnalysis.getCurrent_file()+" line:"+state.getBeginLine()+" Method:"+current_func);
						}
						table.put(new MapOfVariable(varnd), newlist);
						traceinfo = newlist;
						return true;
					}
				}
			}			
		}
		
		return false;
	}
	
	/**
	 * 检查ASTStatementExpression是否具有释放资源特征
	 * @param pe
	 * @return
	 */
	private boolean checkStatementExpressionForRelease(ASTStatementExpression se) {
		return false;
	}
	
	
	
	/**
	 * 对函数特征信息进行迭代。
	 * 需要考虑的情况有以下几种：
	 */
	private class UFMResourceFeatureVisitor extends JavaParserVisitorAdapter{
		@Override
		public Object visit(ASTStatementExpression node, Object data) {
			if (node == null) {
				return null;
			}
			
			isUFMResourceFunction = checkStatementExpression(node);
			return null;
		}
		
	}

}
