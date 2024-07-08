package softtest.rules.java.fault.RL;

import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.rules.java.AliasSet;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

import java.lang.reflect.Method;
import java.util.*;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.method.AbstractFeature;
import softtest.callgraph.java.method.AllocateFeature;
import softtest.callgraph.java.method.AllocateFeatureListener;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.callgraph.java.method.ReleaseFeature;
import softtest.callgraph.java.method.ReleaseFeatureListener;
import softtest.callgraph.java.method.UFMResourceUseFeature;
import softtest.callgraph.java.method.UFMResourceUseFeatureListener;
import softtest.cfg.java.*;
import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.config.java.Config;

public class RLStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			if (fsmmi.getFSMMachine().getName().equals("UFM")) {
				f.format("资源在关闭之后又重新使用，从而产生悬挂指针异常，敬请您查证。");
			}else {
				f.format("资源泄漏: 在 %d 行分配的资源可能在 %d 行泄漏", beginline,errorline);
			}
		}else{
			if (fsmmi.getFSMMachine().getName().equals("UFM")) {
				f.format("UFM: resource allocated at line %d can be leaked at line %d", beginline,getBeginNode().getBeginLine());
			}else {
				f.format("Resource Leak: resource allocated at line %d can be leaked at line %d", beginline,errorline);
			}
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public void registerFeature(FeatureListenerSet listeners) {
		// TODO Auto-generated method stub
		listeners.addListener(AllocateFeatureListener.getInstance());
		listeners.addListener(ReleaseFeatureListener.getInstance());
		listeners.addListener(UFMResourceUseFeatureListener.getInstance());
	}

	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
		// TODO Auto-generated method stub		
	}

	// 释放函数为close的可能资源

	private static String[] RES_STRINGS1 = {"Channel","ByteChannel","DatagramChannel","GatheringByteChannel","FileChannel","Pipe.SinkChannel","SocketChannel","InterruptibleChannel","AbstractInterruptibleChannel","AbstractSelectableChannel","Pipe.SourceChannel","ReadableByteChannel","ScatteringByteChannel","WritableByteChannel","ServerSocketChannel",
		    "OutputStream","FileOutputStream", "FilterOutputStream","BufferedOutputStream","CheckedOutputStream","CipherOutputStream","DataOutputStream","DeflaterOutputStream","GZIPOutputStream","ZipOutputStream","JarOutputStream","DigestOutputStream","PrintStream",
		    "ObjectOutputStream", "PipedOutputStream",
		  
		    "Writer","BufferedWriter","CharArrayWriter", "FilterWriter", "OutputStreamWriter", "FileWriter", "PipedWriter", "PrintWriter", "StringWriter",
		   
		    "InputStream","AudioInputStream","FileInputStream",
			"FilterInputStream","BufferedInputStream","CheckedInputStream","CipherInputStream","DataInputStream", "DigestInputStream",
			"InflaterInputStream","GZIPInputStream","ZipInputStream","JarInputStream",
			"ObjectInput","ObjectInputStream",
			"LineNumberInputStream", "ProgressMonitorInputStream", "PushbackInputStream",
			"PipedInputStream", "SequenceInputStream",
			"ImageInputStream","FileImageInputStream","ImageInputStreamImpl","MemoryCacheImageInputStream","FileCacheImageInputStream",
			"ImageOutputStream","FileImageOutputStream","ImageOutputStreamImpl","FileCacheImageOutputStream","MemoryCacheImageOutputStream",
			
			"BufferedReader","LineNumberReader", 
			"CharArrayReader","PushbackReader","FilterReader", "InputStreamReader", "FileReader","PipedReader","StringReader",
		    
			"File","RandomAccessFile", "ZipFile","JarFile", "JNLPRandomAccessFile", 
			
			"Connection","ResultSet", "CachedRowSet","FilteredRowSet","JoinRowSet","WebRowSet","RowSet",
			"Statement", "CallableStatement","PreparedStatement",
			"PooledConnection", "XAConnection","MidiDevice","Sequencer","Synthesizer","Receiver", "Transmitter",
		    "Line", "Clip","DataLine","SourceDataLine","TargetDataLine","Mixer","Port",
		    "DatagramSocketImpl", "ServerSocket", "Socket", "SSLSocket","SocketImpl",
		    "JMXConnector", "RMIConnection", "RMIConnectionImpl", "RMIConnectionImpl_Stub", "RMIConnector",
			"RMIServerImpl", "ConsoleHandler", "FileHandler", "Handler", "MemoryHandler", "SocketHandler", "StreamHandler", "Scanner", "StartTlsResponse",
			
			"Context","InitialContext","InitialDirContext","InitialLdapContext","LdapContext",
			"EventContext","DirContext","EventDirContext"};
			
	//		"DirectoryDialog","MessageBox"};

	// 释放函数为dispose的资源
	private static String RES_STRINGS2[] = { "StreamPrintService", "CompositeContext", "Graphics", "InputContext", "InputMethod", "PaintContext", "Window",
			"DebugGraphics", "JInternalFrame", "ImageReader", "ImageWrite", "SaslClient", "SaslServer", "GSSContext", "GSSCredential",
			"Frame","Dialog",
	/* "Color","Font","Cursor","GC","Display","Image","Printer","Region", */
	};

	// 释放函数为disconnect的资源
	private static String RES_STRINGS3[] = { "HttpURLConnection",
	/* "URLConnection","HttpsURLConnection","JarURLConnection", */
	};

	// 可能分配资源的方法名
	private static String RES_STRINGS4[] = { "getConnection", "createStatement", "executeQuery", "getResultSet", "prepareStatement", "prepareCall", "accept", 
			"createImageInputStream","open","openStream","getChannel",
			"getGraphics","getResourceAsStream"};

	// 应该去除的资源
	private static String RES_STRINGS5[] = { "ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter", "StringWriter", "ByteArrayInputStream",
			"StringBufferInputStream", "StringReader", };

	// 不应该去除得资源
	private static String RES_STRINGS6[] = { "FileOutputStream", "PipedOutputStream", "FileWriter", "PipedWriter", "FileInputStream", "PipedInputStream",
			"FileReader", "PipedReader", "RandomAccessFile","Socket","ServerSocket","Window","HttpURLConnection","InitialLdapContext","URL",
			"InitialDirContext","DirContext","ReadableByteChannel","DirectoryDialog","MessageBox",
			"Dialog","Frame",};
	
	/**
	 * 所有与资源有关的类名的正则表达式
	 */
	private static String CLASSTYPE_REG_STRING = null;	
	static {
		StringBuffer buffer = new StringBuffer();
		for (String s : RES_STRINGS1) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		for (String s : RES_STRINGS2) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		for (String s : RES_STRINGS3) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS1.length + RES_STRINGS2.length + RES_STRINGS3.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		CLASSTYPE_REG_STRING = buffer.toString();
	}

	/**
	 * 所有可能分配资源的方法名的正则表达式
	 */
	private static String Method_REG_STRING = null;
	static {
		StringBuffer buffer = new StringBuffer();
		for (String s : RES_STRINGS4) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS4.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		Method_REG_STRING = buffer.toString();
	}

	/**
	 * 应该滤除的资源类型名称正则表达式
	 */
	private static String REMOVE_REG_STRING = null;
	static{
		StringBuffer buffer = new StringBuffer();
		for (String s : RES_STRINGS5) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS5.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		REMOVE_REG_STRING = buffer.toString();
	}

	/**
	 * 应该保留的资源类型名称正则表达式
	 */
	private static String PRESERVE_REG_STRING = null;
	static{
		StringBuffer buffer = new StringBuffer();
		for (String s : RES_STRINGS6) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS6.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		PRESERVE_REG_STRING = buffer.toString();
	}

	/**
	 * 检查变量声明是否分配资源
	 *
	 * @param v 需要检查的函数声明
	 * @return 变量声明是否分配资源
	 */
	private static boolean checkVariableDeclaration(VariableNameDeclaration v) {
		if (v == null || v.getTypeImage() == null) {
			return false;
		}

		String image = v.getTypeImage();
		
		// 确定分配资源的类名
		String preserve_regex = "^(" + PRESERVE_REG_STRING + ")$";
		if (image.matches(preserve_regex)) {
			/*
			 * 形如： FileOutputStream v;
			 */
			return true;
		}
		
		// 确定不分配资源的类名
		String remove_regex = "^(" + REMOVE_REG_STRING + ")$";
		if (image.matches(remove_regex)) {
			/*
			 * 形如： ByteArrayOutputStream os=new ByteArrayOutputStream();
			 * ObjectOutputStream v=new ObjectOutputStream(os);
			 */
			return false;
		}

		if (!(v.getNode() instanceof ASTVariableDeclaratorId)) {
			return false;
		}

		// 检查变量初始化
		ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) v.getNode();
		if (id.getNextSibling() instanceof ASTVariableInitializer) {
			ASTVariableInitializer vi = (ASTVariableInitializer) id.getNextSibling();
			if (vi.jjtGetNumChildren() == 1 && vi.jjtGetChild(0) instanceof ASTExpression) {
				/*
				 * 检查赋值号右侧的Allocation表达式是否分配资源
				 * A a = new TTT(......);
				 */
				if (checkExpressionNewAllocate((VariableNameDeclaration) id.getNameDeclaration(), (ASTExpression) vi.jjtGetChild(0))) {
					return true;
				}
			}
		}
		
		// 检查变量定义
		Map variableNames = v.getDeclareScope().getVariableDeclarations();
		if (variableNames == null) {
			return false;
		}
		ArrayList occs = (ArrayList) variableNames.get(v);
		if (occs == null) {
			return false;
		}
		for (Object o : occs) {
			NameOccurrence occ = (NameOccurrence) o;
			if (occ.getOccurrenceType() != NameOccurrence.OccurrenceType.DEF) {
				continue;
			}
			
			if (occ.getLocation() instanceof ASTName && occ.getLocation().jjtGetParent() instanceof ASTPrimaryPrefix
					&& occ.getLocation().jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression
					&& occ.getLocation().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetNumChildren() == 3
					&& occ.getLocation().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(2) instanceof ASTExpression) {
				/*
				 * 检查赋值号右侧的Allocation表达式是否分配资源
				 * a = new TTT(......);
				 */
				ASTExpression expr = (ASTExpression) occ.getLocation().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(2);
				if (checkExpressionNewAllocate(v, expr)) {
					return true;
				}
			}
			break;
		}
		//pzq
		/*
		 * 处理被包装的类是File的情况
		 */
		if (image.matches("File")) {
			return true;
		}
		
		
		return false;
	}
	
	/**
	 * 检查分配的实例是否是资源类的实例
	 * 
	 * @param expr
	 *            type of (ASTExpression) 包含一个AllocationExpression
	 * @return 该函数是否具有资源分配特征
	 */
	private static boolean checkExpressionNewAllocate(VariableNameDeclaration vnd, ASTExpression expr) {
		// 只考虑类名在CLASSTYPE_REG_STRING中的资源类
		final String all_resource_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^("+CLASSTYPE_REG_STRING+")$\')]";
		if (expr.findXpath(all_resource_xpath).size() != 1) {
			return false;
		}
		
		// 变量初始化时的实例属于肯定不分配资源的类
		String remove_init_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^("+REMOVE_REG_STRING+")$\')]";
		if (expr.findXpath(remove_init_xpath).size() != 0) {
			/*
			 * OutputStream os=new ByteArrayOutputStream();
			 */
			return false;
		}

		// 变量初始化时的实例属于肯定分配资源的类
		String preserve_init_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^("+PRESERVE_REG_STRING+")$\')]";
		if (expr.findXpath(preserve_init_xpath).size() != 0) {
			/*
			 * OutputStream os=new FileOutputStream("a.txt");
			 */
			return true;
		}
		
		// 滤除与要去除资源类有关的资源
		final String remove_resource_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression[1]//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^("+REMOVE_REG_STRING+")$\')]";
		if (expr.findXpath(remove_resource_xpath).size() != 0) {
			/*
			 * 形如： ObjectOutputStream v=new ObjectOutputStream(new ByteArrayOutputStream());
			 */
			return false;
		}
		
		// 与必须保留的资源类有关
		final String preserve_resource_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression[1]//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^("+PRESERVE_REG_STRING+")$\')]";
		if (expr.findXpath(preserve_resource_xpath).size() > 0) {
			/*
			 * 形如： ObjectOutputStream v=new ObjectOutputStream(new FileOutputStream("a.txt"));
			 */
			return true;
		}
		
		// 检查AllocationExpression的参数
		/*
		 * 形如： ObjectOutputStream v=new ObjectOutputStream(os);
		 * 应该继续检查变量os
		 */
		final String arg_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression[1]/PrimaryExpression[count(*)=1]/PrimaryPrefix/Name";
		{
			List temp = expr.findXpath(arg_xpath);
			if (temp.size() > 0) {
				ASTName tname = (ASTName) temp.get(0);
				if (!(tname.getNameDeclaration() instanceof VariableNameDeclaration) || vnd == tname.getNameDeclaration()) {
					return false;
				} 
				if (checkVariableDeclaration((VariableNameDeclaration) tname.getNameDeclaration())) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		// 检查AllocationExpression的参数
		/*
		 * 形如： ObjectOutputStream v=new ObjectOutputStream(os);
		 * 应该继续检查变量os
		 */
		final String arg_xpath1 = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression[1]/PrimaryExpression[count(*)=1]/PrimaryPrefix/Name";
		{
			List temp = expr.findXpath(arg_xpath);
			if (temp.size() > 0) {
				ASTName tname = (ASTName) temp.get(0);
				if (!(tname.getNameDeclaration() instanceof VariableNameDeclaration) || vnd == tname.getNameDeclaration()) {
					return false;
				} 
				if (checkVariableDeclaration((VariableNameDeclaration) tname.getNameDeclaration())) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		// 检查AllocationExpression的参数
		/*
		 * 形如： ObjectOutputStream v=new ObjectOutputStream(new TTT(...));
		 * 应该继续检查变量os
		 */
		final String all_xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType and ./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType]]]";
		{
			List temp = expr.findXpath(all_xpath);
			if (temp.size() > 0) {
				ASTAllocationExpression all = (ASTAllocationExpression) temp.get(0);
				if (checkAllocationAsAllocationParam(vnd, all)) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		// 检查AllocationExpression的参数
		/*Author:pzq
		 * 形如： PrintStream v=new PrintStream("test.txt");
		 * 
		 */
		
		final String str_args = ".//PrimaryExpression/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType[matches(@Image,\'^("+CLASSTYPE_REG_STRING+")$\')]]/Arguments/ArgumentList/Expression[1]/PrimaryExpression/PrimaryPrefix/Literal";
		{
			List temp = expr.findXpath(str_args);
			if (temp.size() > 0) {
				return true;
			}
		}
		
		// 检查AllocationExpression的参数
		/*Author:pzq
		 * 形如： ObjectOutputStream v=new ObjectOutputStream(os);
		 * 应该继续检查变量os
		 */
		final String expre_arg = "./PrimaryExpression/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType]/Arguments/ArgumentList/Expression[1]/PrimaryExpression[count(*)>1][./PrimaryPrefix/Name]";
		{
			List temp = expr.findXpath(expre_arg);
			if (temp.size() > 0) {
				ASTPrimaryExpression texpr = (ASTPrimaryExpression) temp.get(0);
				if (texpr.getTypeString().equals("class java.lang.String")) {
					return true;
				}
			}
		}
		
		
		// 更多地过滤检查？		
		return false;
	}
	
	private static List<String> allocInfo = null;
	
	/**
	 * expr包含一个函数调用，checkMethodSummayAllocate检查该函数的函数摘要，返回该函数是否具有资源分配特征。
	 *
	 * @param expr type of (ASTExpression) 包含一个函数调用
	 * @return 该函数是否具有资源分配特征
	 */
	private static boolean checkMethodSummaryAllocate(ASTExpression expr) {
		/*
		 * 检查expr子孙节点的结构图fy
		 * 
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix
		 *   ... ...(0个或多个ASTPrimarySuffix)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 * 
		 */ 
		if (expr == null || expr.jjtGetNumChildren()!=1 || expr.jjtGetChild(0).jjtGetNumChildren()<2
				|| !(expr.jjtGetChild(0) instanceof ASTPrimaryExpression)				
				|| !(expr.jjtGetChild(0).jjtGetChild(expr.jjtGetChild(0).jjtGetNumChildren()-1) instanceof ASTPrimarySuffix)) {
			return false;
		}
		
		/*
		 * pr包含方法原型，调用pr.getType()可以获取方法原型
		 *
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix             (pr)
		 *   --ASTPrimarySuffix(0个或多个)    (pr)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 */
		Object pr = expr.jjtGetChild(0).jjtGetChild(expr.jjtGetChild(0).jjtGetNumChildren()-2);		
		if ( !(pr instanceof ASTPrimaryPrefix) && !(pr instanceof ASTPrimarySuffix) ) {
			return false;
		}
		
		// 没有方法原型，无法查找方法实例
		Object type = ((ExpressionBase)pr).getType();
		if (type == null) {
			return false;
		}
		
		// 查函数摘要，是否为分配资源函数
		if (type instanceof Method) {
			MethodNode methodnode=MethodNode.findMethodNode(type);
			if(methodnode == null){
				return false;
			}
			
			MethodSummary summary = methodnode.getMethodsummary();
			if (summary == null) {
				return false;
			}
			
			// 得到函数摘要，查询是否具有分配资源特征
			for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
				if (!(ff instanceof AllocateFeature)) {
					continue;
				}
				
				if (((AllocateFeature) ff).isAllocateFunction()) {
					allocInfo = ((AllocateFeature) ff).getTraceInfo();
					return true;
				}
				
				// 确定是否具有分配资源特征，可以返回
				return ((AllocateFeature) ff).isAllocateFunction();
			}
		}
		
		return false;
	}
	
	/**
	 * expr包含一个函数调用，checkMethodInfomationAllocate检查该函数名称是否与预定义的名称相同，返回该函数是否具有资源分配特征。
	 * 
	 * @param expr type of (ASTExpression) 包含一个函数调用
	 * @return 该函数是否具有资源分配特征
	 */
	private static boolean checkMethodInfomationAllocate(ASTExpression expr) {
		/*
		 * 检查expr子孙节点的结构图
		 * 
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix
		 *   ... ...(0个或多个ASTPrimarySuffix)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 * 
		 */ 
		if (expr == null || expr.jjtGetNumChildren()!=1 || expr.jjtGetChild(0).jjtGetNumChildren()<2
				|| !(expr.jjtGetChild(0) instanceof ASTPrimaryExpression)				
				|| !(expr.jjtGetChild(0).jjtGetChild(expr.jjtGetChild(0).jjtGetNumChildren()-1) instanceof ASTPrimarySuffix)) {
			return false;
		}
		
		/*
		 * pr包含方法原型，调用pr.getType()可以获取方法原型
		 *
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix             (pr)
		 *   --ASTPrimarySuffix(0个或多个)    (pr)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 */
		Object pr = expr.jjtGetChild(0).jjtGetChild(expr.jjtGetChild(0).jjtGetNumChildren()-2);		
		if ( !(pr instanceof ASTPrimaryPrefix) && !(pr instanceof ASTPrimarySuffix) ) {
			return false;
		}
		
		// 查分配资源方法表，是否为分配资源函数
		String methodName = null;
		if ((pr instanceof ASTPrimaryPrefix) && ((ASTPrimaryPrefix)pr).jjtGetNumChildren()==1 && (((ASTPrimaryPrefix)pr).jjtGetChild(0) instanceof ASTName)) {
			methodName = ((ASTName)((ASTPrimaryPrefix)pr).jjtGetChild(0)).getImage();
		}
		else if ((pr instanceof ASTPrimarySuffix) && (((ASTPrimarySuffix)pr).jjtGetNumChildren() == 0)) {
			methodName = ((ASTPrimarySuffix)pr).getImage();
		}
		else {
			// 未知情况，默认返回false
			return false;
		}
		if (methodName == null || methodName.trim().length() == 0) {
			return false;
		}
		
		// 根据预定义的资源分配的方法名进行正则匹配。匹配不成功即认为该方法不分配资源。
		if (!methodName.matches(Method_REG_STRING)) {
			return false;
		}
		
		if (((ExpressionBase)pr).getType() instanceof Method) {
			Method m = (Method)((ExpressionBase)pr).getType();
			if (m.getReturnType().getName().matches(CLASSTYPE_REG_STRING)) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	// 三种释放方法不同的资源类名的哈希集合
	private static HashSet closeResource = new HashSet<String>(Arrays.asList(RES_STRINGS1));
	private static HashSet disposeResource = new HashSet<String>(Arrays.asList(RES_STRINGS2));
	private static HashSet disconnectResource = new HashSet<String>(Arrays.asList(RES_STRINGS3));
	
	/**
	 * 1. this.close(x,y)
	 * 2. t.close(x,y);
	 * 3. close(x,y);
	 * 
	 * 调用者保证expr的子节点满足xpath: ./PrimaryExpression[./PrimarySuffix[last()]/Arguments]
	 * 
	 * 返回值的调用的函数具有资源释放特征
	 * 
	 * 1. 查函数摘要，是否为资源释放函数 2. 查分配资源方法表，是否为资源释放函数
	 * 
	 * @param expr ASTExpression
	 * @return
	 */
	public static boolean checkPrimaryExpressionRelease(ASTPrimaryExpression pe, FSMMachineInstance fsmmi) {
		if (pe == null || pe.findXpath("./PrimarySuffix[last()]/Arguments").size() != 1) {
			return false;
		}
		
		//ASTPrimaryPrefix pp = (ASTPrimaryPrefix)pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		ASTPrimarySuffix ps = (ASTPrimarySuffix)pe.jjtGetChild(pe.jjtGetNumChildren()-1);
		
		// 获取type
		Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		Object type = null;
		if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
			type = ((ExpressionBase)pr).getType();
		} else {
			return false;
		}
		
		// 获取arguments
		List arguments = pe.findXpath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression");
		
		AliasSet alias = (AliasSet) fsmmi.getRelatedObject();
		
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
						
						if (fsmmi.getFSMMachine().getName().equals("UFM")) {
							return true;
							}
						
						// 释放参数
						for (MapOfVariable mov : rf.getTable().keySet()) {
							// to be implemented
							if (mov.getIndex()<0) {
								if (rf.isCondition()) {
									if (fsmmi.getFSMMachine().getName().equals("RL")) {
										return false;
									}/*else if (fsmmi.getFSMMachine().getName().equals("UFM")) {
										return true;
									}*/
									
								}
								return true;
							}else {
								ASTExpression ae = (ASTExpression) arguments.get(mov.getIndex());
								if (ae.findXpath("./PrimaryExpression[count(*)=1]/PrimaryPrefix/Name").size() != 1) {
									continue;
								}
								ASTName name = (ASTName)ae.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
								if (name.getNameDeclaration() instanceof VariableNameDeclaration) {
									if (alias.contains((VariableNameDeclaration)name.getNameDeclaration())) {
										if (rf.isCondition()) {
											if (fsmmi.getFSMMachine().getName().equals("RL")) {
												return false;
											}/*else if (fsmmi.getFSMMachine().getName().equals("UFM")) {
												return true;
											}*/
											
										}
										return true;
									}
								}
							}
						}
						
						// 释放类成员变量
						// to be implemented
						// ... ...
					}
					return false;
				}
			}
		}
		
		// 2. 查释放资源方法表，是否为释放资源函数
		VariableNameDeclaration vnd = null;
		String image = null;
		//added by yang :&&((ASTPrimaryPrefix)pr).jjtGetNumChildren()!=0
		if ( (pr instanceof ASTPrimaryPrefix) &&((ASTPrimaryPrefix)pr).jjtGetNumChildren()!=0 && (((ASTPrimaryPrefix)pr).jjtGetChild(0) instanceof ASTName) ) {
			ASTName name = (ASTName)((ASTPrimaryPrefix)pr).jjtGetChild(0);
			image = name.getImage();
			if (name.getNameDeclaration() instanceof VariableNameDeclaration) {
				vnd = (VariableNameDeclaration) name.getNameDeclaration();
			}
		}
		else if ( (pr instanceof ASTPrimarySuffix) && (((ASTPrimarySuffix)pr).jjtGetNumChildren() == 0)) {
			ASTPrimarySuffix ps0 = (ASTPrimarySuffix)pr;
			image = ps0.getImage();
			if (ps0.getNameDeclaration() instanceof VariableNameDeclaration) {
				vnd = (VariableNameDeclaration) ps0.getNameDeclaration();
			}
		}
		
		if ( vnd != null && alias.contains(vnd) && image != null && image.matches("^(close|dispose|disconnect|.+\\.dispose|.+\\.disconnect|.+\\.close)$")) {
			if (disconnectResource.contains(vnd.getTypeImage())) {
				if (image.endsWith("disconnect")) {
					return true;
				}
			}
			else if (disposeResource.contains(vnd.getTypeImage())) {
				if (image.endsWith("dispose")) {
					return true;
				}
			}
			else if (closeResource.contains(vnd.getTypeImage())) {
				if (image.endsWith("close")) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * 检查函数调用是否分配资源
	 * 
	 * @param expr type of (ASTExpression) 包含一个函数调用
	 * @return 该函数是否具有资源分配特征
	 */
	private static boolean checkExpressionCallAllocate(ASTExpression expr) {
		if (checkMethodSummaryAllocate(expr)) {
			return true;
		}
		
		if (checkMethodInfomationAllocate(expr)) {
			return true;
		}
		
		// 更多地过滤检查？
		return false;
	}
	
	/**
	 * 检查expr是否分配资源
	 * 
	 * @param expr type of (ASTExpression) 包含一个方法调用或实例构造
	 * @return 该函数是否具有资源分配特征
	 */
	private static boolean checkExpressionAllocate(VariableNameDeclaration vnd, ASTExpression expr) {
		final String alloc_xpath = "./PrimaryExpression[count(*)=1]/PrimaryPrefix/AllocationExpression";
		final String funcc_xpath = "./PrimaryExpression[count(*)>1]/PrimarySuffix[last()]/Arguments";
	
		
		
		if (expr.findXpath(alloc_xpath).size() == 1) {
			return checkExpressionNewAllocate(vnd, expr);
		}
		
		if (expr.findXpath(funcc_xpath).size() == 1) {
			return checkExpressionCallAllocate(expr);
		}

		
	
		
		return false;
	}
	public static void scanAllocated(SimpleJavaNode node, FSMMachine fsm) {
		
		
	}
	
	public static List<FSMMachineInstance> createRLStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		// 在句柄变量声明时，分配资源
		final String var_decl_xpath = ".//VariableDeclaratorId[../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix]";		

		List evaluationResults = node.findXpath(var_decl_xpath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			
			if (fsm.getName().equals("UFM")&&(id.getFirstParentOfType(ASTForStatement.class)!=null||id.getFirstParentOfType(ASTWhileStatement.class)!=null)) {
				continue;
			}
			if (id.hasLocalMethod(node)) {
				continue;
			}
			
			ASTExpression expr = (ASTExpression) id.getNextSibling().jjtGetChild(0);

			allocInfo = null;
			//检查是否进行了资源分配
			if (!checkExpressionAllocate((VariableNameDeclaration) id.getNameDeclaration(), expr)) {
				continue;
			}
			//创建开始状态机实例 
			FSMMachineInstance fsminstance = fsm.creatInstance();
			
			if (allocInfo != null && allocInfo.size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (String s : allocInfo) {
					sb.append(s);
					sb.append("\n");
				}
				fsminstance.setTraceinfo(sb.toString());
			}
			
			AliasSet alias = new AliasSet();
			// 不添加别名集合，在allocated处添加
			// VariableNameDeclaration v = (VariableNameDeclaration)
			// id.getNameDeclaration();
			// alias.add(v);

			fsminstance.setResultString(id.getImage());
			alias.setResouceName(id.getTypeNode().getTypeImage());
			alias.setResource(id);

			fsminstance.setRelatedObject(alias);
			
			list.add(fsminstance);
		}

		// 不在在句柄变量声明时，分配资源，表达式语句
		final String stmt_xpath = ".//PrimaryExpression[../AssignmentOperator[@Image=\'=\'] and ../Expression/PrimaryExpression/PrimaryPrefix]/PrimaryPrefix/Name";
		
		evaluationResults = node.findXpath(stmt_xpath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			
			if (fsm.getName().equals("UFM")&&(name.getFirstParentOfType(ASTForStatement.class)!=null||name.getFirstParentOfType(ASTWhileStatement.class)!=null)) {
				continue;
			}
			if (name.hasLocalMethod(node)) {
				continue;
			}
			
			if (name.getNameDeclaration() == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			
			// 检查赋值号左边的变量类型字符串
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			String image = v.getTypeImage();
			if (image == null || !image.matches(CLASSTYPE_REG_STRING)) {
				continue;
			}
			
			// 检查赋值号右边的表达式
			allocInfo = null;
			ASTExpression expr = (ASTExpression)name.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(2);
			if (!checkExpressionAllocate(v, expr)) {
				continue;
			}
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			
			if (allocInfo != null && allocInfo.size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (String s : allocInfo) {
					sb.append(s);
					sb.append("\n");
				}
				fsminstance.setTraceinfo(sb.toString());
			}
			
			AliasSet alias = new AliasSet();
			// 不添加别名集合，在allocated处添加
			// VariableNameDeclaration v = (VariableNameDeclaration)
			// id.getNameDeclaration();
			// alias.add(v);

			fsminstance.setResultString(name.getImage());
			alias.setResouceName(image);
			alias.setResource(name);

			fsminstance.setRelatedObject(alias);
			
			list.add(fsminstance);
		}	
		
		
//		//全局变量的隐性分配情况
//		final String prim_xpath= ".//PrimaryExpression";
//		
//		evaluationResults = node.findXpath(stmt_xpath);
//		i = evaluationResults.iterator();
//		while (i.hasNext()) {
//			ASTPrimaryExpression pe=(ASTPrimaryExpression)i.next();
//			if (pe.jjtGetNumChildren()<2) {
//				Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-1);
//				Object type = null;
//				if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
//					type = ((ExpressionBase)pr).getType();
//				} 
//				
//				// 1. 查函数摘要，是否为释放资源函数
//				if (type != null && (type instanceof Method)) {
//					MethodNode methodnode=MethodNode.findMethodNode(type);
//					if(methodnode != null){
//						MethodSummary summary = methodnode.getMethodsummary();
//						
//						// 得到函数摘要，查询是否具有释放资源特征
//						if (summary != null) {
//							for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
//								if (!(ff instanceof AllocateFeature)) {
//									continue;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
		return list;
	}

	public static boolean checkSameResource(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		
		while (i.hasNext()) {
			Object o = i.next();			
			if (alias.getResource() == o) {
				// 添加别名集合
				VariableNameDeclaration v = null;
				ASTExpression expr = null;
				if (o instanceof ASTVariableDeclaratorId) {
					ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) o;
					ASTVariableDeclarator vd = (ASTVariableDeclarator) id.jjtGetParent();
					if (vd.jjtGetNumChildren() == 2) {
						expr = (ASTExpression) vd.jjtGetChild(1).jjtGetChild(0);
					}
					v = (VariableNameDeclaration) id.getNameDeclaration();
					
				} else if (o instanceof ASTName) {
					ASTName name = (ASTName) o;
					ASTPrimaryExpression pe = (ASTPrimaryExpression) name.getFirstParentOfType(ASTPrimaryExpression.class);
					if (pe.jjtGetParent().jjtGetNumChildren() == 3) {
						expr = (ASTExpression) pe.jjtGetParent().jjtGetChild(2);
					}
					v = (VariableNameDeclaration) name.getNameDeclaration();
				}
				
				if (v == null || expr == null) {
					continue;
				}
				
				alias.add(v);
				//找什么的？
				final String xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType and ./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType]]]";
				List allocExprs = expr.findXpath(xpath);
				List vnds = new ArrayList();
				for (Object obj : allocExprs) {
					checkAllocationAsInitExpr((ASTAllocationExpression) obj , alias, vnds);
				}
				for (Object obj : vnds) {
					alias.add((VariableNameDeclaration) obj);
				}
				
				/*
				final String xpath = "./PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix/Name";								
				List args = expr.findXpath(xpath);
				if (args.size() > 0) {
					for (Object obj : args) {
						ASTName name = (ASTName) obj;
						String nameimage = name.getImage();
						if (nameimage == null || nameimage.indexOf(".") != -1) {
							continue;
						}

						if (name.getNameDeclaration() == null
								|| !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
							continue;
						}

						VariableNameDeclaration vnd = (VariableNameDeclaration) name
								.getNameDeclaration();
						String typeimage = vnd.getTypeImage();

						if (typeimage.matches(CLASSTYPE_REG_STRING)) {
							alias.add(vnd);
						}
					}
				}
				*/
				return true;
			}
		}
		return false;
	}
	
	private static boolean checkTypeImage(String typeimage) {
		if (typeimage == null) {
			return false;
		}
		return typeimage.matches(CLASSTYPE_REG_STRING);
	}
	
	private static boolean checkNameAsInitParam(ASTName name, AliasSet alias) {
		return checkNameAsInitParam(name, alias, null);
	}
	
	private static boolean checkNameAsInitParam(ASTName name, AliasSet alias, List list) {
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}

		if (name.getImage() != null && name.getImage().contains(".")) {
			return false;
		}
		
		VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();		
		if (list != null) {
			if(v.getTypeImage().matches(RLStateMachine.PRESERVE_REG_STRING)){
				list.add(v);
			}
		}
		if (alias.getTagTreeNode() instanceof ASTVariableDeclaratorId) {
			if (v != ((ASTVariableDeclaratorId)alias.getTagTreeNode()).getNameDeclaration()) {
				return false;
			}
		} if (alias.getTagTreeNode() instanceof ASTName) {
			if (v != ((ASTName)alias.getTagTreeNode()).getNameDeclaration()) {
				return false;
			}
		}

		return true;	
	}
	
	private static boolean checkNameAsAllocationParam(VariableNameDeclaration vnd, ASTName name) {
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}

		if (name.getImage() != null && name.getImage().contains(".")) {
			return false;
		}
		
		VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();		

		if (name.getNameDeclaration() == v || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}

		return checkVariableDeclaration((VariableNameDeclaration)name.getNameDeclaration());	
	}
	
	private static boolean checkAllocationAsAllocationParam(VariableNameDeclaration vnd, ASTAllocationExpression ae) {
		if (!checkTypeImage(((ASTClassOrInterfaceType)ae.jjtGetChild(0)).getImage())) {
			return false;
		}
		
		final String xpath = "./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType and ./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType]]]]/children:*";
		List prefixs = ae.findXpath(xpath);
		
		if (prefixs.size() <= 0) {
			return false;
		}
		
		for (Object obj : prefixs) {			
			if (obj instanceof ASTName) {
				if (checkNameAsAllocationParam(vnd, (ASTName) obj)) {
					return true;
				}
			} else if (obj instanceof ASTAllocationExpression) {
				if (checkAllocationAsAllocationParam(vnd, (ASTAllocationExpression) obj)) {
					return true;
				}
			} else {
				throw new RuntimeException("xpath error : at line "+ae.getBeginLine()+" in source code");
			}
		}
		
		return false;
	}
	
	private static boolean checkAllocationAsInitExpr(ASTAllocationExpression ae, AliasSet alias, List list) {
		if (!checkTypeImage(((ASTClassOrInterfaceType)ae.jjtGetChild(0)).getImage())) {
			return false;
		}
		final String xpath = "./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType and ./Arguments/ArgumentList/Expression/PrimaryExpression[count(*)=1]/PrimaryPrefix[./Name or ./AllocationExpression[./ClassOrInterfaceType]]]]/children:*";
		List prefixs = ae.findXpath(xpath);
		if (prefixs.size() <= 0) {
			return false;
		}
		boolean flag = false;
		for (Object obj : prefixs) {			
			if (obj instanceof ASTName) {
				if (checkNameAsInitParam((ASTName) obj, alias, list)) {
					flag = true;
				}
			} else if (obj instanceof ASTAllocationExpression) {
				if (checkAllocationAsInitExpr((ASTAllocationExpression) obj, alias, list)) {
					flag = true;
				}
			} else {
				throw new RuntimeException("xpath error : at line "+ae.getBeginLine()+" in source code");
			}
		}
		if (flag) return true;
		return false;
	}
	
	private static boolean checkAllocationAsInitExpr(ASTAllocationExpression ae, AliasSet alias) {
		return checkAllocationAsInitExpr(ae, alias, null);
	}
	
	public static boolean checkResourceUseAsInitParam(List nodes, FSMMachineInstance fsmin) {
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			if (checkAllocationAsInitExpr((ASTAllocationExpression) i.next(), alias)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkResourceUseAsParam(List nodes, FSMMachineInstance fsmin) {
		if (!Config.RL_USEASPARAM) {
			AliasSet alias = (AliasSet) fsmin.getRelatedObject();
			Iterator i = nodes.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				ASTArgumentList arglist = (ASTArgumentList) o;
				for (int k = 0; k < arglist.jjtGetNumChildren(); k++) {
					ASTName name = (ASTName) ((SimpleNode) arglist.jjtGetChild(k)).getSingleChildofType(ASTName.class);
					if (name != null && name.getNameDeclaration() instanceof VariableNameDeclaration) {
						if(name.getImage()!=null&&name.getImage().contains(".")){
							continue;
						}
						VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
						if (alias.contains(v)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean checkSameResourceRelease(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (checkPrimaryExpressionRelease((ASTPrimaryExpression)o, fsmin)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkExceptionPath(VexNode vex, FSMMachineInstance fsmin) {
		if (!(vex.getName().startsWith("finally_head_") || vex.getName().startsWith("catch_head_") || vex.getName().startsWith("func_eout_"))) {
			return false;
		}
		
		Hashtable<String, Edge> inedges = vex.getInedges();
		for (Edge e : inedges.values()) {
			VexNode from = e.getTailNode();
			FSMMachineInstance prefsmin=from.getFSMMachineInstanceSet().getTable().get(fsmin);
			if(prefsmin!=null){
				boolean find=false;
				Hashtable<FSMStateInstance, FSMStateInstance>  states=prefsmin.getStates().getTable();
				for(FSMStateInstance state:states.values()){
					if(state.getState().getName().equals("Allocated")){
						find=true;
						break;
					}
				}
				if(find){
					if(!e.getName().startsWith("E_")){
						return false;
					}else if( !fsmin.getRelatedObject().getTagTreeNode().isSelOrAncestor(from.getTreeNode().getConcreteNode())){
						return false;
					}
				}
			}
		}
		
		return true;
	}
	public static boolean checkAliasNotEmpty(VexNode vex, FSMMachineInstance fsmin) {
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		if (!alias.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean checkAliasEmpty(VexNode vex, FSMMachineInstance fsmin) {
		return !checkAliasNotEmpty(vex, fsmin);
	}
	
	public static boolean checkVarOutofFunc(VexNode vex, FSMMachineInstance fsmin) {
		if(vex.getName().startsWith("func_out")){
			if (fsmin.getRelatedObject()instanceof AliasSet) {
				AliasSet alias=(AliasSet)fsmin.getRelatedObject();
				 Hashtable<VariableNameDeclaration, VariableNameDeclaration> table=alias.getTable();
				 Set<VariableNameDeclaration> varSet=table.keySet();
				 for (VariableNameDeclaration var:varSet) {
					if(var.getDeclaratorId().getScope().getClass().equals(ClassScope.class)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	public static boolean checkAliasMemberIsNullPointer(VexNode vex, FSMMachineInstance fsmin) {
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		return alias.isAllMemberNullPointer(vex);
	}
	
	public static List<FSMMachineInstance> createRLInitStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		// 在句柄变量声明时，分配资源
		final String xpath1 = ".//Expression/PrimaryExpression[count(PrimarySuffix)>0]" +
				"/PrimaryPrefix/AllocationExpression" +
				"[./ClassOrInterfaceType and ./Arguments]";
		final String xpath2 = ".//Expression[not(parent::VariableInitializer " +
				"or parent::StatementExpression[count(*)=3 and ./AssignmentOperator and ./PrimaryExpression] " +
				"or parent::ReturnStatement "+
				"or parent::Expression[count(*)=3 and ./AssignmentOperator and ./PrimaryExpression])]" +
				"/PrimaryExpression/PrimaryPrefix/AllocationExpression" +
				"[./ClassOrInterfaceType and ./Arguments]";
/**added by yang 暂时注释掉*/		final String xpath3 = ".//BlockStatement/Statement/StatementExpression/PrimaryExpression/PrimaryPrefix/AllocationExpression";//added by yang

		List evalList1 = node.findXpath(xpath1);
		List evalList2 = node.findXpath(xpath2);
/**added by yang暂时注释掉*/		List evalList3 = node.findXpath(xpath3);
		evalList1.addAll(evalList2);
/**added by yang暂时注释掉*/		evalList1.addAll(evalList3);
		HashSet allocations = new HashSet();
		for (Object o : evalList1) {
			if (allocations.contains(o)) {
				continue;
			}
			allocations.add(o);
			
			ASTClassOrInterfaceType coit = (ASTClassOrInterfaceType)((ASTAllocationExpression)o).jjtGetChild(0);
			if (coit.getImage().matches(PRESERVE_REG_STRING)) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				AliasSet alias = new AliasSet();
				fsminstance.setResultString(coit.getImage());
				alias.setResouceName(coit.getImage());
				alias.setResource(coit);
				fsminstance.setRelatedObject(alias);				
				list.add(fsminstance);
			}
		}		
		return list;
	}
	
	
	/**@author Pzq
	 * 创建UFM状态机
	 * @param node
	 * @param fsm
	 * @return 具有分配资源功能的状态实例链表
	 */
	public static List<FSMMachineInstance> createUFMStateMachines(SimpleJavaNode node, FSMMachine fsm) {
			return RLStateMachine.createRLStateMachines(node, fsm);
	}
	
	/*
	 * 检查资源实例在关闭后是否又重新使用
	 */
	public static boolean checkResuedAfterClosed(VexNode vex, FSMMachineInstance fsmin) {
		
		if (checkAliasEmpty(vex, fsmin)) {
			return false;
		}else {
			AliasSet alias = (AliasSet) fsmin.getRelatedObject();
			Hashtable<VariableNameDeclaration, VariableNameDeclaration> vartableHashtable=alias.getTable();
			List<Edge>list=new ArrayList<Edge>();
			setBeginNode(vex.getTreeNode());
			if(vartableHashtable.size()!=1){
				return false;				
			}
			return checkSucCFGNodeResused(vex,vartableHashtable,list,fsmin);
		}
	}
	/*
	 * 记录递归开始时的抽象语法树节点
	 */
	private static SimpleNode thisbeginNode=null;
	
	public static SimpleNode getBeginNode() {
		return thisbeginNode;
	}

	public static void setBeginNode(SimpleNode beginNode) {
		thisbeginNode = beginNode;
	}
	/*
	 * 记录递归结束时的抽象语法树节点
	 */
	private static SimpleJavaNode endNode=null;
	
	public static SimpleJavaNode getEndNode() {
		return endNode;
	}

	public static void setEndNode(SimpleJavaNode endNode) {
		RLStateMachine.endNode = endNode;
	}
	/*
	 * 检查vartableHashtable是否包含vex及其后继控制流图节点的变量使用出现
	 */
	public static boolean checkSucCFGNodeResused(VexNode vex,Hashtable<VariableNameDeclaration, VariableNameDeclaration> vartableHashtable,List<Edge> edgeList,FSMMachineInstance fsmin) {
		SimpleJavaNode javaNode=vex.getTreeNode();
		if (javaNode instanceof ASTStatementExpression) {
			ASTStatementExpression stateexp=(ASTStatementExpression)javaNode;	
			List<VariableNameDeclaration>varList=new LinkedList<VariableNameDeclaration>();
			if (checkMethodSummaryUFMResourceUse(stateexp,varList)) {
				if (0!=varList.size()) {
					AliasSet alias = (AliasSet) fsmin.getRelatedObject();
					Hashtable<VariableNameDeclaration, VariableNameDeclaration> vtb=alias.getTable();
					for (int i = 0; i < varList.size(); i++) {
						if (null!=varList.get(i)&&vtb.get(varList.get(i))!=null) {
							return true;
						}
					}
				}
			}
		}
		boolean b=false;
		ArrayList<NameOccurrence> occList=vex.getOccurrences();
		Iterator<NameOccurrence>iterator=occList.iterator();
		while (iterator.hasNext()) {
			NameOccurrence nameocc=iterator.next();
			if (vartableHashtable.get(nameocc.getDeclaration())!=null) {
				if (nameocc.getOccurrenceType().equals(NameOccurrence.OccurrenceType.USE)) {
					if (vex.getTreeNode().getFirstParentOfType(ASTCatchStatement.class)==null&&
							vex.getTreeNode().getFirstParentOfType(ASTFinallyStatement.class)==null) {
						if (null==nameocc.getUseDefList()||0==nameocc.getUseDefList().size()) {
							setEndNode(vex.getTreeNode());
							b=true;
						}
						else if(nameocc.getUseDefList().get(0).getLocation().getBeginLine()<=getBeginNode().getBeginLine()){
							setEndNode(vex.getTreeNode());
							b=true;
						}
					}
				}
			}
		}
		if (!b) {
			for (Enumeration<Edge> e=vex.getOutedges().elements();e.hasMoreElements();) {
				Edge edge=e.nextElement();
				if (edgeList.contains(edge)) {
					break;
				}else {
					edgeList.add(edge);
				}
				b=checkSucCFGNodeResused(edge.getHeadNode(),vartableHashtable,edgeList,fsmin);
				if (b) {
					break;
				}
			}
			
		}
		if(getEndNode()!=null)
		{
			vex.setCascadeNode(getEndNode());
		}
		return b;
	}
	
	
	/*
	 * 检查资源实例在关闭后是否未重新使用
	 */
	public static boolean checkNotResuedAfterClosed(VexNode vex, FSMMachineInstance fsmin) {
		return !checkResuedAfterClosed(vex,fsmin);
	}
	/**
	 * expr包含一个函数调用，checkMethodSummayAllocate检查该函数的函数摘要，返回该函数是否具有UFMResourceUse资源使用特征。
	 *
	 * @param sexpr type of (ASTStatementExpression) 包含一个函数调用
	 * @return 该函数是否具有UFMResourceUse资源使用特征
	 */
	private static boolean checkMethodSummaryUFMResourceUse(ASTStatementExpression sexpr,List<VariableNameDeclaration>varlist) {
		/*
		 * 检查expr子孙节点的结构图fy
		 * 
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix
		 *   ... ...(0个或多个ASTPrimarySuffix)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 * 
		 */ 
		if (sexpr == null || sexpr.jjtGetNumChildren()!=1 || sexpr.jjtGetChild(0).jjtGetNumChildren()<2
				|| !(sexpr.jjtGetChild(0) instanceof ASTPrimaryExpression)				
				|| !(sexpr.jjtGetChild(0).jjtGetChild(sexpr.jjtGetChild(0).jjtGetNumChildren()-1) instanceof ASTPrimarySuffix)) {
			return false;
		}
		
		/*
		 * pr包含方法原型，调用pr.getType()可以获取方法原型
		 *
		 * ASTExpression (expr)
		 * --ASTPrimaryExpression
		 *   --ASTPrimaryPrefix             (pr)
		 *   --ASTPrimarySuffix(0个或多个)    (pr)
		 *   --ASTPrimarySuffix
		 *     --ASTArguments
		 */
		Object pr = sexpr.jjtGetChild(0).jjtGetChild(sexpr.jjtGetChild(0).jjtGetNumChildren()-2);		
		if ( !(pr instanceof ASTPrimaryPrefix) && !(pr instanceof ASTPrimarySuffix) ) {
			return false;
		}
		
		// 没有方法原型，无法查找方法实例
		Object type = ((ExpressionBase)pr).getType();
		if (type == null) {
			return false;
		}
		
		// 查函数摘要，是否为UFMResourceUse资源使用函数
		if (type instanceof Method) {
			MethodNode methodnode=MethodNode.findMethodNode(type);
			if(methodnode == null){
				return false;
			}
			
			MethodSummary summary = methodnode.getMethodsummary();
			if (summary == null) {
				return false;
			}
			
			// 得到函数摘要，查询是否具有UFMResourceUse资源使用特征
			for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
				if (!(ff instanceof UFMResourceUseFeature)) {
					continue;
				}				
				else if (((UFMResourceUseFeature) ff).isUMFResourceFunction()) {
					
					UFMResourceUseFeature ufmruf=(UFMResourceUseFeature) ff;
					for (MapOfVariable mov : ufmruf.getTable().keySet()){
						VariableNameDeclaration vnamedec=mov.findVariable((ExpressionBase)pr);
						varlist.add(vnamedec);
					}
					
					allocInfo = ((UFMResourceUseFeature) ff).getTraceInfo();
					return true;
				}
				
				// 确定是否具有UFMResourceUse资源使用特征，可以返回
				return ((UFMResourceUseFeature) ff).isUMFResourceFunction();
			}
		}
		
		return false;
	}
	
	/*
	 * 检查资源实例是否具有重复关闭特征
	 */
	public static boolean checkReclosed(VexNode vex, FSMMachineInstance fsmin) {
		SimpleJavaNode sjn=vex.getTreeNode();
		ASTStatementExpression se=null;
		if (sjn instanceof ASTStatementExpression) {
			se=(ASTStatementExpression)sjn;
		}
		if (se == null || se.jjtGetNumChildren()!=1 || se.jjtGetChild(0).jjtGetNumChildren()<2
				|| !(se.jjtGetChild(0) instanceof ASTPrimaryExpression)				
				|| !(se.jjtGetChild(0).jjtGetChild(se.jjtGetChild(0).jjtGetNumChildren()-1) instanceof ASTPrimarySuffix)) {
			return false;
		}
		
		Object pr = se.jjtGetChild(0).jjtGetChild(se.jjtGetChild(0).jjtGetNumChildren()-2);		
		if ( !(pr instanceof ASTPrimaryPrefix) && !(pr instanceof ASTPrimarySuffix) ) {
			return false;
		}
		
		Object type = ((ExpressionBase)pr).getType();
		if (type == null) {
			return false;
		}
		
		if (type instanceof Method) {
			MethodNode methodnode=MethodNode.findMethodNode(type);
			if(methodnode == null){
				return false;
			}
			
			MethodSummary summary = methodnode.getMethodsummary();
			if (summary != null) {
				for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
					if (!(ff instanceof ReleaseFeature)) {
						continue;
					}
					ReleaseFeature rf = (ReleaseFeature) ff;
					return rf.isReclosed();
				}
			}
		
			return false;
		}
		return false;
	}
}
