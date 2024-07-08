package softtest.callgraph.java.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class ReleaseFeature extends AbstractFeature {
	private HashSet<VariableNameDeclaration> vars = new HashSet<VariableNameDeclaration>();
	//判断函数资源释放特征是否有条件
	private boolean isCondition=false;
	
	@Override
	public void listen(SimpleJavaNode node, FeatureSet set) {
		SimpleJavaNode treenode = node;
		ReleaseFeatureVisitor visitor = new ReleaseFeatureVisitor();
		treenode.jjtAccept(visitor, null);
		if (table.size() > 0) {
			set.addFeature(this);
		}
		vars.clear();
		vars = null;
	}
	
	// 释放函数为close的可能资源
	private static String[] RES_STRINGS1 = { "FileOutputStream", "FilterOutputStream", "PipedOutputStream", "ObjectOutputStream", "BufferedOutputStream",
			"CheckedOutputStream", "CipherOutputStream", "DataOutputStream", "DeflaterOutputStream", "DigestOutputStream", "PrintStream", "ZipOutputStream",
			"GZIPOutputStream", "BufferedWriter", "FilterWriter", "OutputStreamWriter", "FileWriter", "PipedWriter", "PrintWriter", "AudioInputStream",
			"FilterInputStream", "BufferedInputStream", "CheckedInputStream", "CipherInputStream", "DataInputStream", "DigestInputStream",
			"InflaterInputStream", "GZIPInputStream", "ZipInputStream", "LineNumberInputStream", "ProgressMonitorInputStream", "PushbackInputStream",
			"ObjectInputStream", "FileInputStream", "PipedInputStream", "SequenceInputStream", "BufferedReader", "LineNumberReader", "FilterReader",
			"PushbackReader", "InputStreamReader", "FileReader", "PipedReader", "RandomAccessFile", "ZipFile", "JNLPRandomAccessFile", "Connection",
			"ResultSet", "Statement", "PooledConnection", "MidiDevice", "Receiver", "Transmitter", "AudioInputStream", "Line", "DatagramSocketImpl",
			"ServerSocket", "Socket", "SocketImpl", "JMXConnector", "RMIConnection", "RMIConnectionImpl", "RMIConnectionImpl_Stub", "RMIConnector",
			"RMIServerImpl", "ConsoleHandler", "FileHandler", "Handler", "MemoryHandler", "SocketHandler", "StreamHandler", "Scanner", "StartTlsResponse",
			"PreparedStatement", "CallableStatement", "Closeable","ImageInputStream","InitialLdapContext","LdapContext","DatagramChannel","InputStream",
			"InitialDirContext","DirContext","ReadableByteChannel","OutputStream","MessageBox","DirectoryDialog"};
	private static HashSet closeResource = new HashSet<String>(Arrays.asList(RES_STRINGS1));

	// 释放函数为dispose的资源
	private static String RES_STRINGS2[] = { "StreamPrintService", "CompositeContext", "Graphics", "InputContext", "InputMethod", "PaintContext", "Window",
			"DebugGraphics", "JInternalFrame", "ImageReader", "ImageWrite", "SaslClient", "SaslServer", "GSSContext", "GSSCredential",
	/* "Color","Font","Cursor","GC","Display","Image","Printer","Region", */
	};
	private static HashSet disposeResource = new HashSet<String>(Arrays.asList(RES_STRINGS2));

	// 释放函数为disconnect的资源
	private static String RES_STRINGS3[] = { "HttpURLConnection",
	 "URLConnection","HttpsURLConnection",/*"JarURLConnection", */
	};
	private static HashSet disconnectResource = new HashSet<String>(Arrays.asList(RES_STRINGS3));
	
	
		
	/**
	 * 1. this.close(x,y)
	 * 2. t.close(x,y);
	 * 3. close(x,y);
	 * 
	 * 调用者保证pe的子节点满足xpath: ./PrimarySuffix/Arguments
	 * 
	 * 返回值的调用的函数具有资源释放特征
	 * 
	 * 1. 查函数摘要，是否为资源释放函数 2. 查分配资源方法表，是否为资源释放函数
	 * 
	 * @param pe
	 *            ASTPrimaryExpression
	 * @return
	 */
	private boolean checkPrimaryExpression(ASTPrimaryExpression pe) {
		if (pe == null || pe.jjtGetNumChildren()<2) {
			return false;
		}
		
		//ASTPrimaryPrefix pp = (ASTPrimaryPrefix)pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		ASTPrimarySuffix ps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
		ASTArguments args = (ASTArguments) ps.jjtGetChild(0);		
		
		// 获得当前函数的形参列表		
		ASTMethodDeclaration md = (ASTMethodDeclaration)pe.getFirstParentOfType(ASTMethodDeclaration.class);		
		if (md == null) {
			return false;
		}
		
		Hashtable<NameDeclaration, Integer> params = new Hashtable<NameDeclaration, Integer>();
		List varIds = null;
		List varDecls = new ArrayList<NameDeclaration>();
		try {
			varIds = md.findChildNodesWithXPath("./MethodDeclarator/FormalParameters/FormalParameter/VariableDeclaratorId");
			int cnt = 0;
			for (Object o : varIds) {
				ASTVariableDeclaratorId vdi = (ASTVariableDeclaratorId) o;
				params.put(vdi.getNameDeclaration(), cnt);
				varDecls.add(vdi.getNameDeclaration());
				++cnt;
			}
		} catch (JaxenException e) {
			throw new RuntimeException("xpath error @ ReleaseFeature.java : checkPrimaryExpression(checkPrimaryExpression node)",e);
		}
		
//		// 当前函数没有形参，故不可能释放形参资源
//		if (varIds.size() <= 0) {
//			return false;
//		}
		
		// 把函数传递的实参与本函数的形参对应起来
		List realargs = null;
		List argsid = new ArrayList<Integer>();
		// flag 表示ArgumentList的实参至少有一个与当前控制流图节点所在函数的形参对应
		//boolean flag = false;
		NameDeclaration ndA=null;
		ASTName nn=null;
		try {			
			realargs = pe.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression[./PrimaryExpression/PrimaryPrefix/Name]");			
			for (Object o : realargs) {
				ASTExpression expr = (ASTExpression) o;
				List name = expr.findChildNodesWithXPath("./PrimaryExpression[count(PrimarySuffix)=0]/PrimaryPrefix/Name");
				List suffix = expr.findChildNodesWithXPath("./PrimaryExpression[count(PrimarySuffix)=1 and count(PrimaryPrefix)=1 and ./PrimaryPrefix[count(*)=0]]/PrimarySuffix[count(*)=0]");
				NameDeclaration nd = null;
				
				if (name.size() == 1) {
					nn = (ASTName) name.get(0);
					nd = nn.getNameDeclaration();
				} else if (suffix.size() == 1) {
					ASTPrimarySuffix ps1 = (ASTPrimarySuffix) suffix.get(0);
					nd = ps1.getNameDeclaration();					
				}
				if (nd != null && params.containsKey(nd)) {
					//flag = true;
					argsid.add(params.get(nd));
				} else {
					argsid.add(-1);
					ndA=nd;
				}
			}
		} catch (JaxenException e) {
			throw new RuntimeException("xpath error @ ReleaseFeature.java : checkPrimaryExpression(checkPrimaryExpression node)",e);
		}
		
		// 该函数调用没有一个实参与当前函数形参对应，故不可能释放当前函数形参指向资源
		//if (!flag) {
		//	return false;
		//}
		
		// 获取type
		Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		Object type = null;
		if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
			type = ((ExpressionBase)pr).getType();
		} else {
			return false;
		}
		
		// 1. 查函数摘要，是否为释放资源函数
		if (type != null && (type instanceof Method)) {
			MethodNode methodnode=MethodNode.findMethodNode(type);
			if(methodnode != null){
				MethodSummary summary = methodnode.getMethodsummary();
				
				// 得到函数摘要，查询是否具有释放资源特征
				if (summary != null) {
					for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
						if (!(ff instanceof ReleaseFeature)) {
							continue;
						}
						
						ReleaseFeature rf = (ReleaseFeature) ff;
						
						
						for (Map.Entry<MapOfVariable,List<String>> e : rf.getTable().entrySet()) {
							MapOfVariable mov = e.getKey();
							List<String> dis = e.getValue();
							Integer i;
							try {
								i = (Integer)argsid.get(mov.getIndex());
							} catch (Exception exception) {
								return false;
							}
							
							// to be implemented
//							if (!argsid.get(mov.getIndex()).equals(-1) && !vars.contains(varDecls.get(i))) {
								List<String> newlist = new ArrayList<String>(dis);
								if(softtest.config.java.Config.LANGUAGE==0){
									newlist.add("文件:"+ProjectAnalysis.current_file+" 行:"+pe.getBeginLine()+"\n");
								}else{
									newlist.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
								}
								if (rf.isCondition()) {
									this.setCondition(true);
								}
								if (i!=-1) {
									vars.add((VariableNameDeclaration) varDecls.get(i));
									table.put(new MapOfVariable((VariableNameDeclaration)varDecls.get(i)), newlist);
									if (pe.getFirstParentOfType(ASTIfStatement.class)!=null) {
										ASTIfStatement ifstat=(ASTIfStatement)pe.getFirstParentOfType(ASTIfStatement.class);
										if(ifstat.getFirstChildOfType(ASTNullLiteral.class)==null){
											setCondition(true);
										}
									}
								}else {
									vars.add((VariableNameDeclaration) ndA);
									if (null!=ndA) {
										table.put(new MapOfVariable((VariableNameDeclaration)ndA), newlist);
									}
								}
								if (rf.isCondition()) {
									this.setCondition(true);
								}
								
//							}
						}
					}
					return false;
				}
			}
		}
		
		// 2. 查释放资源方法表，是否为释放资源函数
		VariableNameDeclaration vnd = null;
		int nameTag=0;//标记释放资源的语句是否在条件语句内
		int psTag=0;
		String image = null;
		if ( (pr instanceof ASTPrimaryPrefix) && ((ASTPrimaryPrefix)pr).jjtGetNumChildren()==1 && (((ASTPrimaryPrefix)pr).jjtGetChild(0) instanceof ASTName) ) {
			ASTName name = (ASTName)((ASTPrimaryPrefix)pr).jjtGetChild(0);
			if (name.getFirstParentOfType(ASTIfStatement.class)!=null) {
				ASTIfStatement ifstat=(ASTIfStatement)name.getFirstParentOfType(ASTIfStatement.class);
				if(ifstat.getFirstChildOfType(ASTNullLiteral.class)==null){
					nameTag++;
				}
			}
			image = name.getImage();
			if (name.getNameDeclaration() instanceof VariableNameDeclaration) {
				vnd = (VariableNameDeclaration) name.getNameDeclaration();
			}
		}
		else if ( (pr instanceof ASTPrimarySuffix) && (((ASTPrimarySuffix)pr).jjtGetNumChildren() == 0)) {
			ASTPrimarySuffix ps0 = (ASTPrimarySuffix)pr;
			if (ps0.getFirstParentOfType(ASTIfStatement.class)!=null) {
				ASTIfStatement ifstat=(ASTIfStatement)ps0.getFirstParentOfType(ASTIfStatement.class);
				if(ifstat.getFirstChildOfType(ASTNullLiteral.class)==null){
					psTag++;
				}
			}
			image = ps0.getImage();
			if (ps0.getNameDeclaration() instanceof VariableNameDeclaration) {
				vnd = (VariableNameDeclaration) ps0.getNameDeclaration();
			}
		}
		
		if ( vnd != null && image != null && params.containsKey(vnd) && image.matches("^(.+\\.dispose|.+\\.disconnect|.+\\.close)$")) {
			if (disconnectResource.contains(vnd.getTypeImage())) {
				if (image.endsWith(".disconnect") && !vars.contains(vnd)) {
					List<String> dis = new ArrayList<String>();
					if(softtest.config.java.Config.LANGUAGE==0){
						dis.add("文件:"+ProjectAnalysis.current_file+" 行:"+pe.getBeginLine()+"\n");
					}else{
						dis.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
					}
					table.put(new MapOfVariable(vnd), dis);
					vars.add(vnd);
					if (nameTag>0||psTag>0) {
						setCondition(true);
					}
				}
			}
			else if (disposeResource.contains(vnd.getTypeImage()) && !vars.contains(vnd)) {
				if (image.endsWith(".dispose")) {
					List<String> dis = new ArrayList<String>();
					if(softtest.config.java.Config.LANGUAGE==0){
						dis.add("文件:"+ProjectAnalysis.current_file+" 行:"+pe.getBeginLine()+"\n");
					}else{
						dis.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
					}				
					table.put(new MapOfVariable(vnd), dis);
					vars.add(vnd);
					if (nameTag>0||psTag>0) {
						setCondition(true);
					}
				}
			}
			else if (closeResource.contains(vnd.getTypeImage()) && !vars.contains(vnd)) {
				if (image.endsWith(".close")) {
					List<String> dis = new ArrayList<String>();
					if(softtest.config.java.Config.LANGUAGE==0){
						dis.add("文件:"+ProjectAnalysis.current_file+" 行:"+pe.getBeginLine()+"\n");
					}else{
						dis.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
					}
					table.put(new MapOfVariable(vnd), dis);
					vars.add(vnd);
					if (nameTag>0||psTag>0) {
						setCondition(true);
					}
				}
			}else if (closeResource.contains(vnd.getTypeImage()) && vars.contains(vnd)){
				setReclosed(true);
			}
			
		}
		
		
		try {
			
			StringBuffer sb = new StringBuffer();
			sb.append("^(");
			for (String s : RES_STRINGS1) {
				sb.append("(" + s + ")|");
			}
			if (RES_STRINGS1.length > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(")$");
			String regex = sb.toString();
			
			realargs=pe.findChildNodesWithXPath("./PrimaryPrefix/Name");
			for (Object o:realargs) {
				ASTName name=(ASTName)o;
				
				NameDeclaration nd=name.getNameDeclaration();
				if (nd instanceof VariableNameDeclaration) {
					VariableNameDeclaration var=(VariableNameDeclaration)nd;
					ASTVariableDeclaratorId id=var.getDeclaratorId();
					if (id.getScope().getClass().equals(ClassScope.class)) {
						if (id.getTypeNode().getTypeImage().matches(regex)) {
								if (name.getImage().endsWith(".close")) {
									List<String> dis = new ArrayList<String>();
									if(softtest.config.java.Config.LANGUAGE==0){
										dis.add("文件:"+ProjectAnalysis.current_file+" 行:"+pe.getBeginLine()+"\n");
									}else{
										dis.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
									}
									table.put(new MapOfVariable(var), dis);
									vars.add(var);
								}
						}
						if (name.getFirstParentOfType(ASTIfStatement.class)!=null) {
							setCondition(true);
						}
					}
				}
			}
		} catch (JaxenException e) {
			throw new RuntimeException("xpath error @ ReleaseFeature.java : checkPrimaryExpression(checkPrimaryExpression node)",e);
		}
		
		
		
		
		
		
		return false;
		
		// 3. 查node.getType()，该方法是否会return资源类型
		// to be implemented. to do or not to do???				
		// return false;
	}
	
	/**
	 * 遍历控制流节点对应的语法树节点，提取释放资源特征
	 * 
	 * 目前仅考虑函数形参所指向的资源释放情况
	 * 
	 * @author younix
	 *
	 */
	private class ReleaseFeatureVisitor extends JavaParserVisitorAdapter {
		/**
		 * 通过检查当前方法所调用的其它方法，确定当前方法是否释放形参所指的资源。
		 *
		 * 1. this.close(x,y)
		 * 2. t.close(x,y);
		 * 3. close(x,y);
		 */
		public Object visit(ASTPrimaryExpression node, Object data) {
			if (node == null) {
				return null;
			}
			
			try {
				if (node.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments").size() <= 0) {
					return null;
				}
				checkPrimaryExpression(node);
			} catch (JaxenException e) {
				throw new RuntimeException("xpath error @ ReleaseFeature.java : visit(ASTPrimaryExpression node, Object data)",e);
			}
			
			return null;
		}
	}

	public boolean isCondition() {
		return isCondition;
	}

	public void setCondition(boolean isCondition) {
		this.isCondition = isCondition;
	}
	/*
	 * reclosed表示摘要中有资源重复关闭
	 */
	private boolean reclosed=false;

	public boolean isReclosed() {
		return reclosed;
	}

	public void setReclosed(boolean reclosed) {
		this.reclosed = reclosed;
	}
}
