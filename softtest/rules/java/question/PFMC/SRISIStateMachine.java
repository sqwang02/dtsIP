package softtest.rules.java.question.PFMC;

import java.util.*;



import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.TypeSet;


/**
 * SRISIStateMachine
 * 检查子类是否重复实现父类实现的接口
 * 描述：父类实现了一个接口，其子类也声明为实现了该接口，这是无意义的，只要父类实现了接口，子类自动继承，无需去显式声明。
 举例：
   1	public SuperClass implements ObjectOutput{}
   2   public ThisClass extends SuperClass implements ObjectOutput{}
   应警告：ThisClass 重复实现了父类实现的接口。



 * @author cjie
 * 
 */
public class SRISIStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("子类重复实现父类的接口: %d 行上定义的类  \'%s\' ,重复实现了父类的接口", errorline,fsmmi.getResultString());
		}else{
			f.format("SubClass Repeatedly implements The Interface Which SuperClass Implements: the Class \'%s\' defined in line %d "+
				"implements the superclass's interface repeatedly.", fsmmi.getResultString(),errorline);
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
	
	private static String XPATH=".//TypeDeclaration/ClassOrInterfaceDeclaration[child::ExtendsList and child::ImplementsList]";
	
	/**
	 * 功能： 创建子类重复实现父类实现的接口
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 * @throws ClassNotFoundException
	 */
	/**是否子类重复实现父类实现的接口*/
    public static boolean isRepeat=false; 
	public static List<FSMMachineInstance> createSRISIStateMachine(SimpleJavaNode node, FSMMachine fsm) throws ClassNotFoundException {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH);
		for(Object o:result){

			ASTClassOrInterfaceDeclaration classDeclaration=(ASTClassOrInterfaceDeclaration)o;
			int index = 0;
			ASTClassOrInterfaceType extNode = null;
			// such as class C extends B implements A
			if (classDeclaration.jjtGetChild(0).jjtGetChild(0) instanceof ASTClassOrInterfaceType) {
			    extNode=(ASTClassOrInterfaceType) classDeclaration.jjtGetChild(0).jjtGetChild(0);
			    index=1;
			} else if (classDeclaration.jjtGetChild(1).jjtGetChild(0) instanceof ASTClassOrInterfaceType) {
				// such as class C<T> extends B implements A
				extNode=(ASTClassOrInterfaceType) classDeclaration.jjtGetChild(1).jjtGetChild(0);
				index=2;
			}
			
			if (extNode != null) {
				ASTImplementsList impNode=(ASTImplementsList) classDeclaration.jjtGetChild(index);
				List<ASTClassOrInterfaceType> impList=new ArrayList<ASTClassOrInterfaceType> ();
				for(int i=0;i<impNode.jjtGetNumChildren();i++)
				{
					if(impNode.jjtGetChild(i) instanceof ASTClassOrInterfaceType)
					{
						impList.add((ASTClassOrInterfaceType) impNode.jjtGetChild(i));
					}
				}
				isRepeat=false;
				checkRepeatImplementation(node,impList,extNode,null);
				if(isRepeat)
		    	{
		    		FSMMachineInstance fsminstance = fsm.creatInstance();
		    		fsminstance.setResultString(classDeclaration.getImage());
					fsminstance.setRelatedObject(new FSMRelatedCalculation(classDeclaration));
					list.add(fsminstance);
		    	}
			}

		}			
	   return list;
	}
	/**
	 * 
	 * @param currentImpl 实现列表
	 * @param parentNode 继承节点
	 * @param 
	 */
	public static void  checkRepeatImplementation(SimpleJavaNode node,List<ASTClassOrInterfaceType> currentImpl,SimpleJavaNode parentNode,Class superClass)
	{
		 List parents=null;
         if(parentNode!=null &&parentNode instanceof ASTClassOrInterfaceType)
         {
        	 /**匹配父类对应的类的定义节点*/
        	 parents=node.findXpath(".//TypeDeclaration/ClassOrInterfaceDeclaration[@Image='"+parentNode.getImage()+"']");
         }
         /**父类的实现在当前文件中*/
         if(parents!=null&&parents.size()>0)
         {
        	 
        	 if(parents.get(0) instanceof ASTClassOrInterfaceDeclaration)
             {
        		 parentNode=(ASTClassOrInterfaceDeclaration) parents.get(0);
        		 ASTImplementsList impNode=null;
        		 for(int i=0;i<parentNode.jjtGetNumChildren();i++)
        		 {
        			 if(parentNode.jjtGetChild(i) instanceof ASTImplementsList)
        			 {
        				 impNode=(ASTImplementsList) parentNode.jjtGetChild(i) ;
        			 }
        		 }
        		 if(impNode!=null)
        		 {
        			 /**判断是否实现了相同的接口*/
        			 for(int i=0;i<impNode.jjtGetNumChildren();i++)
          			 {
        				
          				if(impNode.jjtGetChild(i) instanceof ASTClassOrInterfaceType)
          				{
          					String image= ((ASTClassOrInterfaceType)impNode.jjtGetChild(i)).getImage();
          					for(ASTClassOrInterfaceType c:currentImpl)
						   {
								if(c.getImage().equals(image))
								{
									isRepeat= true;
		      						return;
								}
						   }
          				}
          			 }
        		 }    			 
       			checkRepeatImplementation(node,currentImpl,parentNode,null);
        	 }
         }
         /**父类的实现不在当前文件中*/
         else
         {
        	 TypeSet typeset=TypeSet.getCurrentTypeSet();
        	 try {
        		 Class opt=null;
        		 if(parentNode!=null)
        		 {
	    			 opt=typeset.findClass(parentNode.getImage());
        		 }
        		 else
        		 {
        			 opt=superClass;
        		 }
				if(opt!=null)
				{
					Class[] interfaces=opt.getInterfaces();
					/**判断是否实现了相同的接口*/
					for(Class clazz:interfaces)
					{
						String image=clazz.getName().lastIndexOf(".")>0?clazz.getName().substring(clazz.getName().lastIndexOf(".")+1, clazz.getName().length()):clazz.getName();
						for(ASTClassOrInterfaceType c:currentImpl)
						{
							if(c.getImage().equals(image))
							{
								isRepeat= true;
	      						return;
							}
						}
					}
					checkRepeatImplementation(node,currentImpl,null,opt.getSuperclass());
				}
			} catch (ClassNotFoundException e) {				
				e.printStackTrace();
			}
         }
     
	}
}
