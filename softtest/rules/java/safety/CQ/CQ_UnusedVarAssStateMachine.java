package softtest.rules.java.safety.CQ;

import softtest.fsm.java.*;
import java.util.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;

/**
 * 赋值后未使用
 * 1    int r = getNum();
 * 2    r = getNewNum(buf);
 */

public class CQ_UnusedVarAssStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */

	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行变量赋值后未使用便重新赋值，可能造成一个漏洞", errorline);
		}else{
			f.format("Code Quality: The variable's value is assigned but never used on line %d",errorline);
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
	
	public static List<FSMMachineInstance> createUnusedVarAssStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		FSMMachineInstance fsmInst = fsm.creatInstance();
		fsmInst.setRelatedObject(new FSMRelatedCalculation(node));	
		list.add(fsmInst);
		return list;
	}

	public static boolean checkAsgnNoUse(VexNode vex,FSMMachineInstance fsmInst) {
		boolean found = false;
		ArrayList occrList = vex.getOccurrences();
		for(int i = 0; i < occrList.size(); i++) {
			NameOccurrence occ = (NameOccurrence)occrList.get(i);		
//			检查是否为构造函数
			if(occ.getLocation()==null||occ.getLocation().getFirstParentOfType(ASTConstructorDeclaration.class)!=null)return false;
//			检查this.x
			if((occ.getLocation().getLastSibling()!=null)&&(occ.getLocation().getLastSibling()instanceof ASTPrimaryPrefix )){
				ASTPrimaryPrefix ape=(ASTPrimaryPrefix)occ.getLocation().getLastSibling();
				if(ape.usesThisModifier())return false;
			}
//			检查声明但是没有初始化的情况
//			且occ是第一次声明
//			if(occ.getLocation()!=null&&(occ.getLocation() instanceof ASTName)){
//				ASTName astn=(ASTName)occ.getLocation();
//				if(!astn.isMethodName()){
//					VariableNameDeclaration v =(VariableNameDeclaration) astn.getNameDeclaration();
//					if(v.getOccs().get(0)==occ){
//						if(v.getNode() instanceof ASTVariableDeclarator){
//							ASTVariableDeclarator astv = (ASTVariableDeclarator)v.getNode().jjtGetParent();
//							if(astv.jjtGetNumChildren()==1)return false;		
//						}
//					}
//				}
//			}
//			检查条件语句里面的赋值
			if(occ.getLocation()==null||occ.getLocation().getFirstParentOfType(ASTIfStatement.class)!=null||occ.getLocation().getFirstParentOfType(ASTSwitchStatement.class)!=null)continue;			
			
			
			List useList = occ.getDefUseList();
			if( occ.getOccurrenceType() == NameOccurrence.OccurrenceType.DEF ) {
				if( useList == null || useList.size() == 0 ) {
					found = true;			
					fsmInst.setResultString(occ.getImage());
					
					// 如果定义－取消定义链不为空，说明存在重复赋值
					List<NameOccurrence> undeflist=occ.getDefUndefList();
					if(undeflist!=null){
						// 如果是自增、减运算，则排除
						if(occ.isSelfIncOrDec()) {
							
							found = false;
							break;
						}
						//	检查声明但是没有初始化的情况且是第一定义的时候
						if(occ.getLocation()!=null&&(occ.getLocation() instanceof ASTName)){
							ASTName astn=(ASTName)occ.getLocation();
							if(!astn.isMethodName()){
								VariableNameDeclaration v =(VariableNameDeclaration) astn.getNameDeclaration();
								if(v.getOccs().get(0).equals(occ)){
									if(v.getNode().jjtGetParent() instanceof ASTVariableDeclarator){
										ASTVariableDeclarator astv = (ASTVariableDeclarator)v.getNode().jjtGetParent();
										//未初始化的情况
										if(astv.jjtGetNumChildren()<=1){
											found = false;
											break;		
										}else
										//检查初始化用的不是方法
											if(astv.jjtGetChild(1) instanceof ASTVariableInitializer){
												
												ASTVariableInitializer avi=(ASTVariableInitializer)astv.jjtGetChild(1);
												Node ac=avi.jjtGetChild(0);
												while(!(ac instanceof ASTName)&&ac.jjtGetNumChildren()!=0){
													ac=ac.jjtGetChild(0);
												}
												if(ac instanceof ASTName){
													ASTName name=(ASTName)ac;
													if(!name.isMethodName()){
														found = false;
														break;
													}
												}else{
													found = false;
													break;
												}
											}
									}
								}
							}
						}
						//	检查boolean型变量
						if(occ.getLocation()!=null&&(occ.getLocation() instanceof ASTName)){
							ASTName astn=(ASTName)occ.getLocation();
							if(!astn.isMethodName()){
								VariableNameDeclaration v =(VariableNameDeclaration) astn.getNameDeclaration();
								try{
									if(v.getType().toString().equals("boolean")){
									   found = false;
									   break;
								    }
								}
								catch(Exception e){
									found = false;
									break;								
								}
							}
						}
					}
					// 如果是循环中，因为定义－取消定义链为空，但是仍要检测
					// 1、在循环的开始处第一次该变量出现(非本次出现)是定义出现
					// 2、在循环的开始处第一次该变量出现(非本次出现)是使用出现
					if(found) {
						found =true;
						fsmInst.setResultString("assign to " + occ.getImage() + " but NoUse");
						break;
					}
				}
			}
		}
		return found;
	}

}
