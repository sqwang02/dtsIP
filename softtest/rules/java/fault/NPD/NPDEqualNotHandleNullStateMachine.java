package softtest.rules.java.fault.NPD;

import java.lang.reflect.*;
import java.util.*;

import softtest.domain.java.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;
import softtest.fsm.java.*;
import softtest.rules.java.*;

import softtest.symboltable.java.*;

public class NPDEqualNotHandleNullStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空指针引用: %d 行上定义的equals()方法参数  \'%s\' ,对其为null的情形并没有进行合适地处理，在 %d 行可能造成一个空指针引用异常", beginline,fsmmi.getResultString(),errorline);
		}else{
			f.format("Null Pointer Dereference: the parameter \'%s\' of method \'equals()\' "+
				"declared on line %d is not handled properly,"+
				"and may cause a NullPointerException at line %d.", fsmmi.getResultString(),beginline,errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		listeners.addListener(NpdPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	private class InfoPackage{
		SimpleJavaNode node;
		List<String> list;
		public InfoPackage(SimpleJavaNode node, List<String> list) {
			this.node = node;
			this.list = list;
		}

	}
	
	private class NpdVisitor extends JavaParserVisitorAdapter {
	
		VariableNameDeclaration v=null;
		
		InfoPackage traceinfo=null;
		
		
		public NpdVisitor(VariableNameDeclaration v){
			this.v=v;
		}
		
		@Override
		public Object visit(ExpressionBase node, Object data) {
			if(traceinfo !=null){
				return null;
			}
			if (node.getType() == null || node.getType() instanceof Class) {
				return super.visit(node, data);
			}
			
			if (node.getType() instanceof Method || node.getType() instanceof Constructor) {
				Object o=node.getType();
				MethodNode methodnode=MethodNode.findMethodNode(o);
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
					Hashtable<VariableNameDeclaration,List<String>> usedvar = npdpre.check(node);
					Iterator<Map.Entry<VariableNameDeclaration,List<String>>> i=usedvar.entrySet().iterator();
					while (i.hasNext()) {
						Map.Entry<VariableNameDeclaration,List<String>> entry=i.next();
						VariableNameDeclaration v=entry.getKey();
						List<String> list=entry.getValue();
						if(this.v==v){
							traceinfo=new InfoPackage(node,list);
							return null;
						}
						if(!(v.getDeclareScope() instanceof LocalScope)){
							continue;
						}
						VexNode vex=node.getCurrentVexNode();
						
						if(!ConvertDomain.DomainIsUnknown(vex.getDomainWithoutNull(v))){
							continue;
						}

						for(NameOccurrence occ:vex.findOccurrenceOfVarDecl(v)){
							if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.USE){
								List<NameOccurrence> occs=occ.getUseDefList();
								if(occs.isEmpty()){
									if(v.getNode() instanceof ASTVariableDeclaratorId){
										ASTVariableDeclaratorId id=(ASTVariableDeclaratorId)v.getNode();
										SimpleJavaNode temp=(SimpleJavaNode)id.getNextSibling();
										if(temp!=null){
											ASTName name=(ASTName)temp.getSingleOrCastChildofType(ASTName.class);
											if(name!=null&&name.getNameDeclaration() == this.v){
												traceinfo=new InfoPackage(node,list);
												return null;
											}
										}
									}
								}
								for(NameOccurrence def:	occs){
									if(def.getLocation().jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression){
										ASTPrimaryExpression prim=(ASTPrimaryExpression)def.getLocation().jjtGetParent().jjtGetParent();
										SimpleJavaNode temp=(SimpleJavaNode)prim.getNextSibling();
										if(temp instanceof ASTAssignmentOperator){
											ASTAssignmentOperator oper=(ASTAssignmentOperator)prim.getNextSibling();
											if(oper.getImage().equals("=")){
												temp=(SimpleJavaNode)oper.getNextSibling();
												ASTName name=(ASTName)temp.getSingleOrCastChildofType(ASTName.class);
												if(name!=null&&name.getNameDeclaration() == this.v){
													traceinfo=new InfoPackage(node,list);
													return null;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			return super.visit(node, data);
		}
		
		public Object visit(ASTName node, Object data) {
			//处理显式引用
			ASTExpression forexp=(ASTExpression)node.getSingleParentofType(ASTExpression.class);
			boolean isforeach=false;
			if(forexp!=null&&forexp.getPrevSibling()instanceof ASTForEachVariableDeclaration){
				isforeach=true;
			}
			if(!node.getImage().contains(".")&&!isforeach){
				//名字中没有包含引用符号
				return null;
			}
			List<NameDeclaration> decllist = node.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (decl instanceof VariableNameDeclaration) {
					VariableNameDeclaration v = (VariableNameDeclaration) decl;
					if(this.v==v){
						traceinfo=new InfoPackage(node,new ArrayList<String>());
						return null;
					}
					if(!(v.getDeclareScope() instanceof LocalScope)){
						continue;
					}
					
					VexNode vex=node.getCurrentVexNode();

					if(!ConvertDomain.DomainIsUnknown(vex.getDomainWithoutNull(v))){
						continue;
					}
					
					for(NameOccurrence occ:vex.findOccurrenceOfVarDecl(v)){
						if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.USE){
							List<NameOccurrence> occs=occ.getUseDefList();
							if(occs.isEmpty()){
								if(v.getNode() instanceof ASTVariableDeclaratorId){
									ASTVariableDeclaratorId id=(ASTVariableDeclaratorId)v.getNode();
									SimpleJavaNode temp=(SimpleJavaNode)id.getNextSibling();
									if(temp!=null){
										ASTName name=(ASTName)temp.getSingleOrCastChildofType(ASTName.class);
										if(name!=null&&name.getNameDeclaration() == this.v){
											traceinfo=new InfoPackage(node,new ArrayList<String>());
											return null;
										}
									}
								}
							}
							for(NameOccurrence def:	occs){
								if(def.getLocation().jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression){
									ASTPrimaryExpression prim=(ASTPrimaryExpression)def.getLocation().jjtGetParent().jjtGetParent();
									SimpleJavaNode temp=(SimpleJavaNode)prim.getNextSibling();
									if(temp instanceof ASTAssignmentOperator){
										ASTAssignmentOperator oper=(ASTAssignmentOperator)prim.getNextSibling();
										if(oper.getImage().equals("=")){
											temp=(SimpleJavaNode)oper.getNextSibling();
											ASTName name=(ASTName)temp.getSingleOrCastChildofType(ASTName.class);
											if(name!=null&&name.getNameDeclaration() == this.v){
												traceinfo=new InfoPackage(node,new ArrayList<String>());
												return null;
											}
										}
									}
								}
							}
						}
					}
					
					
					//只处理第一个变量 为了修正p.q=null;情况的误报
					break;
				}
			}
			return null;
		}
		
	}
	
	public static List<FSMMachineInstance> createEqualNotHandleNullStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//MethodDeclaration[./ResultType/Type/PrimitiveType[@Image=\'boolean\'] and ./MethodDeclarator[@Image=\'equals\']/FormalParameters[count(./FormalParameter)=1 and ./FormalParameter/Type//ReferenceType/ClassOrInterfaceType[@Image=\'Object\']]]";
		List evaluationResults = null;
		evaluationResults = node.findXpath(xPath);
		
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTMethodDeclaration m=(ASTMethodDeclaration)i.next();
			if(!(m.getScope() instanceof MethodScope)){
				continue;
			}
			VariableNameDeclaration v=null;
			MethodScope scope=(MethodScope)m.getScope();
			for(Object o:scope.getVariableDeclarations().keySet()){
				v=(VariableNameDeclaration)o;
				break;
			}
			if(v==null){
				continue;
			}
			
			Graph g = m.getGraph();
			if(g==null){
				continue;
			}
			
			VexNode entry=g.getEntryNode();
			Stack<VexNode> stack = new Stack<VexNode>();
			HashSet<VexNode> table=new HashSet<VexNode>();
			stack.push(entry);
			table.add(entry);		
			
			SimpleJavaNode find=null;
			List<String> traces=null;
			
	out:	while (!stack.isEmpty()) {
				VexNode n=stack.pop();
				for1:for (Enumeration<Edge> e = n.getOutedges().elements(); e.hasMoreElements();) {
					Edge edge = e.nextElement();
					VexNode headnode=edge.getHeadNode();
					
					if(!ConvertDomain.DomainIsUnknown(headnode.getDomainWithoutNull(v))){
						continue;
					}
										
					for(NameOccurrence o:headnode.findOccurrenceOfVarDecl(v)){
						if(o.getOccurrenceType()==NameOccurrence.OccurrenceType.DEF){
							continue for1;
						}
					}
					
					SimpleJavaNode treenode=(SimpleJavaNode)headnode.getTreeNode().getConcreteNode();
					if(!headnode.isBackNode()&&treenode!=null&&!(treenode instanceof ASTConstructorDeclaration)&&!(treenode instanceof ASTMethodDeclaration)){
						NpdVisitor visitor=new NPDEqualNotHandleNullStateMachine().new NpdVisitor(v);
						treenode.jjtAccept(visitor, null);
						if(visitor.traceinfo!=null){
							if(NpdPrecondition.checkNPDNotGuard(visitor.traceinfo.node, v)){
								find=treenode;
								traces =visitor.traceinfo.list;
								break out;
							}
						}
					}		
										
					if (!table.contains(headnode)) {
						table.add(headnode);
						stack.push(headnode);
					}
				}
			}
			
			if (find != null) {

				FSMMachineInstance fsminstance = fsm.creatInstance();
				list.add(fsminstance);
				fsminstance.setRelatedObject(new FSMRelatedCalculation(find));
				fsminstance.setRelatedVariable(v);

				StringBuffer traceinfo = new StringBuffer();
				for (String s : traces) {
					traceinfo.append(s);
				}
				fsminstance.setTraceinfo(traceinfo.toString());
			}
			
		}
		
		return list;
	}
}