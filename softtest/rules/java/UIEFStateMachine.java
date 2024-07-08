package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.config.java.Config;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.symboltable.java.*;

/** 使用低效函数或代码 */
public class UIEFStateMachine {
	/** 在节点node上查找xPath */
	private static List findTreeNodes(SimpleNode node, String xPath) {
		List evaluationResults = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}

	public static List<FSMMachineInstance> createUIEFStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		List evaluationResults = null;

		/**
		 * 形如： s.equals("")
		 */
		xPath = ".//PrimaryExpression[child::PrimarySuffix[1]/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal[@Image=\'\"\"\']]/PrimaryPrefix/Name[matches(@Image,\'^.+\\.equals$\')]";
		evaluationResults = findTreeNodes(node, xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^((.+\\.String)|(String))$")) {
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
			fsminstance.setResultString(name.getImage());
		}

		/**
		 * 形如： "".equals(s)
		 */
		xPath = ".//PrimaryExpression[child::PrimaryPrefix/Literal[@Image=\'\"\"\'] and child::PrimarySuffix[1][@Image=\'equals\']]/PrimarySuffix[2][@Arguments=\'true\']";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTPrimarySuffix suffix = (ASTPrimarySuffix) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(suffix));
			fsminstance.setResultString(suffix.printNode(ProjectAnalysis.getCurrent_file()));
		}

		/**
		 * 形如： if(a&b); for(;a&b;);while(a&b);do{}while(a&b);
		 */
		xPath = ".//Expression[parent::WhileStatement or parent::DoStatement or parent::ForStatement or parent::IfStatement]//*[self::AndExpression or self::InclusiveOrExpression]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			SimpleJavaNode and = (SimpleJavaNode) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(and));
			if(and instanceof ASTAndExpression){
				fsminstance.setResultString("&");
			}else{
				fsminstance.setResultString("|");
			}
		}

		/**
		 * 形如： new Boolean(true);new Short(1);new Integer(4);......
		 */
		xPath = ".//AllocationExpression[./Arguments]/ClassOrInterfaceType[matches(@Image,\'^((.+\\.Boolean)|(Boolean)|(.+\\.Double)|(Double)|(.+\\.Integer)|(Integer)|(.+\\.Float)|(Float)|(.+\\.Short)|(Short)|(.+\\.Byte)|(Byte)|(.+\\.Long)|(Long))$\')]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceType type = (ASTClassOrInterfaceType) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
			fsminstance.setResultString(type.getImage());
		}

		/**
		 * 形如： (int)(rd.nextDouble() * 240)
		 */
		xPath = ".//CastExpression[./Type/PrimitiveType[matches(@Image,\'^((int)|(short)|(byte)|(long))$\')]]//PrimaryExpression[./PrimarySuffix[@Arguments=\'true\']]/PrimaryPrefix/Name[matches(@Image,\'^.+\\.nextDouble$\')]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^((.+\\.Random)|(Random))$")) {
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
			fsminstance.setResultString("nextDouble()");
		}
		
		
		/**
		 * 形如： new java.lang.String("abc")
		 */
		xPath = ".//AllocationExpression[./Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal]/ClassOrInterfaceType[matches(@Image,\'^((.+\\.String)|(String))$\')]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceType type = (ASTClassOrInterfaceType) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
			fsminstance.setResultString(type.getImage());
		}
		
		/**
		 * 形如： final String str="asdfasdfsadfasdf";
		 */
		xPath = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final=\'true\' and ./Type/ReferenceType/ClassOrInterfaceType[matches(@Image,\'^((String)|(.+\\.String))$\')]]/VariableDeclarator/VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Literal";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTLiteral literal = (ASTLiteral) i.next();
			if(literal.getImage()!=null&&literal.getImage().length()<100){
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(literal));
			fsminstance.setResultString("too long literal");
		}
		
		xPath = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final=\'true\' and ./Type/ReferenceType/ClassOrInterfaceType[matches(@Image,\'^((String)|(.+\\.String))$\')]]/VariableDeclarator/VariableInitializer/Expression[./AdditiveExpression/PrimaryExpression/PrimaryPrefix/Literal]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTExpression expr = (ASTExpression) i.next();
			xPath="./AdditiveExpression/PrimaryExpression/PrimaryPrefix/Literal";
			List temp=findTreeNodes(expr, xPath);
			Iterator itor=temp.iterator();
			int len=0;
			while(itor.hasNext()){
				ASTLiteral literal = (ASTLiteral) itor.next();
				if(literal.getImage()!=null){
					len+=literal.getImage().length();
				}
			}
			if(len<100){
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(expr));
			fsminstance.setResultString("too long literal");
		}

		/**
		 * 形如： for(;;){s = s + "dafd";} for(;;){s += "dafd";}
		 */
		xPath = ".//*[self::ForStatement or self::WhileStatement or self::DoStatement]/Statement//AssignmentOperator[(@Image=\'+=\')or(@Image=\'=\' and preceding-sibling::PrimaryExpression/PrimaryPrefix/Name/@Image=following-sibling::Expression/AdditiveExpression/PrimaryExpression/PrimaryPrefix/Name/@Image)]/preceding-sibling::PrimaryExpression/PrimaryPrefix/Name";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^((.+\\.String)|(String))$")) {
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
			fsminstance.setResultString(v.getImage());
		}
		
		/**
		 * 形如：public class Test{
		 * 			int j;
   		 *			class Inner{
	   	 *				private int i=0;
	   	 *				public int getValue(){
		 * 					return i;
	     *				}
   		 *			}
   		 *			Object o=new Object(){
   		 *				void f(){}
   		 *			}
		 *		}
		 */
		xPath = ".//ClassOrInterfaceDeclaration/ClassOrInterfaceBody//*[(self::ClassOrInterfaceDeclaration[@Static=\'false\'])or (self::ClassOrInterfaceBody and parent::AllocationExpression)]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			SimpleJavaNode inner = (SimpleJavaNode) i.next();
			ASTClassOrInterfaceDeclaration outer=(ASTClassOrInterfaceDeclaration)inner.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
			ClassScope out=(ClassScope)outer.getScope();
			
			Map variableNames = null;
			variableNames = out.getVariableDeclarations();
			boolean  bused=false;
			if (variableNames != null) {//获得变量表
				Iterator itor = variableNames.entrySet().iterator();
				while (itor.hasNext()) {
					Map.Entry e = (Map.Entry) itor.next();
					List occs = (List) e.getValue();
					VariableNameDeclaration v=(VariableNameDeclaration)e.getKey();
					if(v.getAccessNodeParent().isStatic()){
						continue;
					}
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						if(occ.getLocation().isSelOrAncestor(inner)){
							bused=true;
							break;
						}
					}
				}
			}
			Map methodNames = null;
			methodNames = out.getMethodDeclarations();
			
			if (!bused&&methodNames != null) {//获得变量表
				Iterator itor = methodNames.entrySet().iterator();
				while (itor.hasNext()) {
					Map.Entry e = (Map.Entry) itor.next();
					List occs = (List) e.getValue();
					MethodNameDeclaration m=(MethodNameDeclaration)e.getKey();
					if(((ASTMethodDeclaration)m.getMethodNameDeclaratorNode().jjtGetParent()).isStatic()){
						continue;
					}
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						if(occ.getLocation().isSelOrAncestor(inner)){
							bused=true;
							break;
						}
					}
				}
			}
			if(!bused){
				FSMMachineInstance fsminstance = fsm.creatInstance();
				list.add(fsminstance);
				fsminstance.setRelatedObject(new FSMRelatedCalculation(inner));
				String image=inner.getImage();
				if(image==null||image.length()==0){
					image="AnonymousInnerClass";
				}
				fsminstance.setResultString(image);
			}
		}		
		
		/**
		 * 形如： final int VAL_ONE = 7;;
		 */
		xPath = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final=\'true\' and @Static=\'false\']/VariableDeclarator/VariableDeclaratorId";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
			fsminstance.setResultString(id.getImage());
		}
		
		/**
		 * 形如： url.equals(o); url.hashCode();
		 */
		xPath = ".//PrimaryExpression[child::PrimarySuffix[1]/Arguments]/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.equals)|(.+\\.hashCode))$\')]";
		evaluationResults = findTreeNodes(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^((.+\\.URL)|(URL))$")) {
				continue;
			}
			FSMMachineInstance fsminstance = fsm.creatInstance();
			list.add(fsminstance);
			fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
			fsminstance.setResultString(name.getImage());
		}
		
		return list;
	}

}