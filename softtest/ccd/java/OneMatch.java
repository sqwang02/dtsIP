package softtest.ccd.java;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class OneMatch  implements Comparable {

    private int stmtCount;
    
    private Set markSet = new TreeSet();
    private Statement[] marks = new Statement[2];
    //private String code;
    private MatchCode mc;
    //private String label;
    
    /*
    public static final Comparator MatchesComparator = new Comparator() {
    	public int compare(Object a, Object b) {
    		OneMatch ma = (OneMatch)a;
    		OneMatch mb = (OneMatch)b;
    		return mb.getMarkCount() - ma.getMarkCount();
    	}
    };
    
    public static final Comparator LinesComparator = new Comparator() {
    	public int compare(Object a, Object b) {
    		OneMatch ma = (OneMatch)a;
    		OneMatch mb = (OneMatch)b;
    		
    		return mb.getLineCount() - ma.getLineCount();
    	}
    };
    
    public static final Comparator LabelComparator = new Comparator() {
    	public int compare(Object a, Object b) {
    		OneMatch ma = (OneMatch)a;
    		OneMatch mb = (OneMatch)b;
    		if (ma.getLabel() == null) return 1;
    		if (mb.getLabel() == null) return -1;
    		return mb.getLabel().compareTo(ma.getLabel());
    	}
    };
    
    public static final Comparator LengthComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
        	OneMatch m1 = (OneMatch) o1;
            OneMatch m2 = (OneMatch) o2;
            return m2.getLineCount() - m1.getLineCount();
        }
    };
    */

    public OneMatch(int stmtCount, Statement first, Statement second) {
        markSet.add(first);
        markSet.add(second);
        marks[0] = first;
        marks[1] = second;
        this.stmtCount = stmtCount;
    }

    public int getMarkCount() {
        return markSet.size();
    }

    public int getStmtCount() {
        return this.stmtCount;
    }

    /*
    public String getSourceCodeSlice() {
        return this.code;
    }

    public void setSourceCodeSlice(String code) {
        this.code = code;
    }
	*/
    public Iterator iterator() {
        return markSet.iterator();
    }

    public int compareTo(Object o) {
        OneMatch other = (OneMatch) o;
        int diff = other.getStmtCount() - getStmtCount();
        if (diff != 0) {
            return diff;
        }
        return other.getFirstMark().getIndex() - getFirstMark().getIndex();
    }

    public Statement getFirstMark() {
        return marks[0];
    }

    public Statement getSecondMark() {
        return marks[1];
    }

    @Override
	public String toString() {
        //return "Match: " + PMD.EOL + "tokenCount = " + tokenCount + PMD.EOL + "marks = " + markSet.size();
    	return "(Len=" + stmtCount + ", marks=" + markSet.size()
    	+ " " + marks[0].getBeginLine() + "<->" + marks[1].getBeginLine() + " mc:" + this.getMatchCode();
    }

    public Set getMarkSet() {
        return markSet;
    }

    public MatchCode getMatchCode() {
        if (mc == null) {
            mc = new MatchCode(marks[0], marks[1]);
        }
        return mc;
    }

    public int getEndIndex() {
        return marks[1].getIndex() + getStmtCount() - 1;
    }

    public void setMarkSet(Set markSet) {
        this.markSet = markSet;
    }

    /*
    public void setLabel(String aLabel) {
    	label = aLabel;
    }
    
    public String getLabel() {
    	return label;
    }*/
}


class MatchCode {

    private int first;
    private int second;

    public MatchCode() {
    }

    public MatchCode(Statement m1, Statement m2) {
        first = m1.getIndex();
        second = m2.getIndex();
    }

    @Override
	public int hashCode() {
        return first + 37 * second;
    }

    @Override
	public boolean equals(Object other) {
        MatchCode mc = (MatchCode) other;
        return mc.first == first && mc.second == second;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @Override
	public String toString() {
    	return ""+this.first +"-"+ this.second;
    }
}