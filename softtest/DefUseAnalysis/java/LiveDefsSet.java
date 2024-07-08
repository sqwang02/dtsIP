package softtest.DefUseAnalysis.java;

import java.util.*;

import softtest.ast.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.symboltable.java.*;

public class LiveDefsSet {
	/** 变量到达定义出现哈希表 */
	Hashtable<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>> table = new Hashtable<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>>();

	/** 获得变量到达定义出现哈希表 */
	public Hashtable<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>> getTable() {
		return table;
	}

	/** 设置变量到达定义出现哈希表 */
	public void setTable(Hashtable<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>> table) {
		this.table = table;
	}

	/** 设置变量的新定义 */
	public void setNewDef(NameOccurrence occ) {
		if (occ.getOccurrenceType() != NameOccurrence.OccurrenceType.DEF) {
			return;
		}
		if (occ.getDeclaration() == null || !(occ.getDeclaration() instanceof VariableNameDeclaration)) {
			return;
		}
		VariableNameDeclaration v = (VariableNameDeclaration) occ.getDeclaration();
		Hashtable<SimpleNode, NameOccurrence> occs = table.get(v);
		if (occs == null) {
			occs = new Hashtable<SimpleNode, NameOccurrence>();
			table.put(v, occs);
		}

		for (Enumeration<NameOccurrence> e = occs.elements(); e.hasMoreElements();) {
			NameOccurrence o = e.nextElement();
			o.addDefUndef(occ);
			occ.addUndefDef(o);
		}
		occs.clear();
		occs.put(occ.getLocation(), occ);
	}

	/** 增加变量到达定义出现 */
	public void addLiveDef(NameOccurrence occ) {
		if (occ.getOccurrenceType() != NameOccurrence.OccurrenceType.DEF) {
			return;
		}
		if (occ.getDeclaration() == null || !(occ.getDeclaration() instanceof VariableNameDeclaration)) {
			return;
		}
		VariableNameDeclaration v = (VariableNameDeclaration) occ.getDeclaration();
		Hashtable<SimpleNode, NameOccurrence> occs = table.get(v);
		if (occs == null) {
			occs = new Hashtable<SimpleNode, NameOccurrence>();
			table.put(v, occs);
		}
		occs.put(occ.getLocation(), occ);
	}

	/** 合并到达定义表 */
	public void mergeLiveDefs(LiveDefsSet set) {
		Set<Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>>> entryset = set.getTable().entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>>> i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>> entry = i.next();
			// VariableNameDeclaration v=entry.getKey();
			Hashtable<SimpleNode, NameOccurrence> occs = entry.getValue();
			for (Enumeration<NameOccurrence> e = occs.elements(); e.hasMoreElements();) {
				NameOccurrence o = e.nextElement();
				addLiveDef(o);
			}
		}
	}

	/** 获得变量的到达定义 */
	public ArrayList<NameOccurrence> getVariableLiveDefs(VariableNameDeclaration v) {
		ArrayList<NameOccurrence> list = new ArrayList<NameOccurrence>();
		Hashtable<SimpleNode, NameOccurrence> occs = table.get(v);
		if (occs != null) {
			for (Enumeration<NameOccurrence> e = occs.elements(); e.hasMoreElements();) {
				NameOccurrence o = e.nextElement();
				list.add(o);
			}
		}
		return list;
	}
	
	/** 打印 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		Set<Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>>> entryset = getTable().entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>>> i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, Hashtable<SimpleNode, NameOccurrence>> entry = i.next();
			VariableNameDeclaration v=entry.getKey();
			b.append("   "+v.getImage()+":");
			Hashtable<SimpleNode, NameOccurrence> occs = entry.getValue();
			for (Enumeration<NameOccurrence> e = occs.elements(); e.hasMoreElements();) {
				NameOccurrence o = e.nextElement();
				b.append(" ["+o.getLocation().getBeginLine()+","+o.getLocation().getBeginColumn()+"]");
			}
		}
		return b.toString();
	}
}
