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
 * δ��ʹ�õı���&����ʹ�õı���
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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �ж���������򣩵�����ķ�����û�г��֣�ʹ�ã����������һ��©��", errorline);
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
		
//		����������������������FieldDeclaration��LocalVariableDeclaration
//		e.g:	�����⣺private static HashSet<DmInfo>deprecatedMethods=null;		
//				�����У�int a;
	
		xPath=".//VariableDeclarator/VariableDeclaratorId";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();

			//	���Field�Ƿ�Ϊpublic
			if(id.getFirstParentOfType(ASTFieldDeclaration.class)!=null){
				ASTFieldDeclaration af=(ASTFieldDeclaration)id.getFirstParentOfType(ASTFieldDeclaration.class);
				if(af.isPublic()||af.isProtected())continue;
			}
			//	���Ϊ��������
			if (id.getNameDeclaration() instanceof VariableNameDeclaration) {
				
				// ���Ϊ���л�ID
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
						//������ֻ����ֵδ��ʹ�õ����
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
		
//		��麯���в���δʹ��
//		e.g:public static void abc(String str)
		
		xPath=".//FormalParameters//VariableDeclaratorId";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			if(id.getFirstParentOfType(ASTMethodDeclaration.class)!=null){
				ASTMethodDeclaration amd=(ASTMethodDeclaration)id.getFirstParentOfType(ASTMethodDeclaration.class);
				
			//	���main�����е�arg����
				if(amd.getMethodName().equals("main"))continue;
				
			//	�����󷽷��еĲ�������
				if(amd.isAbstract())continue;	
				
//				���AllocationExpression�е����
				
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
							//	��ӽӿ��з�������
							for(int im=0;im<met.length;im++){
								set.add(met[im].getName());
							}
						}catch(ClassNotFoundException e){
//							System.out.println(aci.getImage()+" is not exist!");
						}
//						}
						//	����Ƿ�Ϊ�ӿڷ��������еĳ�Ա
						
					}
					if(set.contains(amd.getMethodName()))
						continue;
				}
				
				
				if(id.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){
					ASTClassOrInterfaceDeclaration aci=(ASTClassOrInterfaceDeclaration)id.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
					
					//	���AnnotationΪ@Override�����
					if(aci.getFirstChildrenOfType(ASTMarkerAnnotation.class)!=null){
						ASTMarkerAnnotation ama=(ASTMarkerAnnotation)aci.getFirstChildrenOfType(ASTMarkerAnnotation.class);
						ASTName name=(ASTName)ama.jjtGetChild(0);
						if(name.getImage().contains("Override")){	
							continue;		
						}
					}
					
					//	��鷽��Ϊ�ӿ��еķ�����������
					if(aci.getFirstChildrenOfType(ASTImplementsList.class)!=null){
						ASTImplementsList ail=(ASTImplementsList)aci.getFirstChildrenOfType(ASTImplementsList.class);
						int numofchild=ail.jjtGetNumChildren();
						Set<String> set=new HashSet<String>(){};
						for(int jjti=0;jjti<numofchild;jjti++){
							ASTClassOrInterfaceType ait=(ASTClassOrInterfaceType)ail.jjtGetChild(jjti);
							try{
								Method [] met=softtest.symboltable.java.TypeSet.getCurrentTypeSet().findClass(ait.getImage()).getMethods();
								//	��ӽӿ��з�������
								for(int im=0;im<met.length;im++){
									set.add(met[im].getName());
									}
							}catch(Exception e){
//								e.printStackTrace();
								}
						}
						//	����Ƿ�Ϊ�ӿڷ��������еĳ�Ա
						if(set.contains(amd.getMethodName()))continue;
					}
					
					// ��鷽��Ϊ�̳е����еķ�����������
					if(aci.getFirstChildrenOfType(ASTExtendsList.class)!=null){
						ASTExtendsList ael=(ASTExtendsList)aci.getFirstChildrenOfType(ASTExtendsList.class);
						int numofchild=ael.jjtGetNumChildren();
						Set<String> set=new HashSet<String>(){};
						for(int jjti=0;jjti<numofchild;jjti++){
							ASTClassOrInterfaceType ait=(ASTClassOrInterfaceType)ael.jjtGetChild(jjti);
							try{
								Method [] met=softtest.symboltable.java.TypeSet.getCurrentTypeSet().findClass(ait.getImage()).getMethods();
							//	��Ӽ̳е����з�������
								for(int im=0;im<met.length;im++){
									set.add(met[im].getName());
								}
							}catch(Exception e){
//								e.printStackTrace();
								}
						}
						//	����Ƿ�Ϊ�̳е����з��������еĳ�Ա
						if(set.contains(amd.getMethodName()))continue;
					}
				}
			
			
				
			//	���catch�Ĳ���δʹ�õ����⣬������
				if(id.getFirstParentOfType(ASTFormalParameter.class)!=null){
					ASTFormalParameter af=(ASTFormalParameter)id.getFirstParentOfType(ASTFormalParameter.class);
					if(af.jjtGetParent().toString().equals("CatchStatement"))continue;
				}
		
			//	���Ϊ��������
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
