package softtest.callgraph.java.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTConditionalAndExpression;
import softtest.ast.java.ASTConditionalExpression;
import softtest.ast.java.ASTConditionalOrExpression;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;
import softtest.domain.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.MethodScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**NPDǰ������*/
public class NpdPrecondition extends AbstractPrecondition{
	
	/* (non-Javadoc)
	 * @see softtest.callgraph.java.method.AbstractPrecondition#listen(softtest.cfg.java.VexNode, softtest.callgraph.java.method.PreconditionSet)
	 */
	@Override
	public void listen(SimpleJavaNode node,PreconditionSet set) {
		
		NpdPreconditionVisitor vsitor=new NpdPreconditionVisitor();
		vsitor.root=node;
		node.jjtAccept(vsitor, null);
		
		if(table.size()>0){
			set.addPreconditon(this);
		}
	}
	

	/**
	 * ����ǰǰ��������ʹ��ǰ���������溯������
	 * @param node node ����Mehtod���ͻ�Constructor���͵ı��ʽ�ڵ㣬ͨ��������ASTPrimarySuffix����ASTPrimaryPrefix����ASTAllocationExpression
	 * @return �ں������ù����лᱻֱ�����õ�ʵ�ʲ�����ϣ��keyΪʵ�α�����valueΪ���ٵĺ�������trace��Ϣ
	 */
	public Hashtable<VariableNameDeclaration,List<String>> check(SimpleJavaNode node){
		Hashtable<VariableNameDeclaration,List<String>> ret= new Hashtable<VariableNameDeclaration,List<String>>();
		Iterator<Map.Entry<MapOfVariable,List<String>>> i=table.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,List<String>> entry=i.next();
			MapOfVariable m=entry.getKey();
			List<String> list=entry.getValue();
			VariableNameDeclaration v=m.findVariable(node);
			if(v!=null){
				ret.put(v, list);
			}
		}	
		return ret;
	}
	
	/**
	 * �����﷨���ڵ㣬��麯���ڲ������ñ�����ֱ��ʹ��p.f()��ͬʱҲ������㺯�����ù�ϵf(p,a)����f
	 * ժҪ�д��ڶ�p��ֱ��ʹ��
	 */
	private class NpdPreconditionVisitor extends JavaParserVisitorAdapter{
		SimpleJavaNode root;
		HashSet<VariableNameDeclaration> set = new HashSet<VariableNameDeclaration>();
	 
		@Override
		public Object visit(ASTName node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			//p.f();
			ASTExpression forexp=(ASTExpression)node.getSingleParentofType(ASTExpression.class);
			boolean isforeach=false;
			if(forexp!=null&&forexp.getPrevSibling()instanceof ASTForEachVariableDeclaration){
				isforeach=true;
			}
			if(!node.getImage().contains(".")&&!isforeach){
				//������û�а������÷���
				return null;
			}
			List<NameDeclaration> decllist = node.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			MethodScope methodscope=node.getScope().getEnclosingMethodScope();
			boolean first=true;
out:		while (decliter.hasNext()&&first) {
				NameDeclaration decl = decliter.next();
				if (!(decl instanceof VariableNameDeclaration)){
					//����Ƿ�Ϊ����
					continue;
				}
				first=false;//ֻ�����һ������ Ϊ������p.q=null;�������
				VariableNameDeclaration v = (VariableNameDeclaration) decl;
				
				ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
				if(node.getImage().startsWith(classcope.getClassName()+".")){
					//���� ����.��Ա ���
					continue;
				}	
				
				if(set.contains(v)){
					//����Ƿ��Ѿ���ӹ�
					continue;
				}
				//����Ƿ�Ϊ ��Ա������������
				//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
				if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//				if(methodscope!=v.getDeclareScope()){
					//����Ƿ�Ϊ��ǰ��������
					continue;
				}
				VexNode vex=node.getCurrentVexNode();
				if(vex==null){
					//��鵱ǰ������ͼ�ڵ��Ƿ�Ϊ��
					continue;
				}
				
				if(vex.getContradict()){
					//���ɴ�ڵ�
					continue;
				}
				
				Object domain = vex.getDomainWithoutNull(v);
				if (domain == null || DomainSet.getDomainType(domain) != ClassType.REF) {
					//����Ƿ�Ϊ �����ͱ���
					continue;
				}
				
				ReferenceDomain refdomain=(ReferenceDomain)domain;
				if(!refdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
					continue;
				}
				
				for(NameOccurrence occ:vex.getOccurrences()){
					if(occ.getDeclaration()==v){
						if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
							continue out;
						}
					}
				}
				
				if(!checkNPDNotGuard(node,v)){
					continue;
				}
				
				List<String> list = new ArrayList<String>();
				ASTMethodDeclaration currentmethoddecl=(ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
				ASTConstructorDeclaration currentConstructordecl=(ASTConstructorDeclaration)node.getFirstParentOfType(ASTConstructorDeclaration.class);
				String methodname="";
				if(currentmethoddecl!=null){
					methodname= currentmethoddecl.getMethodName();
				} else if(currentConstructordecl!=null){
					methodname= currentConstructordecl.getMethodName();
				} 
				if(softtest.config.java.Config.LANGUAGE==0){
					list.add(0,"�ļ�:"+ProjectAnalysis.current_file+" ��:"+node.getBeginLine()+" ����:"+methodname+"\n");
				}else{
					list.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
				}
				table.put(new MapOfVariable(v), list);
				set.add(v);
			}
			return null;
		}

		@Override
		public Object visit(ASTArguments node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			//f(p,a);
			Object type=null;
			ExpressionBase last=null;
			if(node.jjtGetParent() instanceof ASTPrimarySuffix &&
					node.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)node.jjtGetParent();
				ASTPrimaryExpression primary=(ASTPrimaryExpression)node.jjtGetParent().jjtGetParent();
				last=(ExpressionBase)primary.jjtGetChild(suffix.getIndexOfParent()-1);
				type=last.getType();
			}else if(node.jjtGetParent() instanceof ASTAllocationExpression){
				ASTAllocationExpression allo=(ASTAllocationExpression)node.jjtGetParent();
				type=allo.getType();
				last=allo;
			}
			if(type instanceof Method||type instanceof Constructor){
				MethodNode methodnode=MethodNode.findMethodNode(type);
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
					Hashtable<VariableNameDeclaration,List<String>> usedvar = npdpre.check(last);
					Iterator<Map.Entry<VariableNameDeclaration,List<String>>> i=usedvar.entrySet().iterator();
	out:			while (i.hasNext()) {
						Map.Entry<VariableNameDeclaration,List<String>> entry=i.next();
						VariableNameDeclaration v=entry.getKey();
						List<String> list=entry.getValue();
							
						if(set.contains(v)){
							//����Ƿ��Ѿ���ӹ�
							continue;
						}
						MethodScope methodscope=node.getScope().getEnclosingMethodScope();
						//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
						if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//						if(methodscope!=v.getDeclareScope()){
							//����Ƿ�Ϊ��ǰ��������
							continue;
						}
							
						VexNode vex=node.getCurrentVexNode();
						if(vex==null){
							//��鵱ǰ������ͼ�ڵ��Ƿ�Ϊ��
							continue;
						}
							
						if(vex.getContradict()){
							//���ɴ�ڵ�
							continue;
						}
							
							
						Object domain = vex.getDomainWithoutNull(v);
						if (domain == null || DomainSet.getDomainType(domain) != ClassType.REF) {
							//����Ƿ�Ϊ �����ͱ���
							continue;
						}
							
						ReferenceDomain refdomain=(ReferenceDomain)domain;
						if(!refdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
							continue;
						}
														
						for(NameOccurrence occ:vex.getOccurrences()){
							if(occ.getDeclaration()==v){
								if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
									continue out;
								}
							}
						}
							
						if(!checkNPDNotGuard(node,v)){
							continue;
						}
							
						ASTMethodDeclaration currentmethoddecl=(ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
						ASTConstructorDeclaration currentConstructordecl=(ASTConstructorDeclaration)node.getFirstParentOfType(ASTConstructorDeclaration.class);
						String methodname="";
						if(currentmethoddecl!=null){
							methodname= currentmethoddecl.getMethodName();
						} else if(currentConstructordecl!=null){
							methodname= currentConstructordecl.getMethodName();
						} 
						List<String> newlist=new ArrayList<String>();
						for(String s:list){
							newlist.add(s);
						}
						if(softtest.config.java.Config.LANGUAGE==0){
							newlist.add(0,"�ļ�:"+ProjectAnalysis.current_file+" ��:"+node.getBeginLine()+" ����:"+methodname+"\n");
						}else{
							newlist.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
						}
						
						table.put(new MapOfVariable(v), newlist);
						set.add(v);
					}
				}
			}
			return super.visit(node, data);
		}
		
	}

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer("NPD-Precondition:");
		Iterator<Map.Entry<MapOfVariable,List<String>>> i=table.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,List<String>> entry=i.next();
			MapOfVariable m=entry.getKey();
			List<String> list=entry.getValue();
			buff.append("("+m.toString()+")");
		}	
		return buff.toString();
	}
	
	/**
	 * @param usenode NPDʹ�ô���Ӧ���﷨���ڵ�
	 * @param v	��������
	 * @return �����NPD�����ʵؼ���ˣ��򷵻�false�����򷵻�true
	 */
	public static boolean checkNPDNotGuard(SimpleJavaNode usenode,VariableNameDeclaration v){
		VexNode vex=usenode.getCurrentVexNode();
		if(vex==null){
			return true;
		}
		SimpleNode treenode=vex.getTreeNode().getConcreteNode();
		if(treenode==null){
			return true;
		}
		// ����Ƿ��������ʽ
		{
			ASTConditionalExpression con = (ASTConditionalExpression) usenode.getFirstParentOfType(ASTConditionalExpression.class);
			if (con != null) {
				String str = ".//EqualityExpression/PrimaryExpression/PrimaryPrefix/Name[../../../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]";
				List evaluationResults = null;
				try {
					if (con.jjtGetChild(0) instanceof ASTEqualityExpression) {
						str = "./PrimaryExpression/PrimaryPrefix/Name[../../../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]";
					}
					XPath xpath = new BaseXPath(str, new DocumentNavigator());
					evaluationResults = xpath.selectNodes(con.jjtGetChild(0));
				} catch (JaxenException e) {
					if (softtest.config.java.Config.DEBUG) {
						e.printStackTrace();
					}
					throw new RuntimeException("xpath error",e);
				}
				if (evaluationResults.size() > 0) {
					ASTName temp = (ASTName) evaluationResults.get(0);
					if (temp.getNameDeclaration() == v) {
						ASTEqualityExpression equal = (ASTEqualityExpression) temp.jjtGetParent().jjtGetParent().jjtGetParent();
						if (equal.getImage().equals("==")) {
							if (usenode.isSelOrAncestor((SimpleJavaNode) con.jjtGetChild(2))) {
								return false;
							}
						} else {
							if (usenode.isSelOrAncestor((SimpleJavaNode) con.jjtGetChild(1))) {
								return false;
							}
						}
					}
				}
			}
		}
		{
			
			String str = ".//InstanceOfExpression/PrimaryExpression/PrimaryPrefix/Name";
			List evaluationResults = null;
			evaluationResults=treenode.findXpath(str);
			Iterator itor = evaluationResults.iterator();
			boolean beffect = true;
			while (itor.hasNext()) {
				ASTName temp = (ASTName) itor.next();
				if (temp.getNameDeclaration() == v&&!temp.getImage().contains(".")) {
					ASTInstanceOfExpression ins = (ASTInstanceOfExpression) temp.jjtGetParent().jjtGetParent().jjtGetParent();
					//����Ƿ�Ϊ&&���ұ�
					ASTConditionalAndExpression con = (ASTConditionalAndExpression) ins.getFirstParentOfType(ASTConditionalAndExpression.class);
					if (con != null) {
						int k = 0;
						for (k = 0; k < con.jjtGetNumChildren(); k++) {
							SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
							if (javanode.getSingleChildofType(ASTInstanceOfExpression.class) == ins) {
								break;
							}
						}
						for (; k < con.jjtGetNumChildren(); k++) {
							SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
							if (usenode.isSelOrAncestor(javanode)) {
								beffect = false;
							}
						}
					}
				}
			}
			if (!beffect) {
				return false;
			}
		}

		{
			// ���name���Ƿ��·
			String str = ".//EqualityExpression/PrimaryExpression/PrimaryPrefix/Name[../../../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]";
			List evaluationResults = null;
			try {
				XPath xpath = new BaseXPath(str, new DocumentNavigator());
				evaluationResults = xpath.selectNodes(treenode);
			} catch (JaxenException e) {
				if (softtest.config.java.Config.DEBUG) {
					e.printStackTrace();
				}
				throw new RuntimeException("xpath error",e);
			}
			Iterator itor = evaluationResults.iterator();
			boolean beffect = true;
			while (itor.hasNext()) {
				ASTName temp = (ASTName) itor.next();
				if (temp.getNameDeclaration() == v&&!temp.getImage().contains(".")) {
					ASTEqualityExpression equal = (ASTEqualityExpression) temp.jjtGetParent().jjtGetParent().jjtGetParent();
					if (equal.getImage().equals("==")) {
						// ����Ƿ�Ϊ||���ұ�
						ASTConditionalOrExpression con = (ASTConditionalOrExpression) equal.getFirstParentOfType(ASTConditionalOrExpression.class);
						if (con != null) {
							int k = 0;
							for (k = 0; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (javanode.getSingleChildofType(ASTEqualityExpression.class) == equal) {
									break;
								}
							}
							for (; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (usenode.isSelOrAncestor(javanode)) {
									beffect = false;
								}
							}
						}
					} else {// !=���
						// ����Ƿ�Ϊ&&���ұ�
						ASTConditionalAndExpression con = (ASTConditionalAndExpression) equal.getFirstParentOfType(ASTConditionalAndExpression.class);
						if (con != null) {
							int k = 0;
							for (k = 0; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (javanode.getSingleChildofType(ASTEqualityExpression.class) == equal) {
									break;
								}
							}
							for (; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (usenode.isSelOrAncestor(javanode)) {
									beffect = false;
								}
							}
						}
					}
				}
			}
			if (!beffect) {
				return false;
			}
		}		
		return true;
	}	
	
	
	/**
	 * @param usenode NPDʹ�ô���Ӧ���﷨���ڵ�
	 * @param method ��Ҫ��鷽����Ӧ�ķ������
	 * @return �����NPD�����ʵؼ���ˣ��򷵻�false�����򷵻�true
	 */
	public static boolean checkNPDRetNotGuard(SimpleJavaNode usenode,Method method){
		VexNode vex=usenode.getCurrentVexNode();
		if(vex==null){
			return true;
		}
		SimpleNode treenode=vex.getTreeNode().getConcreteNode();
		if(treenode==null){
			return true;
		}
		// ����Ƿ��������ʽ
		{
			ASTConditionalExpression con = (ASTConditionalExpression) usenode.getFirstParentOfType(ASTConditionalExpression.class);
			if (con != null) {
				String str = ".//EqualityExpression/PrimaryExpression[../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral and PrimarySuffix]";
				List evaluationResults = null;
				try {
					if (con.jjtGetChild(0) instanceof ASTEqualityExpression) {
						str = "./PrimaryExpression[../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral and PrimarySuffix]";
					}
					XPath xpath = new BaseXPath(str, new DocumentNavigator());
					evaluationResults = xpath.selectNodes(con.jjtGetChild(0));
				} catch (JaxenException e) {
					if (softtest.config.java.Config.DEBUG) {
						e.printStackTrace();
					}
					throw new RuntimeException("xpath error",e);
				}
				if (evaluationResults.size() > 0) {
					ASTPrimaryExpression temp = (ASTPrimaryExpression) evaluationResults.get(0);
					Method lastmethod=temp.getLastMethod();
					if (method.equals(lastmethod)) {
						ASTEqualityExpression equal = (ASTEqualityExpression) temp.jjtGetParent();
						if (equal.getImage().equals("==")) {
							if (usenode.isSelOrAncestor((SimpleJavaNode) con.jjtGetChild(2))) {
								return false;
							}
						} else {
							if (usenode.isSelOrAncestor((SimpleJavaNode) con.jjtGetChild(1))) {
								return false;
							}
						}
					}
				}
			}
		}
		{
			String str = ".//InstanceOfExpression/PrimaryExpression[PrimarySuffix]";
			List evaluationResults = null;
			evaluationResults=treenode.findXpath(str);
			Iterator itor = evaluationResults.iterator();
			boolean beffect = true;
			while (itor.hasNext()) {
				ASTPrimaryExpression temp = (ASTPrimaryExpression) itor.next();
				
				Method lastmethod=temp.getLastMethod();
				if (method.equals(lastmethod)) {
					ASTInstanceOfExpression ins = (ASTInstanceOfExpression) temp.jjtGetParent();
					ASTConditionalAndExpression con = (ASTConditionalAndExpression) ins.getFirstParentOfType(ASTConditionalAndExpression.class);
					if (con != null) {
						int k = 0;
						for (k = 0; k < con.jjtGetNumChildren(); k++) {
							SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
							if (javanode.getSingleChildofType(ASTInstanceOfExpression.class) == ins) {
								break;
							}
						}
						for (; k < con.jjtGetNumChildren(); k++) {
							SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
							if (usenode.isSelOrAncestor(javanode)) {
								beffect = false;
							}
						}
					}
				}
			}
			
			if (!beffect) {
				return false;
			}
		}

		{
			// ���ʹ�ô��Ƿ��·
			String str = ".//EqualityExpression/PrimaryExpression[../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral and PrimarySuffix]";
			List evaluationResults = null;
			try {
				XPath xpath = new BaseXPath(str, new DocumentNavigator());
				evaluationResults = xpath.selectNodes(treenode);
			} catch (JaxenException e) {
				if (softtest.config.java.Config.DEBUG) {
					e.printStackTrace();
				}
				throw new RuntimeException("xpath error",e);
			}
			Iterator itor = evaluationResults.iterator();
			boolean beffect = true;
			while (itor.hasNext()) {
				ASTPrimaryExpression temp = (ASTPrimaryExpression) itor.next();
				
				Method lastmethod=temp.getLastMethod();
				if (method.equals(lastmethod)) {
					ASTEqualityExpression equal = (ASTEqualityExpression) temp.jjtGetParent();
					if (equal.getImage().equals("==")) {
						// ����Ƿ�Ϊ||���ұ�
						ASTConditionalOrExpression con = (ASTConditionalOrExpression) equal.getFirstParentOfType(ASTConditionalOrExpression.class);
						if (con != null) {
							int k = 0;
							for (k = 0; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (javanode.getSingleChildofType(ASTEqualityExpression.class) == equal) {
									break;
								}
							}
							for (; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (usenode.isSelOrAncestor(javanode)) {
									beffect = false;
								}
							}
						}
					} else {// !=���
						// ����Ƿ�Ϊ&&���ұ�
						ASTConditionalAndExpression con = (ASTConditionalAndExpression) equal.getFirstParentOfType(ASTConditionalAndExpression.class);
						if (con != null) {
							int k = 0;
							for (k = 0; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (javanode.getSingleChildofType(ASTEqualityExpression.class) == equal) {
									break;
								}
							}
							for (; k < con.jjtGetNumChildren(); k++) {
								SimpleJavaNode javanode = (SimpleJavaNode) con.jjtGetChild(k);
								if (usenode.isSelOrAncestor(javanode)) {
									beffect = false;
								}
							}
						}
					}
				}
			}
			if (!beffect) {
				return false;
			}
		}
		
		{
			//����Ƿ��������ж�
			String name=method.getName();
			ASTMethodDeclaration mdecl=(ASTMethodDeclaration) usenode.getFirstParentOfType(ASTMethodDeclaration.class);
			if(mdecl!=null){
				String str="matches(@Image,\'^((.+\\."+name+")|("+name+"))$\')";
				String xpath=".//IfStatement/Expression[//*["+str+"]]"+
				" | .//WhileStatement/Expression[//*["+str+"]]"+
				" | .//DoStatement/Expression[//*["+str+"]]";
				List evaluationResults=mdecl.findXpath(xpath);
				Iterator itor = evaluationResults.iterator();
				while (itor.hasNext()) {
					SimpleJavaNode connode=(SimpleJavaNode)itor.next();
					if(usenode.isSelOrAncestor(connode)){
						continue;
					}
					
					xpath=".//EqualityExpression/PrimaryExpression[../PrimaryExpression/PrimaryPrefix/Literal/NullLiteral and PrimarySuffix and //*["+str+"]";
					List list=connode.findXpath(xpath);
					Iterator i=list.iterator();
					while(i.hasNext()){
						ASTPrimaryExpression temp =(ASTPrimaryExpression)i.next();
						Method lastmethod=temp.getLastMethod();
						if (!method.equals(lastmethod)) {
							continue;
						}
						ASTEqualityExpression equal = (ASTEqualityExpression) temp.jjtGetParent();
						
						VexNode fromvex=null,tovex=usenode.getCurrentVexNode();
						VexNode tempvex=connode.getCurrentVexNode();
						if(tovex==null||tempvex==null){
							continue;
						}
						Edge te =null,fe=null;;
						for(Edge edge:tempvex.getOutedges().values()){
							if(edge.getName().startsWith("T_")){
								te=edge;
							}
							if(edge.getName().startsWith("F_")){
								fe=edge;
							}
						}
						if(te==null||fe==null){
							continue;
						}
						if (equal.getImage().equals("==")) {
							//�����֧�������ɴ���
							fromvex=te.getHeadNode();
							if(!Graph.checkVexReachable(fromvex,tovex,fe)){
								return false;
							}
						}else{
							//�Ӽٷ�֧�������ɴ���
							if (fe==null) {
								System.out.println(connode.getBeginLine()+":"+connode.getBeginColumn());
							}
							fromvex=fe.getHeadNode();
							if(!Graph.checkVexReachable(fromvex,tovex,te)){
								return false;
							}
						}
					}
				}
				
			}
		
		}
		return true;
	}	
}

