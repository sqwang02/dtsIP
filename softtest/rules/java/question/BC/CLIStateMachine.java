package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * CLIStateMachine
 * 检查类的循环初始化
 * 描述:两个类当中，分别有对对方类的实例初始化的代码。
 举例：
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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("类的循环初始化: %d 行产生了对类的循环初始化,这样可能造成死循环，然后导致内存溢出。", errorline);
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
	/**匹配类属性*/
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
			
			/**取得属性名*/
			String varName="";
			if(type.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTFieldDeclaration)
			{
				if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0) instanceof ASTVariableDeclaratorId)
				{
					varName=((ASTVariableDeclaratorId)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0))).getImage();
				}
			}
			/**查找在属性里初始化*/
			String xPathFiled=".//FieldDeclaration[@Static='false' and Type/ReferenceType/ClassOrInterfaceType[@Image='"+type.getImage()+"'] and VariableDeclarator[VariableDeclaratorId[@Image='"+varName+"'] and VariableInitializer" +
					          "/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+type.getImage()+"' and not(following-sibling::ArrayDimsAndInits)]]]";

			/**查找在构造函数里初始化*/
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
				/**检查在对方类里是否有对该类的初始化*/
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
	/**查找在对方类里是否有对该类的初始化*/
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
			
			/**取得属性名*/
			String varName="";
			if(type.jjtGetParent().jjtGetParent().jjtGetParent() instanceof ASTFieldDeclaration)
			{
				if(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0) instanceof ASTVariableDeclaratorId)
				{
					varName=((ASTVariableDeclaratorId)(type.jjtGetParent().jjtGetParent().jjtGetParent().jjtGetChild(1).jjtGetChild(0))).getImage();
				}
			}
			/**查找在属性里初始化*/
			String xPathFiled=".//FieldDeclaration[@Static='false' and Type/ReferenceType/ClassOrInterfaceType[@Image='"+fieldName+"'] and VariableDeclarator[VariableDeclaratorId[@Image='"+varName+"'] and VariableInitializer" +
					          "/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='"+fieldName+"']and not(following-sibling::ArrayDimsAndInits)]]";

	        /**查找在构造函数里初始化*/
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
