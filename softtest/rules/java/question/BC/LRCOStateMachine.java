package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * LRCOStateMachine
 * 检查在循环中移除集合中对象
 * 描述：一个集合collection或ArryList使用iterator，当循环没有结束的时候，从collection中移除了东西。如果调用next/previous/add/set/remove方法，则可能产生ConcurrentModificationException的异常。
 * 举例：
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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("在循环中移除集合中对象: 第%d 行在循环中移除集合中对象. \n" +
					"一个集合collection或ArryList使用iterator，当循环没有结束的时候，从collection中移除了东西。" +
					"如果调用next/previous/add/set/remove方法，则可能产生ConcurrentModificationException的异常", errorline);
		}else{
			f.format("Loop Remove Colection Object： Line %d applyed loop remove Colection Object.", errorline);
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
	 * @return List<FSMMachineInstance> 状态机实例列表
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
			/**判断是不是集合*/
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
            /**查找循环节点*/
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
           
            /**查找迭代器*/
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
        			/**判断循环中迭代器对应的变量跟remove方法对应的变量是不是同一个变量*/
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
            /**匹配forEach表达式*/
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
