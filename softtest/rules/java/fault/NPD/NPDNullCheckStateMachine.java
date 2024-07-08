package softtest.rules.java.fault.NPD;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;
import softtest.fsm.java.*;
import softtest.rules.java.*;
import softtest.symboltable.java.*;

public class NPDNullCheckStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空指针引用:%d 行上声明的变量 \'%s\' 在 %d 行可能导致一个空指针引用异常,"
					, beginline,fsmmi.getRelatedVariable().getImage(),errorline);
		}else{
			f.format("Null Pointer Dereference: the variable \'%s\' declared on line %d is checked on line %d,"+
					"but the check is incorrect.", fsmmi.getRelatedVariable().getImage(),beginline,errorline);
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
	
	public static List<FSMMachineInstance> createNPDNullCheckStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//EqualityExpression[./PrimaryExpression/PrimaryPrefix/Literal/NullLiteral and (parent::ConditionalOrExpression or parent::ConditionalAndExpression)]/PrimaryExpression/PrimaryPrefix/Name";
		List evaluationResults = null;
		evaluationResults = node.findXpath(xPath);
		
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name=(ASTName)i.next();
			String image=name.getImage();
			if(image==null||image.contains(".")){
				continue;
			}
			if(name.getCurrentVexNode()==null){
				continue;
			}
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration)){
				continue;
			}
			
			if(((SimpleJavaNode)name.jjtGetParent()).getNextSibling()!=null){
				continue;
			}
			
			VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
			
			ASTEqualityExpression equal=(ASTEqualityExpression)name.getFirstParentOfType(ASTEqualityExpression.class);
			SimpleJavaNode parent=(SimpleJavaNode)equal.jjtGetParent();
			while((parent.jjtGetParent() instanceof ASTConditionalOrExpression)||(parent.jjtGetParent() instanceof ASTConditionalAndExpression)){
				parent=(SimpleJavaNode)parent.jjtGetParent();
			}
			NpdVisitor visitor=new NPDNullCheckStateMachine().new NpdVisitor(v);
			parent.jjtAccept(visitor, null);
			if(visitor.traceinfo!=null){
				if(!NpdPrecondition.checkNPDNotGuard(visitor.traceinfo.node, v)){
					continue;
				}
				FSMMachineInstance fsminstance = fsm.creatInstance();
				list.add(fsminstance);
				fsminstance.setRelatedObject(new FSMRelatedCalculation(visitor.traceinfo.node));
				fsminstance.setRelatedVariable(v);

				StringBuffer traceinfo = new StringBuffer();
				for (String s : visitor.traceinfo.list) {
					traceinfo.append(s);
				}
				fsminstance.setTraceinfo(traceinfo.toString());
			}
		}
		
		return list;
	}

}
