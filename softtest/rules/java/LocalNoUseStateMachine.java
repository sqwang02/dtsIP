package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
3. 变量被声明但没有使用（）
   1   private long foo(long arr[]) {
   2   		long time1 = 0;
   3   		return 0;
   4   }
上出程序第2行中的time1定义后从未使用。
2008-3-28
 */

public class LocalNoUseStateMachine {
	
	public static List getTreeNode(SimpleJavaNode node, String xstr) {
		List evalRlts; 
		try {
			XPath xpath = new BaseXPath(xstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createLocalNoUseStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		String xlocalStr = ".//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId";
		List evalRlts = getTreeNode(node, xlocalStr);
		
		Iterator i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			if (id.getNameDeclaration() instanceof VariableNameDeclaration) {
				VariableNameDeclaration v = id.getNameDeclaration();
				Map map=null;
				map=v.getDeclareScope().getVariableDeclarations();
				if(map!=null){
					List occs=(ArrayList)map.get(v);
					if(occs.size()==0){
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
						fsminstance.setResultString(id.getImage());
						list.add(fsminstance);
					}
				}
			}
		}

		// 参数未使用
		String arguStr = ".//FormalParameters/FormalParameter/VariableDeclaratorId";
		evalRlts = getTreeNode(node, arguStr);
		i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			if (id.getNameDeclaration() instanceof VariableNameDeclaration) {
				VariableNameDeclaration v = id.getNameDeclaration();
				Map map=null;
				try{
					map=v.getDeclareScope().getVariableDeclarations();
				}catch(Exception e){
					
				}
				if(map!=null){
					List occs=(ArrayList)map.get(v);
					if(occs.size()==0){
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
						fsminstance.setResultString(id.getImage() + " argument unused");
						list.add(fsminstance);
					}
				}
			}
		}
		
		return list;
	}
	
	
	public static boolean checkLocalNoUse(VexNode vex,FSMMachineInstance fsmInst) {

		return true;
	}
	
		
	public static void logc1(String str) {
		logc("createLocalNoUseStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkLocalNoUse(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("LocalNoUseStateMachine::" + str);
		}
	}
}
