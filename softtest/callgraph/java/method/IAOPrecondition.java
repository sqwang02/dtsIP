package softtest.callgraph.java.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainSet;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.domain.java.IntegerDomain;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.MethodScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**IAO前置条件*/
public class IAOPrecondition extends AbstractPrecondition{
	
	/* (non-Javadoc)
	 * @see softtest.callgraph.java.method.AbstractPrecondition#listen(softtest.cfg.java.VexNode, softtest.callgraph.java.method.PreconditionSet)
	 */
	@Override
	public void listen(SimpleJavaNode node,PreconditionSet set) {
		
		IAOPreconditionVisitor vsitor=new IAOPreconditionVisitor();
		vsitor.root=node;
		node.jjtAccept(vsitor, null);
		
		if(table.size()>0){
			set.addPreconditon(this);
		}
	}
	

	/**
	 * 处理当前前置条件，使用前置条件代替函数调用
	 * @param node node 具有Mehtod类型或Constructor类型的表达式节点，通常可能是ASTPrimarySuffix或者ASTPrimaryPrefix或者ASTAllocationExpression
	 * @return 在函数调用过程中会被直接引用的实际参数哈希表，key为实参变量，value为跟踪的函数调用trace信息
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
	 * 处理语法树节点，检查函数内部对引用变量的直接使用p.f()，同时也考虑深层函数调用关系f(p,a)其中f
	 * 摘要中存在对p的直接使用
	 */
	private class IAOPreconditionVisitor extends JavaParserVisitorAdapter{
		SimpleJavaNode root;
		HashSet<VariableNameDeclaration> set = new HashSet<VariableNameDeclaration>();
	 
		@Override
		public Object visit(ASTName node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			return null;
		}

		@Override
		public Object visit(ASTMultiplicativeExpression node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			if(node.getImage().equals("/") || node.getImage().equals("%")) {
            	List names = ((SimpleJavaNode)node.jjtGetChild(1)).findXpath(".//Name");
            	if (null != names) {
            		for(int i=0; i<names.size(); i++) {
            			ASTName name = (ASTName) names.get(i);
            			List<NameDeclaration> decllist = name.getNameDeclarationList();
            			Iterator<NameDeclaration> decliter = decllist.iterator();
            			MethodScope methodscope=node.getScope().getEnclosingMethodScope();
            out:		while (decliter.hasNext()) {
            				NameDeclaration decl = decliter.next();
            				if (!(decl instanceof VariableNameDeclaration)){
            					//检查是否为变量
            					continue;
            				}
            				VariableNameDeclaration v = (VariableNameDeclaration) decl;
            				
            				ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
            				if(set.contains(v)){
            					//检查是否已经添加过
            					continue;
            				}
            				//检查是否为 成员或函数参数变量
            				//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
            				if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//            				if(methodscope!=v.getDeclareScope()){
            					//检查是否为当前函数参数
            					continue;
            				}
            				VexNode vex=node.getCurrentVexNode();
            				if(vex==null){
            					//检查当前控制流图节点是否为空
            					continue;
            				}
            				
            				if(vex.getContradict()){
            					//不可达节点
            					continue;
            				}
            				
            				Object domain = vex.getDomainWithoutNull(v);
            				if (domain == null || DomainSet.getDomainType(domain) != ClassType.INT) {
            					//检查是否为 引用型变量
            					continue;
            				}
            				
            				IntegerDomain intdomain=(IntegerDomain)domain;
            				if(!intdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
            					continue;
            				}
            				
            				for(NameOccurrence occ:vex.getOccurrences()){
            					if(occ.getDeclaration()==v){
            						if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
            							continue out;
            						}
            					}
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
            					list.add(0,"文件:"+ProjectAnalysis.current_file+" 行:"+node.getBeginLine()+" 方法:"+methodname+"\n");
            				}else{
            					list.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
            				}
            				table.put(new MapOfVariable(v), list);
            				set.add(v);
            			}
            		}
            	}
            }
			return super.visit(node, data);
		}
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
					if (!(pre instanceof IAOPrecondition)) {
						continue;
					}
					IAOPrecondition iaopre = (IAOPrecondition) pre;
					Hashtable<VariableNameDeclaration,List<String>> usedvar = iaopre.check(last);
					Iterator<Map.Entry<VariableNameDeclaration,List<String>>> i=usedvar.entrySet().iterator();
	out:			while (i.hasNext()) {
						Map.Entry<VariableNameDeclaration,List<String>> entry=i.next();
						VariableNameDeclaration v=entry.getKey();
						List<String> list=entry.getValue();
							
						if(set.contains(v)){
							//检查是否已经添加过
							continue;
						}
						MethodScope methodscope=node.getScope().getEnclosingMethodScope();
						//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
						if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//						if(methodscope!=v.getDeclareScope()){
							//检查是否为当前函数参数
							continue;
						}
							
						VexNode vex=node.getCurrentVexNode();
						if(vex==null){
							//检查当前控制流图节点是否为空
							continue;
						}
							
						if(vex.getContradict()){
							//不可达节点
							continue;
						}
							
							
						Object domain = vex.getDomainWithoutNull(v);
						if (domain == null || DomainSet.getDomainType(domain) != ClassType.INT) {
							//检查是否为 引用型变量
							continue;
						}
						IntegerDomain intdomain=(IntegerDomain)domain;
						if(!intdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
							continue;
						}
														
						for(NameOccurrence occ:vex.getOccurrences()){
							if(occ.getDeclaration()==v){
								if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
									continue out;
								}
							}
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
							newlist.add(0,"文件:"+ProjectAnalysis.current_file+" 行:"+node.getBeginLine()+" 方法:"+methodname+"\n");
						}else{
							newlist.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
						}
						//Config.OOB_PRE_INSTANCE_COUNT++;
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
		StringBuffer buff=new StringBuffer("IAO-Precondition:");
		Iterator<Map.Entry<MapOfVariable,List<String>>> i=table.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,List<String>> entry=i.next();
			MapOfVariable m=entry.getKey();
			List<String> list=entry.getValue();
			buff.append("("+m.toString()+")");
		}	
		return buff.toString();
	}
}

