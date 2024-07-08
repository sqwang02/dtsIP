package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.symboltable.java.NameOccurrence;

import java.util.*;

import softtest.ast.java.*;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
����6.3������ȱ��ָ���Ǹ�һ�����������˸�ֵ��ȴû�ж�ȡ��ֵ��
1. ��������ֵȴû��ʹ��
����6-4�� ���г���
   1   private long foo(long arr[]) {
   2   	  for (int i = 0; i < arr.length; i++) {
   3   		long l = arr[i];
   4   		boolean is = isCaseSensitive(arr[i]);
   5   		// not optimal or missing usage or 'is'
   6   		if (i % 2 == 0) return l;
   7   	  }
   8   	  return 0;
   9   	}
2. ����ʹ��ǰ���¸�ֵ
����6-5�� ���г���
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
					
					// ������壭ȡ����������Ϊ�գ�˵�������ظ���ֵ
					List<NameOccurrence> undeflist=occ.getDefUndefList();
					if(undeflist!=null){
						// ����������������㣬���ų�
						for(NameOccurrence o:undeflist){
							if(o.isSelfIncOrDec()) {
								found = false;
								break;
							}
						}
					}
					// �����ѭ���У���Ϊ���壭ȡ��������Ϊ�գ�������Ҫ���
					// 1����ѭ���Ŀ�ʼ����һ�θñ�������(�Ǳ��γ���)�Ƕ������
					// 2����ѭ���Ŀ�ʼ����һ�θñ�������(�Ǳ��γ���)��ʹ�ó���
					
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
