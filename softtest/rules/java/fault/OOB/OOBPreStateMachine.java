package softtest.rules.java.fault.OOB;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.AbstractPrecondition;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.IAOPrecondition;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.OOBPrecondition;
import softtest.callgraph.java.method.OOBPreconditionListener;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.domain.java.IntegerDomain;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class OOBPreStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("数组越界:  %d 行上数组 \'%s\' 的下标可能超过其合法的长度导致一个数组越界异常", errorline,fsmmi.getResultString());
		}else{
			f.format("Out of Bound: the index of array \'%s\' at line %d can be greater than its size.", fsmmi.getResultString(),errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		listeners.addListener(OOBPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}

	

	public static List<FSMMachineInstance> createOOBPreStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//Arguments";
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

			if(!(type instanceof Method)){
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
				if (!(pre instanceof OOBPrecondition)) {                                                                      
					continue;
				}
				OOBPrecondition oobpre = (OOBPrecondition) pre;
				for(Enumeration<MapOfVariable> en=oobpre.getTable().keys();en.hasMoreElements();){
					MapOfVariable v=en.nextElement();
					if(v.getIndex()>=0 && v.getIndex()<arguments.jjtGetChild(0).jjtGetNumChildren()){						
						SimpleJavaNode n=(SimpleJavaNode)arguments.jjtGetChild(0).jjtGetChild(v.getIndex());
						ASTName name=(ASTName)n.getSingleChildofType(ASTName.class);
						//检查f(Literal)的情况
						if(name==null){
							DomainData expdata = new DomainData();
							expdata.sideeffect = false;
							n.jjtAccept(new ExpressionDomainVisitor(), expdata);
							if(expdata.type==ClassType.INT){
								IntegerDomain intDomain =(IntegerDomain)expdata.domain;
								
								if(!intDomain.getUnknown() && intDomain.jointoOneInterval().getMax() >= v.getArrayLimit()){
									StringBuffer traceinfo=new StringBuffer();
									for(String s:oobpre.getTable().get(v)){
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
							
						} else {//检查f(i)的情况
							NameDeclaration dec =  name.getNameDeclaration();
							if(dec instanceof VariableNameDeclaration) {
								Object domain = ((VariableNameDeclaration)dec).getDomain();
								if(domain instanceof IntegerDomain){
									IntegerDomain intDomain =(IntegerDomain)domain;
									if(!intDomain.getUnknown() && intDomain.jointoOneInterval().getMax() >= v.getArrayLimit()){
										StringBuffer traceinfo=new StringBuffer();
										for(String s:oobpre.getTable().get(v)){
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
						// 检查f()的情况
					} else if (v.getIndex() < 0) {
						VariableNameDeclaration dec = v.findVariable(null);
						if (null != dec) {
							Object domain = ((VariableNameDeclaration) dec)
									.getDomain();
							if (domain instanceof IntegerDomain) {
								IntegerDomain intDomain = (IntegerDomain) domain;
								if (/*intDomain.getUnknown()
										|| */!intDomain.getUnknown()
										&& intDomain.jointoOneInterval().getMax() >= v.getArrayLimit()) {
									StringBuffer traceinfo = new StringBuffer();
									for (String s : oobpre.getTable().get(v)) {
										traceinfo.append(s);
									}
									FSMMachineInstance fsminstance = fsm
											.creatInstance();
									list.add(fsminstance);
									fsminstance
											.setRelatedObject(new FSMRelatedCalculation(
													arguments));
									String str = "";
									if (type instanceof Method) {
										if (softtest.config.java.Config.LANGUAGE == 0) {
											str = "方法 "
													+ ((Method) type).getName();
										} else {
											str = " of method "
													+ ((Method) type).getName();
										}
									}
									fsminstance.setResultString(str);
									fsminstance.setTraceinfo(traceinfo
											.toString());
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
