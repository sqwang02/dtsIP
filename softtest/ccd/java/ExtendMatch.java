package softtest.ccd.java;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 一个拓展的匹配，包括一个OneMatch的对象作为拓展匹配的核心，以及在匹配范围之外
 * 的若干语句
 *        around1 |OneMatch| around1
 *        around2 |  core  | around2
 */
public class ExtendMatch implements Comparable {

	//private Set markSet = new TreeSet();

	//private Statement[] marks = new Statement[2];
	
	/**
	 * 存储mark1的 前后相关 的语句在语句串中的序号（索引）
	 */
	private Set<Integer>  arounds1 = new HashSet<Integer>();
	
	private Set<Integer>  arounds2 = new HashSet<Integer>();
	
	private MatchCode mc;

	/**
	 * 连续匹配的部分，称为 核
	 */
	private OneMatch core;
	
	
	public ExtendMatch(OneMatch match) {
		this.core = match;
	}
	
	public int compareTo(Object o) {
		ExtendMatch other = (ExtendMatch) o;
		int diff = 0;//other.getStmtCount() - getStmtCount();
		if (diff != 0) {
			return diff;
		}
		return other.getFirstMark().getIndex() - getFirstMark().getIndex();
	}
	
	public Statement getFirstMark() {
		return core.getFirstMark();
	}

	public Statement getSecondMark() {
		return core.getSecondMark();
	}

	public Set<Integer>  getArounds1() {
		return arounds1;
	}
	
	public Set<Integer>  getArounds2() {
		return arounds2;
	}
	
	public void  addArounds1(int i) {
		arounds1.add(i);
	}

	public void  addArounds2(int i) {
		arounds2.add(i);
	}
	
	@Override
	public String toString() {
		List<Integer> list1 = new ArrayList<Integer>(arounds1.size());
		List<Integer> list2 = new ArrayList<Integer>(arounds2.size());
		list1.addAll(arounds1);
		list2.addAll(arounds2);
		Collections.sort(list1);
		Collections.sort(list2);
		
		StringBuffer sb = new StringBuffer();
		sb.append(core.toString());
		sb.append("\r\n1[");
		//for(Iterator<Integer> iter = arounds1.iterator(); iter.hasNext(); ) {
		for(Iterator<Integer> iter = list1.iterator(); iter.hasNext(); ) {
			int val = iter.next();
			//sb.append(val + "("+StmtsSliceMatcher.statements.get(val).getBeginLine()+"),");
			sb.append(StmtsSliceMatcher2.statements.get(val).getBeginLine()+",");
		}
		sb.append("]");
		sb.append("\r\n2[");
		//for(Iterator<Integer> iter = arounds2.iterator(); iter.hasNext(); ) {
		for(Iterator<Integer> iter = list2.iterator(); iter.hasNext(); ) {
			int val = iter.next();
			//sb.append(val + "("+StmtsSliceMatcher.statements.get(val).getBeginLine()+"),");
			sb.append(StmtsSliceMatcher2.statements.get(val).getBeginLine()+",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public MatchCode getMatchCode() {
        if (mc == null) {
            mc = new MatchCode(core.getFirstMark(), core.getSecondMark());
        }
        return mc;
    }
	
	public int getCoreLen() {
		return core.getStmtCount();
	}
}