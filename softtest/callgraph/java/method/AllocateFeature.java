package softtest.callgraph.java.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jaxen.JaxenException;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTResultType;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTType;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.LocalScope;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * AllocateFeature用于描述一个方法是否具有资源分配的特征。
 * 它所描述的特征有两种：
 * 1. 方法返回值是新分配的资源;
 * 2. 修改方法所属对象的成员属性。
 * 
 * @author 杨绣
 *
 */
public class AllocateFeature extends AbstractFeature{	
	/**
	 * 描述成员方法的返回值是否为新分配的资源
	 */
	private boolean isAllocateFunction = false;
	
	/**
	 * 描述函数调用资源分配的trace信息
	 */
	private List<String> traceinfo = null;
	
	public List<String> getTraceInfo() {
		return traceinfo;
	}
	
	/**
	 * 描述成员方法的返回值是否为新分配的资源
	 * @return this.isAllocateFunction
	 */
	public boolean isAllocateFunction() {
		return this.isAllocateFunction;
	}
	
	private String current_func = null;
	
	@Override
	public void listen(SimpleJavaNode node, FeatureSet set) {
		if (node instanceof ASTMethodDeclaration) {
			current_func = ((ASTMethodDeclaration) node).getMethodName();
		} else if (node instanceof ASTConstructorDeclaration) {
			current_func = ((ASTConstructorDeclaration) node).getMethodName();
		}
		
		AllocateFeatureVisitor vsitor = new AllocateFeatureVisitor();
		node.jjtAccept(vsitor, null);
		if (isAllocateFunction) {
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
			"InputStream","InitialDirContext","DirContext","ReadableByteChannel","OutputStream","DirectoryDialog","MessageBox",
			};
	//added by yang"DataInputStream"
	private static HashSet res_strings = new HashSet<String>(Arrays.asList(RES_STRINGS));
	
	/**
	 * 调用者保证expr的子节点满足xpath: 
	 * ./PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType]
	 * 
	 * 检查 new XXXX () 是否具有分配资源特征
	 *
	 * @param expr ASTExpression子节点是一个AllocationExpression
	 * @return 是否具有分配资源特征
	 */
	private boolean checkNewAllocExpression(ASTExpression expr) {
		if (expr == null) {
			return false;
		}
		
		List aeList = expr.findXpath(".//AllocationExpression[./ClassOrInterfaceType]");
		for (Object obj : aeList) {
			ASTAllocationExpression ae = (ASTAllocationExpression) obj;
			ASTClassOrInterfaceType coit = (ASTClassOrInterfaceType)(ae.jjtGetChild(0));
			if (coit.getImage() == null || coit.getImage().trim().length() == 0) {
				continue;
			}
			if (res_strings.contains(coit.getImage())) {
				// 确定具有分配资源特征，可以返回
				List<String> newlist = new ArrayList<String>();
				if(softtest.config.java.Config.LANGUAGE==0){
					newlist.add("文件:"+ProjectAnalysis.getCurrent_file()+" 行:"+expr.getBeginLine()+" 方法:"+current_func);
				}else{
					newlist.add("file:"+ProjectAnalysis.getCurrent_file()+" line:"+expr.getBeginLine()+" Method:"+current_func);
				}
				
				traceinfo = newlist;
				return true;
			}
		}
		return false;
	}
	
	/**	 
	 * 调用者保证expr的子节点满足xpath: 
	 * ./PrimaryExpression[count(PrimarySuffix)>=1 and count(PrimaryPrefix)=1 and ./PrimaryPrefix/Name and ./PrimarySuffix/Arguments]
	 * 
	 * 检查函数返回值是否具有资源分配特征
	 * 
	 * 1. 查函数摘要，是否为分配资源函数
	 * 2. 查分配资源方法表，是否为分配资源函数
	 * 3. 查node.getType()，该方法是否会return资源类型
	 *
	 * 形如：
	 * x.getConnection()
	 * or getConnection()
	 * or a.b.getA().c.getConnection()
	 * 
	 * @param expr ASTExpression形如x.y(z)的函数调用
	 * @return 是否具有分配资源特征
	 */
	private boolean checkFuncCallExpression(ASTExpression expr) {
		if (expr == null || expr.jjtGetNumChildren()!=1 || expr.jjtGetChild(0).jjtGetNumChildren()<2) {
			return false;
		}
		
		// 获取type
		ASTPrimaryExpression pe = (ASTPrimaryExpression)expr.jjtGetChild(0);
		Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		Object type = null;
		if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
			type = ((ExpressionBase)pr).getType();
		} else {
			return false;
		}
		
		// 获取方法名
		String methodName = null;
		if ( (pr instanceof ASTPrimaryPrefix) && (((ASTPrimaryPrefix)pr).jjtGetChild(0) instanceof ASTName) ) {
			methodName = ((ASTName)((ASTPrimaryPrefix)pr).jjtGetChild(0)).getImage();
		}
		else if ( (pr instanceof ASTPrimarySuffix) && (((ASTPrimarySuffix)pr).jjtGetNumChildren() == 0)) {
			methodName = ((ASTPrimarySuffix)pr).getImage();
		}
		else {
			// 未知异常情况，默认返回false
			return false;
		}
		if (methodName == null || methodName.trim().length() == 0) {
			return false;
		}
		
		// 1. 查函数摘要，是否为分配资源函数
		MethodNode methodnode=null;
		if (type != null && (type instanceof Method)) {
			methodnode=MethodNode.findMethodNode(type);
			if(methodnode != null){
				MethodSummary summary = methodnode.getMethodsummary();
				
				// 得到函数摘要，查询是否具有分配资源特征
				if (summary != null) {
					for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
						if (!(ff instanceof AllocateFeature)) {
							continue;
						}
						
						if (((AllocateFeature)ff).isAllocateFunction()) {
							List<String> newlist = new ArrayList<String>(((AllocateFeature)ff).getTraceInfo());
							/*
							String name = methodName;
							if (type instanceof Method) {
								name = ((Method) type).toGenericString();
							} else if (type instanceof Constructor) {
								name = ((Constructor) type).toGenericString();
							}
							*/
							if(softtest.config.java.Config.LANGUAGE==0){
								newlist.add("文件:"+ProjectAnalysis.getCurrent_file()+" 行:"+expr.getBeginLine()+" 方法:"+current_func);
							}else{
								newlist.add("file:"+ProjectAnalysis.getCurrent_file()+" line:"+expr.getBeginLine()+" Method:"+current_func);
							}
							
							traceinfo = newlist;
							return true;
						}
						
						// 确定是否具有分配资源特征，可以返回
						return false;
					}
					return false;
				}
			}
		}
		
		// 2. 查分配资源方法表，是否为分配资源函数		
		// 根据预定义的方法名进行正则匹配。匹配成功即认为方法名是一个分配资源的函数。
		if (methodName.matches(regex4)) {
			List<String> newlist = new ArrayList<String>();			
			/*
			String name = methodName;
			if (type instanceof Method) {
				name = ((Method) type).toGenericString();
			} else if (type instanceof Constructor) {
				name = ((Constructor) type).toGenericString();
			}
			*/
			if(softtest.config.java.Config.LANGUAGE==0){
				newlist.add("文件:"+ProjectAnalysis.getCurrent_file()+" 行:"+expr.getBeginLine()+" 方法:"+current_func);
			}else{
				newlist.add("file:"+ProjectAnalysis.getCurrent_file()+" line:"+expr.getBeginLine()+" Method:"+current_func);
			}
			traceinfo = newlist;
			return true;
		}
		
		// 3. 查node.getType()，该方法是否会return资源类型
		// to be implemented. to do or not to do???
				
		return false;
	}
	
	/**
	 * 检查定义引用链，最近一次定义是否为分配资源
	 * 
	 * 调用者保证expr的子节点满足xpath:
	 * ./PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name
	 * 
	 * @param expr ASTExpression
	 * @return
	 */
	private boolean checkVarExpression(ASTExpression expr) {		
		ASTName nn = (ASTName)expr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
		if (!(nn.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}
		
		VariableNameDeclaration vnd = (VariableNameDeclaration) nn.getNameDeclaration();
		
		// 获得nn所在的控制流节点vex
		VexNode vex = nn.getCurrentVexNode();
		if (vex == null) {
			return false;
		}
		
		// 检查初始化是否分配资源
		if (vnd.getDeclareScope() instanceof LocalScope && expr.getScope().isSelfOrAncestor(vnd.getDeclareScope()) && expr.getScope().getEnclosingMethodScope().equals(vnd.getDeclareScope().getEnclosingMethodScope())) {
			ASTVariableDeclaratorId vdi = vnd.getDeclaratorId();
			if (vdi.jjtGetParent() instanceof ASTVariableDeclarator && vdi.getNextSibling() instanceof ASTVariableInitializer) {
				ASTVariableInitializer vi = (ASTVariableInitializer) vdi.getNextSibling();
				if (vi.jjtGetNumChildren() == 1 && vi.jjtGetChild(0) instanceof ASTExpression) {
					// 检查赋值号右边的表达式是否分配资源(间接递归)
					if (checkExpression((ASTExpression) vi.jjtGetChild(0))) {
						return true;
					}
				}
			}
		}
		
		// 仅考察在vex出现的vnd
		// UseDefList不包含函数初始化的定义，需要另外处理！！！ to be implemented
		for (NameOccurrence occ : vex.getOccurrences()) {
			if (occ.getDeclaration() != vnd) {
				continue;
			}
			
			if(occ.getUseDefList()==null){
				continue;
			}
			
			// 考察对vnd的赋值
			for (NameOccurrence no : occ.getUseDefList()) {
				if ( !no.isOnLeftHandSide()) {
					continue;
				}
				ASTPrimaryExpression pe = (ASTPrimaryExpression)no.getLocation().getFirstParentOfType(ASTPrimaryExpression.class);
				if (pe != null && pe.getNextSibling() instanceof ASTAssignmentOperator) {
					ASTAssignmentOperator ao = (ASTAssignmentOperator) pe.getNextSibling();
					if ( ao.getNextSibling() instanceof ASTExpression) {
						// 检查赋值号右边的表达式是否分配资源(间接递归)
						ASTExpression e = (ASTExpression)ao.getNextSibling();
						
						while (e.jjtGetNumChildren()==3 && e.jjtGetChild(1) instanceof ASTAssignmentOperator && e.jjtGetChild(2) instanceof ASTExpression) {
							e = (ASTExpression) e.jjtGetChild(2);
						}
						
						if (e != expr && checkExpression(e)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;		
	}
	
	/**
	 * 检查表达式是否分配资源
	 *
	 * @param expr ASTExpression
	 * @return 是否分配资源
	 */
	private boolean checkExpression(ASTExpression node) {
		if (node == null) {
			return false;
		}
		
		ASTMethodDeclaration md=(ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
		if (md!=null) {
			if (md.jjtGetChild(0).jjtGetChild(0)instanceof ASTType) {
				ASTType type=(ASTType)md.jjtGetChild(0).jjtGetChild(0);
				String typeImage=type.getTypeImage();
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
					return true;
				}
			}
		}
		
		
//		try {
//			/*
//			 * return new XXXX();
//			 * 返回的对象是资源实例或封装资源的实例
//			 */
//			if (node.findChildNodesWithXPath("./PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/AllocationExpression[./ClassOrInterfaceType]").size() != 0) {
//				return checkNewAllocExpression(node);
//			}
//			
//			/*
//			 * return v;
//			 * 返回变量的最后一次定义是赋值分配资源实例
//			 * 
//			 * 检查定义引用链，最近一次定义是否为分配资源
//			 */
//			if (node.findChildNodesWithXPath("./PrimaryExpression[not(./PrimarySuffix)]/PrimaryPrefix/Name").size() != 0) {
//				return checkVarExpression(node);
//			}
//			
//			/*
//			 * return x.getConnection();
//			 * or return getConnection();
//			 * or return a.b.getA().c.getConnection();
//			 * 
//			 * 返回值的调用的函数具有资源分配特征
//			 * 
//			 * 1. 查函数摘要，是否为分配资源函数
//			 * 2. 查分配资源方法表，是否为分配资源函数
//			 * 3. 查node.getType()，该方法是否会return资源类型
//			 */
//			if (node.findChildNodesWithXPath("./PrimaryExpression[count(PrimarySuffix)>=1 and count(PrimaryPrefix)=1 and ./PrimaryPrefix/Name and ./PrimarySuffix/Arguments]").size() != 0) {
//				return checkFuncCallExpression(node);
//			}
//		} catch (JaxenException e) {
//			throw new RuntimeException("xpath error @ AllocateFeature.java : checkExpression(ASTExpression node)",e);
//		}
		return false;
	}
	
	/**
	 * 检查node节点是否是赋值表达式节点
	 * @param node 需要检查的节点，通常为StatementExpression或Expression
	 * @return 是否是赋值表达式节点
	 */
	private boolean checkAssignmentExpression(SimpleJavaNode node) {
		if (node.jjtGetNumChildren()==3 
				&& node.jjtGetChild(1) instanceof ASTAssignmentOperator 
				&& ((ASTAssignmentOperator) node.jjtGetChild(1)).getImage().equals("=")
				&& node.jjtGetChild(0) instanceof ASTPrimaryExpression 
				&& node.jjtGetChild(2) instanceof ASTExpression) {			
			if (checkAssignmentExpressionLeft((ASTPrimaryExpression) node.jjtGetChild(0)) 
					&& checkAssignmentExpressionRight((ASTExpression) node.jjtGetChild(2)) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 检查赋值号左边的语法树节点是否形如
	 * PrimaryExpression
	 *   --PrimaryPrefix
	 *     --Name
	 * @param node 赋值号左边的语法树节点
	 * @return node及其子节点是否为PrimaryExpression-PrimaryPrefix-Name结构
	 */
	private boolean checkAssignmentExpressionLeft(SimpleJavaNode node) {
		if (node instanceof ASTPrimaryExpression 
				&& node.jjtGetNumChildren()==1 
				&& node.jjtGetChild(0).jjtGetNumChildren()==1 
				&& node.jjtGetChild(0).jjtGetChild(0) instanceof ASTName) {
			ASTName name = (ASTName) node.jjtGetChild(0).jjtGetChild(0);
			if (name.getNameDeclaration() instanceof VariableNameDeclaration) {
				VariableNameDeclaration vnd = (VariableNameDeclaration) name.getNameDeclaration();
				if (vnd.getDeclareScope() == name.getScope().getEnclosingClassScope()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean checkAssignmentExpressionRight(SimpleJavaNode node) {
		if (node instanceof ASTExpression && checkExpression((ASTExpression) node)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 对函数特征信息进行迭代。
	 * 需要考虑的情况有以下几种：
	 * 
	 * 1. d.getxx(...) 在分配特征中特指形如DriverManager.getConnection(a,b,c)、返回值为某种资源的函数调用;
	 * 2. d.getxx(...) 在分配特征中特指形如DriverManager.getConnection(a,b,c)、返回值为void、把生成的资源实例赋给d.xx的函数调用;
	 * 3. gen(...) 在分配特征中特指形如getConnection(a,b,c)、返回值为某种资源的函数调用;
	 * 4. gen(...) 在分配特征中特指形如getConnection(a,b,c)、返回值为void、把生成的资源实例赋给this.xx的函数调用;
	 * 
	 */
	private class AllocateFeatureVisitor extends JavaParserVisitorAdapter{
		/**
		 * 通过检查return语句，确定函数返回值是否为新分配资源。
		 * 1. return new FileReader("abc");
		 * 2. return DriverManager.getConnection();
		 * 3. return v;
		 *    (1) v = new FileReader("abc");
		 *    (2) v = DriverManager.getConnection();
		 */
		@Override
		public Object visit(ASTReturnStatement node, Object data) {
			if (node == null) {
				return null;
			}
			
			if (node.jjtGetNumChildren() == 1 && node.jjtGetChild(0) instanceof ASTExpression) {
				isAllocateFunction = checkExpression((ASTExpression)node.jjtGetChild(0));
			}
			return null;
		}
		
		/*
		public Object visit(ASTStatementExpression node, Object data) {
			if (checkAssignmentExpression(node)) {
				ASTName name = (ASTName) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
				VariableNameDeclaration vnd = (VariableNameDeclaration) name.getNameDeclaration();
				List<String> list = new ArrayList<String>();
				list.add("file:"+ProjectAnalysis.current_file+"line:"+node.getBeginLine()+"\n");
				table.put(new MapOfVariable(vnd), list);
			}
			return super.visit(node, data);
		}
		
		public Object visit(ASTExpression node, Object data) {
			if (checkAssignmentExpression(node)) {
				ASTName name = (ASTName) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
				VariableNameDeclaration vnd = (VariableNameDeclaration) name.getNameDeclaration();
				List<String> list = new ArrayList<String>();
				list.add("file:"+ProjectAnalysis.current_file+"line:"+node.getBeginLine()+"\n");
				table.put(new MapOfVariable(vnd), list);
			}
			return super.visit(node, data);
		}
		
		public Object visit(ASTArguments node, Object data) {
			if (node.jjtGetParent() instanceof ASTPrimarySuffix) {
				ASTPrimarySuffix suffix = (ASTPrimarySuffix) node.jjtGetParent();
				if (suffix.getLastSibling() instanceof ExpressionBase) {
					ExpressionBase eb = (ExpressionBase) suffix.getLastExpression();
					if (eb.getType() instanceof Method) {
						MethodNode mn = MethodNode.findMethodNode(eb.getType());
						if (mn != null && mn.getMethodsummary() != null) {
							MethodSummary summary = mn.getMethodsummary();
							// 得到函数摘要，查询是否具有分配资源特征
							for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
								if (!(ff instanceof AllocateFeature)) {
									continue;
								}
								AllocateFeature fea = (AllocateFeature) ff;
								Hashtable<MapOfVariable, List<String>> fun = fea.getTable();
								
								for (MapOfVariable mov : fun.keySet()) {
									if (mov.findVariable(eb) != null) {
										
									}
								}
								
								// 确定是否具有分配资源特征，可以返回
								//return ((AllocateFeature) ff).isAllocateFunction();
							}
						}
					}
				}
			}
			return super.visit(node, data);
		}
		*/
		
		/*
		 * 
		 * ASTPrimaryExpression
		 * --ASTPrimaryPrefix
		 *   --ASTName(*)
		 * --ASTPrimarySuffix
		 *   --ASTArguments
		 * 
		 * @param node ASTName
		 * @return null
		 */
		/*
		public Object visit(ASTName node, Object data) {
			// 参数检查
			if (node == null || node.getImage() == null) {
				return null;
			}
			
			// ASTName的父节点必须是ASTPrimaryPrefix, 父父节点必须是ASTPrimaryExpression
			if ( !(node.jjtGetParent() instanceof ASTPrimaryPrefix) || !(node.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression) ) {
				return null;
			}
			
			// ASTPrimaryExpression只有两个子节点ASTPrimaryPrefix、ASTPrimarySuffix。
			{
				
				ASTPrimaryExpression pe = (ASTPrimaryExpression)node.jjtGetParent().jjtGetParent();
				
				if (pe.jjtGetNumChildren() != 2) {
					return null;
				}
				if (pe.jjtGetChild(1).jjtGetNumChildren() != 1 || !(pe.jjtGetChild(1).jjtGetChild(0) instanceof ASTArguments)) {
					return null;
				}
			}
			
			if (node.getImage().matches("^[A-Za-z_][A-Za-z0-9_]*\\.[A-Za-z_][A-Za-z0-9_]*+$")) {
				// 形如 x.close, y._a
				
			}
			else if (node.getImage().matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
				// 形如 close, _a, _a0
			}
			else {
				// 目前仅处理上述两种情况
				// do nothing here
			}
			
			return null;
		}
		*/
		/*
		public Object visit1(ASTName node, Object data) {
			//p.f();
			if(!node.getImage().contains(".")){
				//名字中没有包含引用符号
				return null;
			}
			List<NameDeclaration> decllist = node.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			MethodScope methodscope=node.getScope().getEnclosingMethodScope();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (!(decl instanceof VariableNameDeclaration)){
					//检查是否为变量
					continue;
				}
				VariableNameDeclaration v = (VariableNameDeclaration) decl;
				
				ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
				if(node.getImage().startsWith(classcope.getClassName()+".")){
					//过滤 类名.成员 情况
					continue;
				}	
				
				if(table.contains(v)){
					//检查是否已经添加过
					continue;
				}
				if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
					//检查是否为 成员或函数参数变量
					continue;
				}
				VexNode vex=node.getCurrentVexNode();
				if(vex==null){
					//检查当前控制流图节点是否为空
					continue;
				}
				Object domain = vex.getDomainWithoutNull(v);
				if (domain == null || DomainSet.getDomainType(domain) != ClassType.REF) {
					//检查是否为 引用型变量
					continue;
				}
				
				ReferenceDomain refdomain=(ReferenceDomain)domain;
				if(!refdomain.getUnknown()){
					continue;
				}
				
				for(NameOccurrence occ:vex.getOccurrences()){
					if(occ.getDeclaration()==v){
						if(occ.getUseDefList()!=null){
							continue;
						}
					}
				}
				
				List<SimpleJavaNode> list = new ArrayList<SimpleJavaNode>();
				list.add(node);
				table.put(v, list);
			}
			return null;
		}

		@Override
		public Object visit(ASTArguments node, Object data) {
			//f(p,a);
			if(node.jjtGetParent() instanceof ASTPrimarySuffix &&
					node.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)node.jjtGetParent();
				ASTPrimaryExpression primary=(ASTPrimaryExpression)node.jjtGetParent().jjtGetParent();
				ExpressionBase last=(ExpressionBase)primary.jjtGetChild(suffix.getIndexOfParent()-1);
				if(last.getType() instanceof Method){
					MethodNode methodnode=ProjectAnalysis.getMcgraph().getMethodNode(last.getType());
					if(methodnode==null){
						return super.visit(node, data);
					}
					MethodSummary summary = methodnode.getMethodsummary();
					if (summary == null) {
						return super.visit(node, data);
					}

					for (AbstractPrecondition pre : summary.getPreconditons().getTable().values()) {
						if (!(pre instanceof NpdPrecondition)) {
							continue;
						}
						NpdPrecondition npdpre = (NpdPrecondition) pre;
						Hashtable<VariableNameDeclaration,List<SimpleJavaNode>> usedvar = npdpre.check(last);
						Iterator<Map.Entry<VariableNameDeclaration,List<SimpleJavaNode>>> i=usedvar.entrySet().iterator();
						while (i.hasNext()) {
							Map.Entry<VariableNameDeclaration,List<SimpleJavaNode>> entry=i.next();
							VariableNameDeclaration v=entry.getKey();
							List<SimpleJavaNode> list=entry.getValue();
							
							VexNode vex=node.getCurrentVexNode();
							if(vex==null){
								//检查当前控制流图节点是否为空
								continue;
							}
							Object domain = vex.getDomainWithoutNull(v);
							if (domain == null || DomainSet.getDomainType(domain) != ClassType.REF) {
								//检查是否为 引用型变量
								continue;
							}
														
							for(NameOccurrence occ:vex.getOccurrences()){
								if(occ.getDeclaration()==v){
									if(occ.getUseDefList()!=null){
										continue;
									}
								}
							}
							list.add(node);
							table.put(v, list);
						}
					}
				}
			}
			return super.visit(node, data);
		}
		*/
	}

}
