package softtest.rules.java.fault.IAO;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sun.codemodel.internal.JJavaName;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.DomainVexVisitor;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.AbstractPrecondition;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.IAOPrecondition;
import softtest.callgraph.java.method.IAOPreconditionListener;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.VexNode;
import softtest.domain.java.Domain;
import softtest.domain.java.IntegerDomain;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class IAOPreStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("非法操作:  %d 行的表达式 \'%s\' 可能存在一个非法操作",errorline, fsmmi.getResultString());
		}else{
			f.format("Illegal Arithmetic Operation: the operand of expression \'%s\' at line %d may be illegal.", fsmmi.getResultString(),errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		listeners.addListener(IAOPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}

	

	public static List<FSMMachineInstance> createIAOPreStateMachines(SimpleJavaNode node, FSMMachine fsm) {
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
				if (!(pre instanceof IAOPrecondition)) {
					continue;
				}
				IAOPrecondition iaopre = (IAOPrecondition) pre;
				for(Enumeration<MapOfVariable> en=iaopre.getTable().keys();en.hasMoreElements();){
					MapOfVariable v=en.nextElement();
					if(v.getIndex()>=0 && v.getIndex()<arguments.jjtGetChild(0).jjtGetNumChildren()){						
						SimpleJavaNode n=(SimpleJavaNode)arguments.jjtGetChild(0).jjtGetChild(v.getIndex());
						ASTName name=(ASTName)n.getSingleChildofType(ASTName.class);
						//检查f(0)的情况
						if(name==null){
							DomainData expdata = new DomainData();
							expdata.sideeffect = false;
							n.jjtAccept(new ExpressionDomainVisitor(), expdata);
							if(expdata.type==ClassType.INT){
								IntegerDomain intDomain =(IntegerDomain)expdata.domain;
								if(!intDomain.getUnknown() && intDomain.contains(0)){
									StringBuffer traceinfo=new StringBuffer();
									for(String s:iaopre.getTable().get(v)){
										traceinfo.append(s);
									}
									FSMMachineInstance fsminstance = fsm.creatInstance();
									//Config.IAO_PRE_INSTANCE_COUNT++;
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
								DomainData expdata = new DomainData();
								expdata.sideeffect = false;
								n.jjtAccept(new ExpressionDomainVisitor(), expdata);
								if(expdata.type == ClassType.INT){
									IntegerDomain intDomain =(IntegerDomain)expdata.domain;
									if(/*intDomain.getUnknown() ||*/!intDomain.getUnknown() && intDomain.contains(0)){
										StringBuffer traceinfo=new StringBuffer();
										for(String s:iaopre.getTable().get(v)){
											traceinfo.append(s);
										}
										FSMMachineInstance fsminstance = fsm.creatInstance();
										//Config.IAO_PRE_INSTANCE_COUNT++;
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
							SimpleJavaNode nod = arguments;
							while(null != nod && nod.getCurrentVexNode() == null) {
								nod = (SimpleJavaNode) nod.jjtGetParent();
							}
							if(nod == null || nod.getCurrentVexNode() == null) {
								continue;
							}
							VexNode vex = nod.getCurrentVexNode();
							Domain domain = (Domain) vex.getDomain(dec);
							if(domain instanceof IntegerDomain){
								IntegerDomain intDomain =(IntegerDomain)domain;
								if (/*intDomain.getUnknown()
										||*/ !intDomain.getUnknown()
										&& intDomain.contains(0)) {
									StringBuffer traceinfo = new StringBuffer();
									for (String s : iaopre.getTable().get(v)) {
										traceinfo.append(s);
									}
									FSMMachineInstance fsminstance = fsm
											.creatInstance();
									//Config.IAO_PRE_INSTANCE_COUNT++;
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
