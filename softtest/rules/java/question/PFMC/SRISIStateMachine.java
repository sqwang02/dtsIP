package softtest.rules.java.question.PFMC;

import java.util.*;



import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.TypeSet;


/**
 * SRISIStateMachine
 * ��������Ƿ��ظ�ʵ�ָ���ʵ�ֵĽӿ�
 * ����������ʵ����һ���ӿڣ�������Ҳ����Ϊʵ���˸ýӿڣ�����������ģ�ֻҪ����ʵ���˽ӿڣ������Զ��̳У�����ȥ��ʽ������
 ������
   1	public SuperClass implements ObjectOutput{}
   2   public ThisClass extends SuperClass implements ObjectOutput{}
   Ӧ���棺ThisClass �ظ�ʵ���˸���ʵ�ֵĽӿڡ�



 * @author cjie
 * 
 */
public class SRISIStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�����ظ�ʵ�ָ���Ľӿ�: %d ���϶������  \'%s\' ,�ظ�ʵ���˸���Ľӿ�", errorline,fsmmi.getResultString());
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
	 * ���ܣ� ���������ظ�ʵ�ָ���ʵ�ֵĽӿ�
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 * @throws ClassNotFoundException
	 */
	/**�Ƿ������ظ�ʵ�ָ���ʵ�ֵĽӿ�*/
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
	 * @param currentImpl ʵ���б�
	 * @param parentNode �̳нڵ�
	 * @param 
	 */
	public static void  checkRepeatImplementation(SimpleJavaNode node,List<ASTClassOrInterfaceType> currentImpl,SimpleJavaNode parentNode,Class superClass)
	{
		 List parents=null;
         if(parentNode!=null &&parentNode instanceof ASTClassOrInterfaceType)
         {
        	 /**ƥ�丸���Ӧ����Ķ���ڵ�*/
        	 parents=node.findXpath(".//TypeDeclaration/ClassOrInterfaceDeclaration[@Image='"+parentNode.getImage()+"']");
         }
         /**�����ʵ���ڵ�ǰ�ļ���*/
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
        			 /**�ж��Ƿ�ʵ������ͬ�Ľӿ�*/
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
         /**�����ʵ�ֲ��ڵ�ǰ�ļ���*/
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
					/**�ж��Ƿ�ʵ������ͬ�Ľӿ�*/
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
