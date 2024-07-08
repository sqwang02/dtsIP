package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * LRCOStateMachine
 * �����ѭ�����Ƴ������ж���
 * ������һ������collection��ArryListʹ��iterator����ѭ��û�н�����ʱ�򣬴�collection���Ƴ��˶������������next/previous/add/set/remove����������ܲ���ConcurrentModificationException���쳣��
 * ������
   1   void fixList(Collection col) {
   2   		for (Iterator iter = col.iterator(); iter.hasNext();) {
   3   			String el = (String) iter.next();
   4   			if (el.startsWith("/")) {
   5   				col.remove(el);
   6   			}
   7   		}
   8   }


 * @author cjie
 * 
 */
public class LRCOStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ѭ�����Ƴ������ж���: ��%d ����ѭ�����Ƴ������ж���. \n" +
					"һ������collection��ArryListʹ��iterator����ѭ��û�н�����ʱ�򣬴�collection���Ƴ��˶�����" +
					"�������next/previous/add/set/remove����������ܲ���ConcurrentModificationException���쳣", errorline);
		}else{
			f.format("Loop Remove Colection Object�� Line %d applyed loop remove Colection Object.", errorline);
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
	
	private static String XPATH=".//*[self::ForStatement or self::WhileStatement or self::DoStatement]/Statement//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'.*\\.remove$')]";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createLRCOStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTName name=(ASTName)o;
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration))
			{
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			/**�ж��ǲ��Ǽ���*/
			Class superclass=v.getType();
			boolean isColl=false;
			if(superclass==Collection.class)
				isColl=true;
			while (!isColl&&superclass!=null&&!(superclass == Collection.class)) {
				Class[] lists=superclass.getInterfaces();
				for(Class c:lists)
				{
					if(c==Collection.class)
					{
						isColl=true;
						break;
					}
				}
				superclass=superclass.getSuperclass();
			}
            if(!isColl)
            	continue;
            /**����ѭ���ڵ�*/
            ASTForStatement forSt=null;
            ASTWhileStatement whileSt=null;
            ASTDoStatement doSt=null;
            SimpleJavaNode parent=(SimpleJavaNode) name.jjtGetParent();
            while(parent!=null)
            {
            	if(parent instanceof ASTForStatement)
            	{
            		forSt=(ASTForStatement) parent;
            		break;
            	}
            	else if(parent instanceof ASTWhileStatement)
            	{
            		whileSt=(ASTWhileStatement) parent;
            		break;
            	}
            	else if(parent instanceof ASTDoStatement)
            	{
            		doSt=(ASTDoStatement) parent;
            		break;
            	}
            	parent=(SimpleJavaNode) parent.jjtGetParent();
            }
           
            /**���ҵ�����*/
            List namelst=null;
            List namelstForEach=null;
            if(forSt!=null)
            {
            	namelst=forSt.findXpath("Expression//Name[matches(@Image,'.*\\.hasNext$')]");
            	namelstForEach=forSt.findXpath("Expression/PrimaryExpression/PrimaryPrefix/Name");
            }
            if(whileSt!=null)
            {
            	namelst=whileSt.findXpath("Expression//Name[matches(@Image,'.*\\.hasNext$')]");
            }
            if(doSt!=null)
            {
            	namelst=doSt.findXpath("Expression//Name[matches(@Image,'.*\\.hasNext$')]");
            	
            }
            boolean isError=false;
            if(!isError&&namelst!=null)
            {
            	for(Object r:namelst){
        			ASTName nameIter=(ASTName)r;
        			if(!(nameIter.getNameDeclaration() instanceof VariableNameDeclaration))
        			{
        				continue;
        			}
        			VariableNameDeclaration vIter = (VariableNameDeclaration) nameIter.getNameDeclaration();
        			/**�ж�ѭ���е�������Ӧ�ı�����remove������Ӧ�ı����ǲ���ͬһ������*/
        			SimpleJavaNode iter=(SimpleJavaNode) vIter.getNode();
        			if(iter.jjtGetParent() instanceof ASTVariableDeclarator)
        			{
        				ASTVariableDeclarator varNode=(ASTVariableDeclarator) iter.jjtGetParent();
        				List iterIns=varNode.findXpath(".//VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Name");
        				for(Object c:iterIns){
                			ASTName nameCol=(ASTName)c;
                			if(!(nameCol.getNameDeclaration() instanceof VariableNameDeclaration))
                			{
                				continue;
                			}
                			VariableNameDeclaration vCol = (VariableNameDeclaration) nameCol.getNameDeclaration();
                			if(vCol.equals(v))
                			{
                				isError=true;
                				break;
                			}
                			
        				}
        			}
            	}
            }
            /**ƥ��forEach���ʽ*/
            if(!isError&&namelstForEach!=null)
            {
            	for(Object c:namelstForEach){
        			ASTName nameCol=(ASTName)c;
        			if(!(nameCol.getNameDeclaration() instanceof VariableNameDeclaration))
        			{
        				continue;
        			}
        			VariableNameDeclaration vCol = (VariableNameDeclaration) nameCol.getNameDeclaration();
        			if(vCol.equals(v))
        			{
        				isError=true;
        				break;
        			}
        			
				}
            }
            if(!isError)
            	continue;
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Loop Remove Colection Object");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}

}
