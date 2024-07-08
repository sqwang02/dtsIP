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
 * ��ֵ��δʹ��
 * 1    int r = getNum();
 * 2    r = getNewNum(buf);
 */

public class CQ_UnusedVarAssStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */

	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �б�����ֵ��δʹ�ñ����¸�ֵ���������һ��©��", errorline);
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
//			����Ƿ�Ϊ���캯��
			if(occ.getLocation()==null||occ.getLocation().getFirstParentOfType(ASTConstructorDeclaration.class)!=null)return false;
//			���this.x
			if((occ.getLocation().getLastSibling()!=null)&&(occ.getLocation().getLastSibling()instanceof ASTPrimaryPrefix )){
				ASTPrimaryPrefix ape=(ASTPrimaryPrefix)occ.getLocation().getLastSibling();
				if(ape.usesThisModifier())return false;
			}
//			�����������û�г�ʼ�������
//			��occ�ǵ�һ������
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
//			��������������ĸ�ֵ
			if(occ.getLocation()==null||occ.getLocation().getFirstParentOfType(ASTIfStatement.class)!=null||occ.getLocation().getFirstParentOfType(ASTSwitchStatement.class)!=null)continue;			
			
			
			List useList = occ.getDefUseList();
			if( occ.getOccurrenceType() == NameOccurrence.OccurrenceType.DEF ) {
				if( useList == null || useList.size() == 0 ) {
					found = true;			
					fsmInst.setResultString(occ.getImage());
					
					// ������壭ȡ����������Ϊ�գ�˵�������ظ���ֵ
					List<NameOccurrence> undeflist=occ.getDefUndefList();
					if(undeflist!=null){
						// ����������������㣬���ų�
						if(occ.isSelfIncOrDec()) {
							
							found = false;
							break;
						}
						//	�����������û�г�ʼ����������ǵ�һ�����ʱ��
						if(occ.getLocation()!=null&&(occ.getLocation() instanceof ASTName)){
							ASTName astn=(ASTName)occ.getLocation();
							if(!astn.isMethodName()){
								VariableNameDeclaration v =(VariableNameDeclaration) astn.getNameDeclaration();
								if(v.getOccs().get(0).equals(occ)){
									if(v.getNode().jjtGetParent() instanceof ASTVariableDeclarator){
										ASTVariableDeclarator astv = (ASTVariableDeclarator)v.getNode().jjtGetParent();
										//δ��ʼ�������
										if(astv.jjtGetNumChildren()<=1){
											found = false;
											break;		
										}else
										//����ʼ���õĲ��Ƿ���
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
						//	���boolean�ͱ���
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
					// �����ѭ���У���Ϊ���壭ȡ��������Ϊ�գ�������Ҫ���
					// 1����ѭ���Ŀ�ʼ����һ�θñ�������(�Ǳ��γ���)�Ƕ������
					// 2����ѭ���Ŀ�ʼ����һ�θñ�������(�Ǳ��γ���)��ʹ�ó���
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
