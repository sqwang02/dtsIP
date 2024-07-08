package softtest.rules.java.question.BC;
import java.util.*;


import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * CSSStateMachine
 * ��������жϡ��������ķ�֧����ͬ�Ĵ���
 * �������������жϺͿ������ķ�֧�У�ʹ������ͬ�Ĵ��룬����һ�ֲ�̬�Ŀ���������
 ������
  ����1�� ���г���
   1   public void setValue(int a){
   2   		String s=����;
   3   		if(a<=0){
   4   			System.out.pritln(��The value is:��+s)
   5   		}
   6   		else{
   7   			System.out.pritln(��The value is:��+s)
   8   		}
   9   }     
���������4��7�У�if���������֧����������ͬ�Ĵ��롣
����2�� ���г���
   1   public void setValue(int a){
   2   		String s=����;
   3   		switch(a){
   4   		case 1:
   5   			System.out.pritln(��The value is:��+s);
   6   			break;
   7   		case 2:
   8   			System.out.pritln(��The value is:��+s);
   9   			break;
   10   	default:
   11   			System.out.pritln(��There not value ��);
   12   	}
   13   }




 * @author cjie
 * 
 */
public class CSSStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�����������ķ�֧����ͬ�Ĵ���: %d �з�֧��������ͬ�Ĵ���: \'%s\'", beginline,fsmmi.getResultString());
		}else{
			f.format("Conditions Has Same Code: At line %d  Two branch have the same code: \'%s\'.", beginline,fsmmi.getResultString());
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
	
	/**���ҷ�֧��������1��if�ڵ�*/
	private static String XPATH1=".//IfStatement[count(child::Statement)>1]";
	/**���ұ�ǩ��������1��switch�ڵ�*/
	private static String XPATH2=".//SwitchStatement[count(child::SwitchLabel)>1]";
	/**
	 * ���ܣ� ���������жϡ��������ķ�֧����ͬ�Ĵ���״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 */
	/**�Ƿ������жϡ��������ķ�֧����ͬ�Ĵ���*/
    public static boolean isRepeat=true; 
	public static List<FSMMachineInstance> createCSSStateMachine(SimpleJavaNode node, FSMMachine fsm)  {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH1);
		for(Object o:result){

			ASTIfStatement ifStatement=(ASTIfStatement)o;;

			if(ifStatement.jjtGetChild(1) instanceof ASTStatement&&ifStatement.jjtGetChild(2) instanceof ASTStatement)
			{
				ASTStatement st1=(ASTStatement) ifStatement.jjtGetChild(1);
				ASTStatement st2=(ASTStatement) ifStatement.jjtGetChild(2);				
				isRepeat=true;
				checkRepeatCode(st1,st2);
				if(isRepeat)
		    	{
		    		FSMMachineInstance fsminstance = fsm.creatInstance();
		    		if(Config.LANGUAGE==0)
		    			fsminstance.setResultString("If���ʹ������ͬ�Ĵ���");
		    		else
		    			fsminstance.setResultString("IfStatement used the same code");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(ifStatement));
					list.add(fsminstance);
		    	}
			}
		}		
		result=node.findXpath(XPATH2);
		for(Object o:result){

			ASTSwitchStatement switchStatement=(ASTSwitchStatement)o;
			/**��ű�ǩ�������Ӧ������*/
			Map<ASTSwitchLabel,List<ASTBlockStatement>> switchMap=new HashMap<ASTSwitchLabel,List<ASTBlockStatement>>();
			ASTSwitchLabel label=null;
			for(int i=1;i<switchStatement.jjtGetNumChildren();i++)
			{
				
				if(switchStatement.jjtGetChild(i) instanceof ASTSwitchLabel)
				{
					label=(ASTSwitchLabel) switchStatement.jjtGetChild(i);
						
				}
				else if(switchStatement.jjtGetChild(i) instanceof ASTBlockStatement)
				{
					List<ASTBlockStatement> blockList =switchMap.get(label);
					if(blockList==null)
					{
						blockList=new ArrayList<ASTBlockStatement> ();
					}
					blockList.add((ASTBlockStatement) switchStatement.jjtGetChild(i));
					switchMap.put(label, blockList);
				} 
			}
			ASTSwitchLabel[] labels=new ASTSwitchLabel[switchMap.keySet().size()];
			labels=(ASTSwitchLabel[]) switchMap.keySet().toArray(labels);
			for(int i=0;i<labels.length;i++)
			{
				for(int j=i+1;j<labels.length;j++)
				{
				    /**�ж�����label��Ӧ�������Ƿ���ͬ*/
					List<ASTBlockStatement> list1=switchMap.get(labels[i]);
					List<ASTBlockStatement> list2=switchMap.get(labels[j]);
					isRepeat=true;
					int labelName1=labels[i].getBeginLine();
					int labelName2=labels[j].getBeginLine();
					if(list1.size()==list2.size())
					{
						for(int k=0;k<list1.size();k++)
						{
							checkRepeatCode(list1.get(k),list2.get(k));
						}						
					}
					else
					{
						isRepeat=false;
					}
					if(isRepeat)
			    	{
			    		FSMMachineInstance fsminstance = fsm.creatInstance();
			    		if(Config.LANGUAGE==0)
			    			fsminstance.setResultString("Switch��� ��ǩ:��"+labelName1+"�� �� ��ǩ:��"+labelName2+"�� ʹ������ͬ�Ĵ���");
			    		else
			    			fsminstance.setResultString("Switch used the same codes in label:line-"+labelName1+" and label:line-"+labelName2);
						fsminstance.setRelatedObject(new FSMRelatedCalculation(switchStatement));
						list.add(fsminstance);
			    	}
				}
			}
		}		
	   return list;
	}
	/**
	 * ��������ڵ��Ƿ���ͬ��Ĭ��TRUE
	 *
	 * @param ASTClassOrInterfaceDeclaration 
	 */
	public static void  checkRepeatCode(SimpleJavaNode node1,SimpleJavaNode node2)
	{
		 /**�жϽڵ������Ƿ���ͬ*/
         if(!(node1.getClass()==node2.getClass()))
         {
        	 isRepeat=false;
        	 return;
         }
         else
         {
        	 /**�����Boolean����*/
        	 if(node1.getClass()==ASTBooleanLiteral.class)
        	 {
        		 if(!(((ASTBooleanLiteral)node1).isTrue()==((ASTBooleanLiteral)node2).isTrue()))
        		 {
        	    	 isRepeat=false;
        	    	 return;
        		 }
        	 }
        	 /**�жϽڵ��image�Ƿ���ͬ*/
        	 if(node1.getImage()==null&&node2.getImage()==null
        			 ||node1.getImage()!=null&&node2.getImage()!=null&&node1.getImage().equals(node2.getImage()))
        	 {
        		 /**�жϵ�ǰ�ڵ���ӽڵ�����Ƿ���ͬ*/
        	     if(node1.jjtGetNumChildren()!=node2.jjtGetNumChildren())
        	     {
        	    	 isRepeat=false;
        	    	 return;
        	     }
        	     /**�ݹ��ж��ӽڵ��Ƿ���ͬ*/
        	     for(int i=0;i<node1.jjtGetNumChildren();i++)
        	     {
        	    	 checkRepeatCode((SimpleJavaNode)node1.jjtGetChild(i),(SimpleJavaNode)node2.jjtGetChild(i));
        	     }
        	 }
        	 else
        	 {
        		 isRepeat=false;
    	    	 return;
        	 }
         }
	}
}
