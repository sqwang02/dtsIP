package softtest.rules.java.fault.NPD;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.*;
import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.AbstractPrecondition;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.NpdPrecondition;
import softtest.callgraph.java.method.NpdPreconditionListener;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.domain.java.ReferenceDomain;
import softtest.domain.java.ReferenceValue;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class NPDParamStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空指针引用： %d 行上，\'%s\' 可能为null，可能导致一个空指针异常",errorline,fsmmi.getResultString());
		}else{
			f.format("Null Pointer Dereference: \'%s\' on line %d should not be null.", fsmmi.getResultString(),errorline);
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

	

	public static List<FSMMachineInstance> createNPDParamStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//Arguments[count(ArgumentList)=1]";
		List evaluationResults = null;
		evaluationResults = node.findXpath(xPath);
		
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTArguments arguments=(ASTArguments)i.next();
			Object type=null;
			if(arguments.jjtGetParent() instanceof ASTPrimarySuffix){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)arguments.jjtGetParent();
				ExpressionBase e=suffix.getLastExpression();
				type=e.getType();
			}else if(arguments.jjtGetParent() instanceof ASTAllocationExpression){
				ASTAllocationExpression allo=(ASTAllocationExpression)arguments.jjtGetParent();
				type=allo.getType();
			}

			if(!(type instanceof Method||type instanceof Constructor)){
				continue;
			}
			MethodNode mn=MethodNode.findMethodNode(type);
			if(mn==null){
				continue;
			}
			MethodSummary summary = mn.getMethodsummary();
			if (summary == null) {
				continue;
			}

			for (AbstractPrecondition pre : summary.getPreconditons().getTable().values()) {
				if (!(pre instanceof NpdPrecondition)) {
					continue;
				}
				NpdPrecondition npdpre = (NpdPrecondition) pre;
				for(Enumeration<MapOfVariable> en=npdpre.getTable().keys();en.hasMoreElements();){
					MapOfVariable v=en.nextElement();
					if(v.getIndex()>=0 && v.getIndex()<arguments.jjtGetChild(0).jjtGetNumChildren()){						
						SimpleJavaNode n=(SimpleJavaNode)arguments.jjtGetChild(0).jjtGetChild(v.getIndex());
						ASTName name=(ASTName)n.getSingleChildofType(ASTName.class);
						//过滤NPDStateMachine中已经考虑过的情况 f(p)
						if(name==null){
							DomainData expdata = new DomainData();
							expdata.sideeffect = false;
							n.jjtAccept(new ExpressionDomainVisitor(), expdata);
							if(expdata.type==ClassType.REF){
								ReferenceDomain ref=(ReferenceDomain)expdata.domain;
								if(!ref.getUnknown()&&
										(ref.getValue()==ReferenceValue.NULL||ref.getValue()==ReferenceValue.NULL_OR_NOTNULL)){
									ASTPrimaryExpression p=(ASTPrimaryExpression)n.getSingleChildofType(ASTPrimaryExpression.class);
									
									if(p!=null){
										Method method=p.getLastMethod();
										if(method!=null&&!NpdPrecondition.checkNPDRetNotGuard(p, method)){
											continue;
										}
									}
									
									StringBuffer traceinfo=new StringBuffer();
									for(String s:npdpre.getTable().get(v)){
										traceinfo.append(s);
									}
									
									FSMMachineInstance fsminstance = fsm.creatInstance();
									list.add(fsminstance);
									fsminstance.setRelatedObject(new FSMRelatedCalculation(n));
									String str="";
									if(type  instanceof Method){
										if(softtest.config.java.Config.LANGUAGE==0){
											str="方法 "+((Method)type).getName();
										}else{
											str=" of method "+((Method)type).getName();
										}
									}else{
										if(softtest.config.java.Config.LANGUAGE==0){
											str="构造方法 "+((Constructor)type).getName();
										}else{
											str=" of constructor "+((Constructor)type).getName();
										}
									}
									if(softtest.config.java.Config.LANGUAGE==0){
										str=str+" 的第 "+(v.getIndex()+1)+" 个参数";
									}else{
										str="Parameter "+ (v.getIndex()+1) +str;
									}
									fsminstance.setResultString(str);
									fsminstance.setTraceinfo(traceinfo.toString());
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

}
