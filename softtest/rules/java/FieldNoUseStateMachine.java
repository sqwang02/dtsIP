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
六
4. 定义私有成员（域）变量但在类的方法中没有出现（使用）
【例6-5-4】 下列程序：
  1   class Car {
  2      private double speed;
   3      private long foo(long arr[]) {
   4   		long time1 = 0;
   5   		return time1;
   6      }
   7   }
上出程序第2行中的speed属于私有访问权限而且在类的方法中从未使用。

2008-3-28
 */

public class FieldNoUseStateMachine {
	
	public static List<FSMMachineInstance> createFieldNoUseStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List evalRlts = new ArrayList();

		// ///////////// Process Field Member //////////////

		String fieldstr = ".//ClassOrInterfaceDeclaration/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Private=\'true\']/VariableDeclarator/VariableDeclaratorId";

		try {
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		Iterator i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			if (id.getNameDeclaration() instanceof VariableNameDeclaration) {
				VariableNameDeclaration v = id.getNameDeclaration();
				Map map=null;
				map=v.getDeclareScope().getVariableDeclarations();
				if(map != null){
					List occs=(ArrayList)map.get(v);
					if(occs.size()==0){
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(id));
						fsminstance.setResultString(id.getImage());
						list.add(fsminstance);
					}
				} else {
					logc(" map is null ?????????? ");
				}
			}
		}

		return list;
	}
	
	
	public static boolean checkFieldNoUse(VexNode vex,FSMMachineInstance fsmInst) {

		return true;
	}
	
		
	public static void logc1(String str) {
		logc("createFieldNoUseStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkFieldNoUse(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("FieldNoUseStateMachine::" + str);
		}
	}
}
