package softtest.rules.java.safety.ECP;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;
public class ECP_AppletCodeProbStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 检查Applet代码中存在的问题
	 * 2009.09.11@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("封装问题模式: %d 行的applets程序中存在问题方法。移动代码可以伴随其他潜在的恶意移动代码一起运行，这将为攻击者操纵对象的状态和行为提供可能。", errorline);
		}else{
			f.format("ECP: Applets code problem on line %d",errorline);
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
	public static List<FSMMachineInstance> createAppletCodeProblemStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		String xPath = ".//TypeDeclaration//ClassOrInterfaceDeclaration/ExtendsList/ClassOrInterfaceType[1]";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceType parentclass = (ASTClassOrInterfaceType) i.next();
			String image=parentclass.getImage();
			if(image==null||!(image.toLowerCase().contains("applet"))){
				continue;
			}
			ASTClassOrInterfaceDeclaration acd=(ASTClassOrInterfaceDeclaration)parentclass.jjtGetParent().jjtGetParent();
			/*1.检查Applet中 public 的成员变量都应声明 final
			 * */	
			xPath=".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Public='true'][@Final='false']/VariableDeclarator";
			List dec=acd.findXpath(xPath);
			Iterator a1=dec.iterator();
			if(a1.hasNext()){
				ASTVariableDeclarator varDec=(ASTVariableDeclarator)a1.next();
				ASTVariableDeclaratorId varId=(ASTVariableDeclaratorId)varDec.jjtGetChild(0);
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(varDec));
				fsminstance.setResultString("Applet use a public variable but not final:"+varId.getImage());
				list.add(fsminstance);
			}
			
			/*2.检查Applet中 public 的成员变量都应声明 final
			 * */	
			xPath=".//ClassOrInterfaceBodyDeclaration//FieldDeclaration[@Public='true']/Type[@Array='true']";
			List arr=acd.findXpath(xPath);
			Iterator a2=arr.iterator();
			if(a2.hasNext()){
				ASTType type=(ASTType)a2.next();
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
				fsminstance.setResultString("Applet use public declare a Array");
				list.add(fsminstance);
			}
			
			/*3.检查Applet中使用了内部类
			 * */	
			xPath=".//ClassOrInterfaceBodyDeclaration/ClassOrInterfaceDeclaration[@Nested='true']";
			List nClass=acd.findXpath(xPath);
			Iterator a3=nClass.iterator();
			if(a3.hasNext()){
				ASTClassOrInterfaceDeclaration clasDec=(ASTClassOrInterfaceDeclaration)a3.next();
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(clasDec));
				fsminstance.setResultString("Applet use inner Class");
				list.add(fsminstance);
			}
			/*4.检查公共函数返回私有类型的变量
			 *	检查了所有公共函数，不仅仅是输出的。
			 * */	
			xPath=".//MethodDeclaration//Block//PrimaryExpression/PrimaryPrefix/Name[@MethodName='false']";
			List nPub=acd.findXpath(xPath);
			Iterator a4=nPub.iterator();
			if(a4.hasNext()){
				ASTName name=(ASTName)a4.next();
				image=name.getImage();
				VariableNameDeclaration nameV=(VariableNameDeclaration)name.getNameDeclaration(); 
				if(nameV.getAccessNodeParent()==null){
					return list;
				}
				if(nameV.getAccessNodeParent().isPrivate()){
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
					fsminstance.setResultString("Applet's public function use a private variable");
					list.add(fsminstance);					
				}
			}
			
		}
		return list;
	}
}
