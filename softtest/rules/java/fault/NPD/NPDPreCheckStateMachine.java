package softtest.rules.java.fault.NPD;

import java.lang.reflect.*;
import java.util.*;

import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;
import softtest.domain.java.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;


public class NPDPreCheckStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空指针引用:在 %d 行声明的变量  \'%s\' 在 %d 行可能导致一个空指针异常，因为该变量在当前函数的其他地方进行了空指针检查，这暗示了它可能为null",
					beginline,fsmmi.getRelatedVariable().getImage(),errorline);
		}else{
			f.format("Null Pointer Dereference: the variable \'%s\' declared on line %d is checked for null"+
				" elsewhere and the check implys that it may cause a NullPointerException at line %d.",
				fsmmi.getRelatedVariable().getImage(),beginline,errorline);
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
					//只处理第一个变量 为了修正p.q=null;情况的误报
					break;
				}
			}
			return null;
		}
		
	}

	public static List<FSMMachineInstance> createNPDPreCheckStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//EqualityExpression[./PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]/PrimaryExpression/PrimaryPrefix/Name";
		List evaluationResults = null;
		evaluationResults = node.findXpath(xPath);
		
		Hashtable<VariableNameDeclaration,HashSet<VexNode>> varset=new Hashtable<VariableNameDeclaration,HashSet<VexNode>>();
		HashSet<VexNode> vexset=null;
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name=(ASTName)i.next();
			String image=name.getImage();
			if(image==null||image.contains(".")){
				continue;
			}
			
			//if(name.getFirstParentOfType(ASTFinallyStatement.class)!=null){
			//	continue;
			//}
			
			if(((SimpleJavaNode)name.jjtGetParent()).getNextSibling()!=null){
				continue;
			}
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration)){
				continue;
			}
			VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
			VexNode vex=name.getCurrentVexNode();
			
			if(varset.containsKey(v)){
				vexset=varset.get(v);
			}else{
				vexset=new HashSet<VexNode>();
				varset.put(v, vexset);
			}
			if(vex!=null){
				vexset.add(vex);
			}
			if(name.getFirstParentOfType(ASTFinallyStatement.class)!=null){
				List<VexNode> vexlist=name.getCurrentVexList();
				if(vexlist!=null){
					for(VexNode vex1:vexlist){
						if(!vex1.isBackNode()){
							vexset.add(vex1);
						}
					}
				}
			}
		}
		
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name=(ASTName)i.next();
			String image=name.getImage();
			if(image==null||image.contains(".")){
				continue;
			}
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration)){
				continue;
			}
			
			if(((SimpleJavaNode)name.jjtGetParent()).getNextSibling()!=null){
				continue;
			}
			
			if(name.getFirstParentOfType(ASTFinallyStatement.class)!=null){
				continue;
			}
			
			VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
			vexset=varset.get(v);
			VexNode vex=name.getCurrentVexNode();
		
			if(vex==null|| DomainSet.getDomainType(v.getDomain())!=ClassType.REF || !ConvertDomain.DomainIsUnknown(vex.getDomainWithoutNull(v))){
				continue;
			}
						
			NameOccurrence occ=vex.findOccurrence(name);
			if(occ==null || occ.getOccurrenceType()==NameOccurrence.OccurrenceType.DEF || !occ.getUseDefList().isEmpty()){
				continue;
			}
			
			Graph g = vex.getGraph();
			
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
					
					if(vexset!=null&&vexset.contains(headnode)){
						continue;
					}
					
					for(NameOccurrence o:headnode.findOccurrenceOfVarDecl(v)){
						if(o.getOccurrenceType()==NameOccurrence.OccurrenceType.DEF){
							continue for1;
						}
					}
					
					SimpleJavaNode treenode=(SimpleJavaNode)headnode.getTreeNode().getConcreteNode();
					if(!headnode.isBackNode()&&treenode!=null&&!(treenode instanceof ASTConstructorDeclaration)&&!(treenode instanceof ASTMethodDeclaration)){
						NpdVisitor visitor=new NPDPreCheckStateMachine().new NpdVisitor(v);
						treenode.jjtAccept(visitor, null);
						if(visitor.traceinfo!=null){
							find=treenode;
							traces =visitor.traceinfo.list;
							break out;
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
