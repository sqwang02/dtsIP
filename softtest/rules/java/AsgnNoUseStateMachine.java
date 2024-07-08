package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.symboltable.java.NameOccurrence;

import java.util.*;

import softtest.ast.java.*;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
定义6.3：此类缺陷指的是给一个变量进行了赋值但却没有读取其值。
1. 变量被赋值却没有使用
【例6-4】 下列程序：
   1   private long foo(long arr[]) {
   2   	  for (int i = 0; i < arr.length; i++) {
   3   		long l = arr[i];
   4   		boolean is = isCaseSensitive(arr[i]);
   5   		// not optimal or missing usage or 'is'
   6   		if (i % 2 == 0) return l;
   7   	  }
   8   	  return 0;
   9   	}
2. 变量使用前重新赋值
【例6-5】 下列程序：
   1   private long foo(long arr[]) {
   2   		long time1 = 0;
   3   		long time2 = 0;
   4   		long len = 0;
   5   		for (int i = 0; i < arr.length; i += 2) {
   6   			time1 = arr[i];
   7   			time1 = arr[i + 1];
   8   			if (time1 < time2) {
   9   				long d = time1 - time2;
   10   				if (d > len) len = d;
   11   			}
   12   		}
   13   		return len;
   14   }

2008-3-24
 */

public class AsgnNoUseStateMachine {

	public static List<FSMMachineInstance> createAsgnNoUseStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		FSMMachineInstance fsmInst = fsm.creatInstance();
		fsmInst.setRelatedObject(new FSMRelatedCalculation(node));
		list.add(fsmInst);
		return list;
	}
	
	
	public static boolean checkAsgnNoUse(VexNode vex,FSMMachineInstance fsmInst) {
		boolean found = false;

		ArrayList occrList = vex.getOccurrences();
		for(int i = 0; i < occrList.size(); i++) {
			NameOccurrence occ = (NameOccurrence)occrList.get(i);
			List useList = occ.getDefUseList();
			if( occ.getOccurrenceType() == NameOccurrence.OccurrenceType.DEF ) {
				if( useList == null || useList.size() == 0 ) {
					found = true;
					fsmInst.setResultString(occ.getImage());
					
					// 如果定义－取消定义链不为空，说明存在重复赋值
					List<NameOccurrence> undeflist=occ.getDefUndefList();
					if(undeflist!=null){
						// 如果是自增、减运算，则排除
						for(NameOccurrence o:undeflist){
							if(o.isSelfIncOrDec()) {
								found = false;
								break;
							}
						}
					}
					// 如果是循环中，因为定义－取消定义链为空，但是仍要检测
					// 1、在循环的开始处第一次该变量出现(非本次出现)是定义出现
					// 2、在循环的开始处第一次该变量出现(非本次出现)是使用出现
					
					if(found) {
						logc2("+----------------+");
						logc2("| checkAsgnNoUse | :" + occ.getImage() + " (" + occ.getLocation().getBeginLine() + "," + occ.getLocation().getBeginColumn()+ ")" );
						logc2("+----------------+");
						logc2("" + (useList==null?null:useList.size()));
						found = true;
						fsmInst.setResultString("assign to " + occ.getImage() + " but NoUse");
						break;
					}
				}
			} else {
				logc2("not def:" + occ.getImage());
			}
		}
		return found;
	}
	
	public static void logc1(String str) {
		logc("createAsgnNoUseStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkAsgnNoUse(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("AsgnNoUseStateMachine::" + str);
		}
	}
}
