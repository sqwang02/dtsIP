package softtest.rules.java.fault.NPD;



import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.jaxen.JaxenException;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;
import softtest.domain.java.*;

public class NPDArrayStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空指针引用:  %d 行声明的变量 \'%s\' 在 %d 行可能导致一个空指针异常",
					beginline,fsmmi.getRelatedVariable().getImage(),errorline);
		}else{
			f.format("Null Pointer Dereference: the variable \'%s\' declared on line %d may cause a  NullPointerException at line %d.",
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
	
	private class SummaryVisitor extends JavaParserVisitorAdapter {
		@Override
		public Object visit(ExpressionBase node, Object data) {
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
						if(!node.hasLocalMethod(current_node)){
							current_use_var.put(v, new InfoPackage(node,list));
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
					if (!node.hasLocalMethod(current_node)){
						ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
						if(node.getImage().startsWith(classcope.getClassName()+".")){
							//过滤 类名.成员 情况
							break;
						}
						current_use_var.put(v, new NPDArrayStateMachine().new InfoPackage(node,new ArrayList<String>()));
					}
					//只处理第一个变量 为了修正p.q=null;情况的误报
					break;
				}
			}
			return null;
		}
		
	}
	private class InfoPackage{
		SimpleJavaNode node;
		List<String> list;
		public InfoPackage(SimpleJavaNode node, List<String> list) {
			this.node = node;
			this.list = list;
		}

	}
	
	private static SimpleJavaNode current_node=null;
	private static Hashtable<VariableNameDeclaration,InfoPackage> current_use_var=new Hashtable<VariableNameDeclaration,InfoPackage> ();
	private static void  refreshCurrentUseTable(SimpleJavaNode node){
		if(current_node==node){
			return;
		}
		current_node=node;
		current_use_var.clear();
		//处理隐式引用
		node.jjtAccept(new NPDArrayStateMachine().new SummaryVisitor(), null);	
		
		//处理显式引用
		/*String xPath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^.+\\.\')]";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			List<NameDeclaration> decllist = name.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (decl instanceof VariableNameDeclaration) {
					VariableNameDeclaration v = (VariableNameDeclaration) decl;
					if (!name.hasLocalMethod(current_node)){
						ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
						if(name.getImage().startsWith(classcope.getClassName()+".")){
							//过滤 类名.成员 情况
							break;
						}
						current_use_var.put(v, new NPDStateMachine().new InfoPackage(name,new ArrayList<String>()));
					}
					//只处理第一个变量 为了修正p.q=null;情况的误报
					break;
				}
			}
		}*/
	}   

	public static List<FSMMachineInstance> createNPDArrayStateMachines(SimpleJavaNode node, FSMMachine fsm)  {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List exp = null;
		refreshCurrentUseTable(node);
		for (Enumeration<VariableNameDeclaration> e = current_use_var.keys(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			ClassType domaintype = DomainSet.getDomainType(v.getDomain());
		
			
			
			if (domaintype == ClassType.ARRAY ) {
//				ASTVariableDeclaratorId vid = v.getDeclaratorId();
//				ASTVariableDeclarator vd=(ASTVariableDeclarator)vid.jjtGetParent();
				
//			    try {
//					exp= vd.findChildNodesWithXPath("./VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix");
//				} catch (JaxenException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				
//				Iterator itexp = exp.iterator();
//				while(itexp.hasNext()){
//					ASTPrimaryPrefix asexp=(ASTPrimaryPrefix)itexp.next();
//
//					if (asexp.getType() instanceof Method || asexp.getType() instanceof Constructor) {
//						      Object o=asexp.getType();
//						      MethodNode methodnode=MethodNode.findMethodNode(o);
//						      if(methodnode==null){
//							     continue;
//						      }

				ReferenceDomain refdomain2=(ReferenceDomain)ConvertDomain.DomainSwitch(v.getDomain(), ClassType.REF);
					
				if(refdomain2.getValue().toString().equals("NULL_OR_NOTNULL")||refdomain2.getValue().toString().equals("NULL")){

				  FSMMachineInstance fsminstance = fsm.creatInstance();
				  fsminstance.setRelatedVariable(v);
				  list.add(fsminstance);
			}
		}
				}
//			}
//		}
		return list;
	}
	
	public static boolean checkSameVariableAndMaybeNull(VexNode node, FSMMachineInstance fsmin) {
		if(node.isBackNode()){
			return false;
		}
		SimpleJavaNode treenode=(SimpleJavaNode)node.getTreeNode().getConcreteNode();
		if(treenode==null||(treenode instanceof ASTConstructorDeclaration)||(treenode instanceof ASTMethodDeclaration)){
			return false;
		}
		refreshCurrentUseTable(treenode);
		VariableNameDeclaration v=fsmin.getRelatedVariable();
		
		Object domain=node.getDomainWithoutNull(v);
		ClassType domaintype = DomainSet.getDomainType(domain);
		if (domaintype != ClassType.ARRAY ) {
			return false;
		}
		
		if(((ArrayDomain)domain).getvalue().getUnknown()||((ArrayDomain)domain).getvalue().getValue().equals(ReferenceValue.NOTNULL)||((ArrayDomain)domain).getvalue().getValue().equals(ReferenceValue.EMPTY)){
			return false;
		}
		
		if(!current_use_var.containsKey(v)){
			return false;
		}
		
		SimpleJavaNode usenode=current_use_var.get(v).node;
			
		if (!NpdPrecondition.checkNPDNotGuard(usenode, v)) {
			return false;
		}
		
		List<String> list=current_use_var.get(v).list;
		StringBuffer traceinfo=new StringBuffer();
		for(String s:list){
			traceinfo.append(s);
		}
		fsmin.setTraceinfo(traceinfo.toString());
		
		return true;
	}
}
