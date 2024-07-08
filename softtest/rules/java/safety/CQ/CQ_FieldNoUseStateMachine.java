package softtest.rules.java.safety.CQ;

import softtest.fsm.java.*;
import java.lang.reflect.Method;
import java.util.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTMarkerAnnotation;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTImplementsList;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;

/**
 * 未被使用的变量&永不使用的变量
 * 
 *     public class Dead {
 *         String glue;
 *         public String getGlue() {
 *             return "glue";
 *         }
 *     }
 * 
 */

public class CQ_FieldNoUseStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行定义变量（域）但在类的方法中没有出现（使用），可能造成一个漏洞", errorline);
		}else{
			f.format("Code Quality: Field is not used on line %d",errorline);
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
	public static List<FSMMachineInstance> createFieldNoUseStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		List evalRlts = null;
		Iterator i = null;
		
//		检查域声明的情况：包括：FieldDeclaration和LocalVariableDeclaration
//		e.g:	函数外：private static HashSet<DmInfo>deprecatedMethods=null;		
//				函数中：int a;
	
		xPath=".//VariableDeclarator/VariableDeclaratorId";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();

			//	检查Field是否为public
			if(id.getFirstParentOfType(ASTFieldDeclaration.class)!=null){
				ASTFieldDeclaration af=(ASTFieldDeclaration)id.getFirstParentOfType(ASTFieldDeclaration.class);
				if(af.isPublic()||af.isProtected())continue;
			}
			//	检查为变量声明
			if (id.getNameDeclaration() instanceof VariableNameDeclaration) {
				
				// 检查为序列化ID
				if(id.getImage().equals("serialVersionUID"))continue;
				
				VariableNameDeclaration v = id.getNameDeclaration();
				Map map=null;
				map=v.getDeclareScope().getVariableDeclarations();
				if(map != null){
					List occs=(ArrayList)map.get(v);
					if(occs.size()==0){
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
						fsminstance.setResultString("Field is not used: "+id.getImage());
						list.add(fsminstance);
					}
					else{
						//检查变量只被赋值未被使用的情况
						List occ=v.getOccs();
						Iterator vo=occ.iterator();
						Set<ASTVariableDeclaratorId> st=new HashSet(){};
						while(vo.hasNext()){
							NameOccurrence non=(NameOccurrence)vo.next();							
							if(non.getOccurrenceType()==NameOccurrence.OccurrenceType.USE){		
								st.add(id);
								continue;
							}else{
								if(non.getOccurrenceType()==NameOccurrence.OccurrenceType.DEF){
									if(non.isSelfIncOrDec()){
										st.add(id);
										continue;
									}
								}
							}
						}
						if(!st.contains(id)){
							FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
							fsminstance.setResultString("Field is not used: "+id.getImage());
							list.add(fsminstance);
						}
					}
				} 
			}	
		}
		
//		检查函数中参数未使用
//		e.g:public static void abc(String str)
		
		xPath=".//FormalParameters//VariableDeclaratorId";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			if(id.getFirstParentOfType(ASTMethodDeclaration.class)!=null){
				ASTMethodDeclaration amd=(ASTMethodDeclaration)id.getFirstParentOfType(ASTMethodDeclaration.class);
				
			//	检查main函数中的arg问题
				if(amd.getMethodName().equals("main"))continue;
				
			//	检查抽象方法中的参数问题
				if(amd.isAbstract())continue;	
				
//				检查AllocationExpression中的情况
				
				if(id.getFirstParentOfType(ASTAllocationExpression.class)!=null){
					ASTAllocationExpression astAE=(ASTAllocationExpression)id.getFirstParentOfType(ASTAllocationExpression.class);
					Set<String> set=new HashSet<String>(){};
					if(astAE.getFirstChildrenOfType(ASTClassOrInterfaceType.class)!=null){
						ASTClassOrInterfaceType aci=(ASTClassOrInterfaceType)astAE.getFirstChildrenOfType(ASTClassOrInterfaceType.class);
//						int numofchild=aci.jjtGetNumChildren();
						
//						for(int jjti=0;jjti<numofchild;jjti++){
//							ASTClassOrInterfaceType ait=(ASTClassOrInterfaceType)aci.jjtGetChild(jjti);
						
						try{
							Method [] met=softtest.symboltable.java.TypeSet.getCurrentTypeSet().findClass(aci.getImage()).getMethods();
							//	添加接口中方法集合
							for(int im=0;im<met.length;im++){
								set.add(met[im].getName());
							}
						}catch(ClassNotFoundException e){
//							System.out.println(aci.getImage()+" is not exist!");
						}
//						}
						//	检查是否为接口方法集合中的成员
						
					}
					if(set.contains(amd.getMethodName()))
						continue;
				}
				
				
				if(id.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){
					ASTClassOrInterfaceDeclaration aci=(ASTClassOrInterfaceDeclaration)id.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
					
					//	检查Annotation为@Override的情况
					if(aci.getFirstChildrenOfType(ASTMarkerAnnotation.class)!=null){
						ASTMarkerAnnotation ama=(ASTMarkerAnnotation)aci.getFirstChildrenOfType(ASTMarkerAnnotation.class);
						ASTName name=(ASTName)ama.jjtGetChild(0);
						if(name.getImage().contains("Override")){	
							continue;		
						}
					}
					
					//	检查方法为接口中的方法，不报错
					if(aci.getFirstChildrenOfType(ASTImplementsList.class)!=null){
						ASTImplementsList ail=(ASTImplementsList)aci.getFirstChildrenOfType(ASTImplementsList.class);
						int numofchild=ail.jjtGetNumChildren();
						Set<String> set=new HashSet<String>(){};
						for(int jjti=0;jjti<numofchild;jjti++){
							ASTClassOrInterfaceType ait=(ASTClassOrInterfaceType)ail.jjtGetChild(jjti);
							try{
								Method [] met=softtest.symboltable.java.TypeSet.getCurrentTypeSet().findClass(ait.getImage()).getMethods();
								//	添加接口中方法集合
								for(int im=0;im<met.length;im++){
									set.add(met[im].getName());
									}
							}catch(Exception e){
//								e.printStackTrace();
								}
						}
						//	检查是否为接口方法集合中的成员
						if(set.contains(amd.getMethodName()))continue;
					}
					
					// 检查方法为继承的类中的方法，不报错
					if(aci.getFirstChildrenOfType(ASTExtendsList.class)!=null){
						ASTExtendsList ael=(ASTExtendsList)aci.getFirstChildrenOfType(ASTExtendsList.class);
						int numofchild=ael.jjtGetNumChildren();
						Set<String> set=new HashSet<String>(){};
						for(int jjti=0;jjti<numofchild;jjti++){
							ASTClassOrInterfaceType ait=(ASTClassOrInterfaceType)ael.jjtGetChild(jjti);
							try{
								Method [] met=softtest.symboltable.java.TypeSet.getCurrentTypeSet().findClass(ait.getImage()).getMethods();
							//	添加继承的类中方法集合
								for(int im=0;im<met.length;im++){
									set.add(met[im].getName());
								}
							}catch(Exception e){
//								e.printStackTrace();
								}
						}
						//	检查是否为继承的类中方法集合中的成员
						if(set.contains(amd.getMethodName()))continue;
					}
				}
			
			
				
			//	检查catch的参数未使用的问题，不报告
				if(id.getFirstParentOfType(ASTFormalParameter.class)!=null){
					ASTFormalParameter af=(ASTFormalParameter)id.getFirstParentOfType(ASTFormalParameter.class);
					if(af.jjtGetParent().toString().equals("CatchStatement"))continue;
				}
		
			//	检查为变量声明
				if (id.getNameDeclaration() instanceof VariableNameDeclaration) {		
					VariableNameDeclaration v = id.getNameDeclaration();
					Map map=null;
					map=v.getDeclareScope().getVariableDeclarations();
					if(map != null){
						List occs=(ArrayList)map.get(v);					
						if(occs.size()==0){
							FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
							fsminstance.setResultString("Parameter is not used: "+id.getImage()+" in method: "+amd.getMethodName());
							list.add(fsminstance);
						}
					} 
				}
			}
		}
		return list;
	}	
}
