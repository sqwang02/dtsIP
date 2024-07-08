package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * CLIStateMachine
 * ������ѭ����ʼ��
 * ����:�����൱�У��ֱ��жԶԷ����ʵ����ʼ���Ĵ��롣
 ������
   1   public class FirstClass{   
   2   		public FirstClass{}
   3   		SecondClass  secondClass=new SecondClass();
   4   		public void set(int value) {
   5   			value=secondClass.get();
   6   		}
   7   }
   8   public class SecondClass{
   9   		public SecondClass{} 
   10   	FirstClass firstClass=new FirstClass(); 
   11   		public int get(){
   12   		int a=firstClass.set(int b)
   13   		return a;
   14   	}
   15   }

 * @author cjie
 * 
 */

public class CLIStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("���ѭ����ʼ��: %d �в����˶����ѭ����ʼ��,�������������ѭ����Ȼ�����ڴ������", errorline);
		}else{
			f.format("Class Loop Initialize:at line %d Find class loop Initialize.",errorline);
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
	/**ƥ��������*/
	private static String XPATH=".//ClassOrInterfaceDeclaration/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Static='false']/Type/ReferenceType/ClassOrInterfaceType";
		
	public static List<FSMMachineInstance> createCLIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTClassOrInterfaceType type=(ASTClassOrInterfaceType)o;
			
			ASTClassOrInterfaceDeclaration astClass=null;
			if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTClassOrInterfaceDeclaration )
				astClass=(ASTClassOrInterfaceDeclaration)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent());
			
			/**ȡ��������*/
			String varName="";
			if(type.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTFieldDeclaration)
			{
				if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0) instanceof ASTVariableDeclaratorId)
				{
					varName=((ASTVariableDeclaratorId)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0))).getImage();
				}
			}
			/**�������������ʼ��*/
			String xPathFiled=".//FieldDeclaration[@Static='false' and Type/ReferenceType/ClassOrInterfaceType[@Image='"+type.getImage()+"'] and VariableDeclarator[VariableDeclaratorId[@Image='"+varName+"'] and VariableInitializer" +
					          "/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+type.getImage()+"' and not(following-sibling::ArrayDimsAndInits)]]]";

			/**�����ڹ��캯�����ʼ��*/
			String xPathCon=".//ConstructorDeclaration[.//StatementExpression[PrimaryExpression/PrimaryPrefix/Name[@Image='"+varName+"'] " +
    		"and AssignmentOperator[@Image='=' ]and Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+type.getImage()+"'and not(following-sibling::ArrayDimsAndInits)]]]";

			if(astClass!=null){
				
				List field =astClass.findXpath(xPathFiled);
				boolean find=false;
				if(field!=null&&field.size()>0)
				{
			         find=true;
				}
				else {
					 List con =astClass.findXpath(xPathCon);
					 if(con!=null&&con.size()>0)
					 {
				         find=true;
					 }
				}
				/**����ڶԷ������Ƿ��жԸ���ĳ�ʼ��*/
				if(find&&checkLoop(node,type.getImage(),astClass.getImage()))
				{
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("Class Loop Initialize.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
					list.add(fsminstance);
				}
			}
			
			
			
		}
		return list;
	}
	/**�����ڶԷ������Ƿ��жԸ���ĳ�ʼ��*/
	private static boolean checkLoop(SimpleJavaNode node,String className,String fieldName)
	{
		String fieldCondition =".//ClassOrInterfaceDeclaration[@Image='"+className+"']/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Static='false']/Type/ReferenceType/ClassOrInterfaceType[@Image='"+fieldName+"' and not(following-sibling::ArrayDimsAndInits)]";

        List result=null;	
		result=node.findXpath(fieldCondition);
		boolean find=false;
		for(Object o:result){
            ASTClassOrInterfaceType type=(ASTClassOrInterfaceType)o;
			
			ASTClassOrInterfaceDeclaration astClass=null;
			if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTClassOrInterfaceDeclaration )
				astClass=(ASTClassOrInterfaceDeclaration)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent().jjtGetParent());
			
			/**ȡ��������*/
			String varName="";
			if(type.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTFieldDeclaration)
			{
				if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0) instanceof ASTVariableDeclaratorId)
				{
					varName=((ASTVariableDeclaratorId)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0))).getImage();
				}
			}
			/**�������������ʼ��*/
			String xPathFiled=".//FieldDeclaration[@Static='false' and Type/ReferenceType/ClassOrInterfaceType[@Image='"+fieldName+"'] and VariableDeclarator[VariableDeclaratorId[@Image='"+varName+"'] and VariableInitializer" +
					          "/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+fieldName+"']and not(following-sibling::ArrayDimsAndInits)]]";

	        /**�����ڹ��캯�����ʼ��*/
	        String xPathCon=".//ConstructorDeclaration[.//StatementExpression[PrimaryExpression/PrimaryPrefix/Name[@Image='"+varName+"'] " +
	        		"and AssignmentOperator[@Image='=' ]and Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+fieldName+"' and not(following-sibling::ArrayDimsAndInits)]]]";
  
			if(astClass!=null){
				List field =astClass.findXpath(xPathFiled);
				if(field!=null&&field.size()>0)
				{
			         find=true;
			         break;
				}
				else {
					 List con =astClass.findXpath(xPathCon);
					 if(con!=null&&con.size()>0)
					 {
				         find=true;
				         break;
					 }
				}
			}
		}
		return find;
	}
}
