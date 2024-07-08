package softtest.callgraph.java.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.DomainSet;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.config.java.Config;
import softtest.domain.java.ArrayDomain;
import softtest.domain.java.ConvertDomain;
import softtest.domain.java.IntegerDomain;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.MethodScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**OOB前置条件*/
public class OOBPrecondition extends AbstractPrecondition{
	
	/* (non-Javadoc)
	 * @see softtest.callgraph.java.method.AbstractPrecondition#listen(softtest.cfg.java.VexNode, softtest.callgraph.java.method.PreconditionSet)
	 */
	@Override
	public void listen(SimpleJavaNode node,PreconditionSet set) {
		
		OOBPreconditionVisitor vsitor=new OOBPreconditionVisitor();
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
	private class OOBPreconditionVisitor extends JavaParserVisitorAdapter{
		SimpleJavaNode root;
		HashSet<VariableNameDeclaration> set = new HashSet<VariableNameDeclaration>();
	 
		@Override
		public Object visit(ASTName node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			List<NameDeclaration> decllist = node.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (decl instanceof VariableNameDeclaration) {
					VariableNameDeclaration v = (VariableNameDeclaration) decl;
						// 检察是否越界
						Object domain = node.findCurrentDomain(v);
						if (domain instanceof ArrayDomain) {
							ArrayDomain a = (ArrayDomain) domain;
							ASTPrimaryExpression priexpr = (ASTPrimaryExpression) node.jjtGetParent().jjtGetParent();
							List suffixlist = priexpr.findDirectChildOfType(ASTPrimarySuffix.class);
							Iterator siter = suffixlist.iterator();
							while (siter.hasNext()) {
								ASTPrimarySuffix suffix = (ASTPrimarySuffix) siter.next();
								if (!suffix.isArrayDereference()) {
									continue;
								}
								ASTName name = (ASTName) suffix.getFirstChildrenOfType(ASTName.class);
								if(name == null) {
									continue;
								}
								VexNode vex = node.getCurrentVexNode();
								DomainSet old = vex.getDomainSet();
								vex.setDomainSet(vex.getLastDomainSet());
								DomainData expdata = new DomainData();
								expdata.sideeffect = false;
								// 计算下标表达式
								SimpleJavaNode indexexp = (SimpleJavaNode) suffix.jjtGetChild(0);
								indexexp.jjtAccept(new ExpressionDomainVisitor(), expdata);
								vex.setDomainSet(old);

								IntegerDomain idomain = (IntegerDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
								if(idomain.getUnknown()) {
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
		            				if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
		            					continue;
		            				}
		            				//Config.OOB_PRE_INSTANCE_COUNT++;
		            				MapOfVariable mapOfVariable = new MapOfVariable((VariableNameDeclaration)name.getNameDeclaration());
		            				mapOfVariable.setArrayLimit(a.getDimension(0).jointoOneInterval().getMax());
		            				table.put(mapOfVariable, list);
		            				set.add(v);
								}
							}
						}
				}
			}
			return null;
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
					if (!(pre instanceof OOBPrecondition)) {
						continue;
					}
					OOBPrecondition oobpre = (OOBPrecondition) pre;
					Hashtable<VariableNameDeclaration,List<String>> usedvar = oobpre.check(last);
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
						MapOfVariable mapOfVariable = new MapOfVariable(v);
						for(Enumeration<MapOfVariable> en=oobpre.getTable().keys();en.hasMoreElements();){
							MapOfVariable map=en.nextElement();

							VariableNameDeclaration ret=null;
							
							if (map.getIndex() != -1) {
								if(node!=null&&node.jjtGetNumChildren()>0&&map.getIndex()<node.jjtGetChild(0).jjtGetNumChildren()){
									ASTName name=(ASTName)((SimpleJavaNode)(node.jjtGetChild(0).jjtGetChild(map.getIndex()))).getSingleChildofType(ASTName.class);
									if(name!=null&&name.getNameDeclaration() instanceof VariableNameDeclaration){
										ret=(VariableNameDeclaration)name.getNameDeclaration();
									}
								}
							}
							if(ret == v) {
								mapOfVariable.setArrayLimit(map.getArrayLimit());
								break;
							}
						}
						table.put(mapOfVariable, newlist);
						set.add(v);
					}
				}
			}
			return super.visit(node, data);
		}
		
	}
		

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer("OOB-Precondition:");
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

