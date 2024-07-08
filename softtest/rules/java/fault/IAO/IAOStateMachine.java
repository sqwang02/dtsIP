package softtest.rules.java.fault.IAO;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.JavaNode;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;
import softtest.domain.java.*;

public class IAOStateMachine  extends AbstractStateMachine{
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
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	public static List<FSMMachineInstance> createIAOStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		//除法和取余操作
		String xPath = ".//MultiplicativeExpression[matches(@Image,'/|%')]";
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTMultiplicativeExpression mulexpr = (ASTMultiplicativeExpression) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(mulexpr));
			if(!mulexpr.hasLocalMethod(node)){
				list.add(fsminstance);
			}
		}
		
		//可能出现非法计算的函数
		xPath=".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.asin)|(asin)|(.+\\.acos)|(acos)|(.+\\.atan2)|(atan2)|(.+\\.log)|(log)|(.+\\.log10)|(log10)|(.+\\.sqrt)|(sqrt))$\')]";
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
			if(!name.hasLocalMethod(node)){
				list.add(fsminstance);
			}
		}
		return list;
	}

	public static boolean checkChildAndDividerNotZero(List nodes, FSMMachineInstance fsmin) {
		Iterator i=nodes.iterator();
		while (i.hasNext()) {
			SimpleJavaNode treenode = (SimpleJavaNode) i.next();
			
			ASTMultiplicativeExpression mulexpr = null;
			if(fsmin.getRelatedObject().getTagTreeNode()instanceof ASTMultiplicativeExpression){
				mulexpr=(ASTMultiplicativeExpression) fsmin.getRelatedObject().getTagTreeNode();
			}
			if (mulexpr!=treenode) {
				continue;
			}
			String image = mulexpr.getImage();
			String[] operators = image.split("#");
			for (int j = 1; j < mulexpr.jjtGetNumChildren(); j++) {
				String operator = operators[j - 1];
				if (operator.equals("/")||operator.equals("%")) {
					List<VexNode> vexlist = mulexpr.getCurrentVexList();
					VexNode vex = vexlist.get(0);
					DomainSet old=vex.getDomainSet();
					vex.setDomainSet(vex.getLastDomainSet());
					
					DomainData expdata = new DomainData();
					expdata.sideeffect = false;
					JavaNode javanode = (JavaNode) mulexpr.jjtGetChild(j);
					javanode.jjtAccept(new ExpressionDomainVisitor(), expdata);
					
					vex.setDomainSet(old);
					if (expdata.type == ClassType.DOUBLE) {
						/*DoubleDomain domain = (DoubleDomain) expdata.domain;
						if (!domain.getUnknown()&&domain.contains(0)) {
							fsmin.setResultString(((SimpleNode)(mulexpr.jjtGetChild(j))).printNode(ProjectAnalysis.getCurrent_file()));
							return true;
						}*/
						//return false;
					} else if (expdata.type == ClassType.INT) {
						IntegerDomain domain = (IntegerDomain) expdata.domain;
						if (!domain.getUnknown()&&domain.contains(0)) {
							fsmin.setResultString(((SimpleNode)(mulexpr.jjtGetChild(j))).printNode(ProjectAnalysis.getCurrent_file()));
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean checkChildAndFunction(List nodes, FSMMachineInstance fsmin) {
		Iterator i=nodes.iterator();
		while (i.hasNext()) {
			SimpleJavaNode treenode = (SimpleJavaNode) i.next();
			ASTName name = null;
			if(fsmin.getRelatedObject().getTagTreeNode()instanceof ASTName){
				name=(ASTName) fsmin.getRelatedObject().getTagTreeNode();
			}
			if (name!=treenode) {
				continue;
			}
			ASTPrimaryExpression primaryexpression=(ASTPrimaryExpression)name.jjtGetParent().jjtGetParent();
			ASTPrimarySuffix primarysuffix=null;
			if(primaryexpression.jjtGetChild(primaryexpression.jjtGetNumChildren()-1) instanceof ASTPrimarySuffix){
				primarysuffix=(ASTPrimarySuffix)primaryexpression.jjtGetChild(primaryexpression.jjtGetNumChildren()-1);
				if(!primarysuffix.isArguments()){
					primarysuffix=null;
				}
			}
			if(primarysuffix==null){
				continue;
			}
			String image=name.getImage();
			if(image.matches("^((.+\\.asin)|(asin)|(.+\\.acos)|(acos))$")){
				ASTArguments arguments=(ASTArguments)primarysuffix.jjtGetChild(0);
				if(arguments.jjtGetNumChildren()!=1){
					continue;
				}
				ASTArgumentList argumentlist=(ASTArgumentList)arguments.jjtGetChild(0);
				if(argumentlist.jjtGetNumChildren()!=1){
					continue;
				}
				List<VexNode> vexlist = name.getCurrentVexList();
				VexNode vex = vexlist.get(0);
				DomainSet old=vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
				
				DomainData expdata = new DomainData();
				expdata.sideeffect = false;
				JavaNode javanode = (JavaNode) argumentlist.jjtGetChild(0);
				javanode.jjtAccept(new ExpressionDomainVisitor(), expdata);
				
				vex.setDomainSet(old);
				if (expdata.type == ClassType.DOUBLE) {
					DoubleDomain domain = (DoubleDomain) expdata.domain;
					DoubleDomain legal=new DoubleDomain(-1,1,false,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				} else if (expdata.type == ClassType.INT) {
					IntegerDomain domain = (IntegerDomain) expdata.domain;
					IntegerDomain legal=new IntegerDomain(-1,1,false,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				}
			}
			else if(image.matches("^((.+\\.atan2)|(atan2))$")){
				ASTArguments arguments=(ASTArguments)primarysuffix.jjtGetChild(0);
				if(arguments.jjtGetNumChildren()!=1){
					continue;
				}
				ASTArgumentList argumentlist=(ASTArgumentList)arguments.jjtGetChild(0);
				if(argumentlist.jjtGetNumChildren()!=2){
					continue;
				}
				List<VexNode> vexlist = name.getCurrentVexList();
				VexNode vex = vexlist.get(0);
				DomainSet old=vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
				
				DomainData expdata = new DomainData();
				expdata.sideeffect = false;
				JavaNode javanode = (JavaNode) argumentlist.jjtGetChild(1);
				javanode.jjtAccept(new ExpressionDomainVisitor(), expdata);
				
				vex.setDomainSet(old);
				if (expdata.type == ClassType.DOUBLE) {
					DoubleDomain domain = (DoubleDomain) expdata.domain;
					if (!domain.getUnknown()&&domain.contains(0)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				} else if (expdata.type == ClassType.INT) {
					IntegerDomain domain = (IntegerDomain) expdata.domain;
					if (!domain.getUnknown()&&domain.contains(0)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				}
			}else if(image.matches("^((.+\\.log)|(log)|(.+\\.log10)|(log10))$")){
				ASTArguments arguments=(ASTArguments)primarysuffix.jjtGetChild(0);
				if(arguments.jjtGetNumChildren()!=1){
					continue;
				}
				ASTArgumentList argumentlist=(ASTArgumentList)arguments.jjtGetChild(0);
				if(argumentlist.jjtGetNumChildren()!=1){
					continue;
				}
				List<VexNode> vexlist = name.getCurrentVexList();
				VexNode vex = vexlist.get(0);
				DomainSet old=vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
				
				DomainData expdata = new DomainData();
				expdata.sideeffect = false;
				JavaNode javanode = (JavaNode) argumentlist.jjtGetChild(0);
				javanode.jjtAccept(new ExpressionDomainVisitor(), expdata);
				
				vex.setDomainSet(old);
				if (expdata.type == ClassType.DOUBLE) {
					DoubleDomain domain = (DoubleDomain) expdata.domain;
					DoubleDomain legal=new DoubleDomain(0,Double.POSITIVE_INFINITY,true,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				} else if (expdata.type == ClassType.INT) {
					IntegerDomain domain = (IntegerDomain) expdata.domain;
					IntegerDomain legal=new IntegerDomain(0,Long.MAX_VALUE,true,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				}				
			}if(image.matches("^((.+\\.sqrt)|(sqrt))$")){
				ASTArguments arguments=(ASTArguments)primarysuffix.jjtGetChild(0);
				if(arguments.jjtGetNumChildren()!=1){
					continue;
				}
				ASTArgumentList argumentlist=(ASTArgumentList)arguments.jjtGetChild(0);
				if(argumentlist.jjtGetNumChildren()!=1){
					continue;
				}
				List<VexNode> vexlist = name.getCurrentVexList();
				VexNode vex = vexlist.get(0);
				DomainSet old=vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
				
				DomainData expdata = new DomainData();
				expdata.sideeffect = false;
				JavaNode javanode = (JavaNode) argumentlist.jjtGetChild(0);
				javanode.jjtAccept(new ExpressionDomainVisitor(), expdata);
				
				vex.setDomainSet(old);
				if (expdata.type == ClassType.DOUBLE) {
					DoubleDomain domain = (DoubleDomain) expdata.domain;
					DoubleDomain legal=new DoubleDomain(0,Double.POSITIVE_INFINITY,false,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				} else if (expdata.type == ClassType.INT) {
					IntegerDomain domain = (IntegerDomain) expdata.domain;
					IntegerDomain legal=new IntegerDomain(0,Long.MAX_VALUE,false,false);
					if (!domain.getUnknown()&&!legal.contains(domain)) {
						fsmin.setResultString(name.printNode(ProjectAnalysis.getCurrent_file()));
						return true;
					}
				}				
			}
		}
		return false;
	}
}
