package softtest.ccd.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import softtest.config.java.Config;

public class StmtMatchCollector2 {

	private StmtsSliceMatcher4 sm;
	private Map startMap = new HashMap();
	//private Map fileMap = new HashMap();
	private List pairMatches = null;
	
	public void  clear() {
		startMap.clear();
		if( pairMatches != null ) {
			pairMatches.clear();
		}
	}

	public void collect(List marks) {
		logc("marks.size:" + marks.size());
		//first get a pairwise collection of all maximal matches
		for (int i = 0; i < marks.size() - 1; i++) {
			Statement mark1 = (Statement) marks.get(i);
			for (int j = i + 1; j < marks.size(); j++) {
				Statement mark2 = (Statement) marks.get(j);
				int diff = mark1.getIndex() - mark2.getIndex();
				if (-diff < StmtsSliceMatcher4.minTiles) {
					continue;
				}
				// 如果mark1,mark2的前面的语句也是相同的，则mark1,mark2就不需要再处理
				if (hasPreviousDupe(mark1, mark2)) {
					continue;
				}

				// "match too small" check   以mark1,mark2开始相同的token的长度
				int dupes = countDuplicateStmts(mark1, mark2);
				if (dupes < StmtsSliceMatcher4.minTiles) {
					continue;
				}
				// is it still too close together
				if (diff + dupes >= 1) {
					continue;
				}
				determineMatch(mark1, mark2, dupes);
			}
		}
	}

	public List getMatches() {
		List matchList = new ArrayList(startMap.values());
		Collections.sort(matchList);

		/*
		//log("--------- Sorted matchlist begin ---------");
		for (int i = 0; i < matchList.size(); i++) {
			//log("" + matchList.get(i));
		}
		//log("--------- Sorted end ---------");
		*/
		Set matchSet = new HashSet();
		MatchCode matchCode = new MatchCode();
		for (int i = matchList.size(); i > 1; i--) {
			OneMatch match1 = (OneMatch) matchList.get(i - 1);
			Statement mark1 = (Statement) match1.getMarkSet().iterator().next();
			// log("mark1:" + mark1.getIndex());
			matchSet.clear();
			matchSet.add(match1.getMatchCode());
			// A <-> B, B <-> C, A <-> C, then, remove the B <-> C
			// AE BE CE DE
			for (int j = i - 1; j > 0; j--) {
				OneMatch match2 = (OneMatch) matchList.get(j - 1);
				if (match1.getStmtCount() != match2.getStmtCount()) {
					break;
				}
				Statement mark2 = null;
				for (Iterator iter = match2.getMarkSet().iterator(); iter.hasNext();) {
					mark2 = (Statement) iter.next();
					if (mark2 != mark1) {
						break;
					}
				}
				int dupes = countDuplicateStmts(mark1, mark2);
				if (dupes < match1.getStmtCount()) {
					break;
				}
				matchSet.add(match2.getMatchCode());
				match1.getMarkSet().addAll(match2.getMarkSet());
				// loc("remove:[" + (i - 2) + "] "	+ matchList.get(i - 2));///////////
				matchList.remove(i - 2);
				i--;
			}
			if (matchSet.size() == 1) {
				continue;
			}

			//prune the mark set
			Set pruned = match1.getMarkSet();
			boolean done = false;
			ArrayList a1 = new ArrayList(match1.getMarkSet());
			Collections.sort(a1);
			for (int outer = 0; outer < a1.size() - 1 && !done; outer++) {
				Statement cmark1 = (Statement) a1.get(outer);
				//log("  " + cmark1.getIndex());//////////////////
				for (int inner = outer + 1; inner < a1.size() && !done; inner++) {
					Statement cmark2 = (Statement) a1.get(inner);
					matchCode.setFirst(cmark1.getIndex());
					matchCode.setSecond(cmark2.getIndex());
					if (!matchSet.contains(matchCode)) {
						if (pruned.size() > 2) {
							pruned.remove(cmark2);
						}
						if (pruned.size() == 2) {
							done = true;
						}
					}
				}
			}
		}
		return matchList;
	}

	/**
	 * A greedy algorithm for determining non-overlapping matches
	 */
	private void determineMatch(Statement mark1,	Statement mark2, int dupes) {
		OneMatch match = new OneMatch(dupes, mark1, mark2);

		if (pairMatches == null) {
			pairMatches = new ArrayList();
		}
		boolean add = true;
		for (int i = 0; i < pairMatches.size(); i++) {
			OneMatch other = (OneMatch) pairMatches.get(i);
			if (other.getFirstMark().getIndex() + other.getStmtCount()	- mark1.getIndex() > 0) {
				boolean ordered = other.getSecondMark().getIndex()	- mark2.getIndex() < 0;
				if ((ordered && (other.getEndIndex() - mark2.getIndex() > 0))
						|| (!ordered && (match.getEndIndex() - other.getSecondMark().getIndex()) > 0)) {
					if (other.getStmtCount() >= match.getStmtCount()) {
						add = false;
						//log("add = false");
						break;
					} else {
						pairMatches.remove(i);
						startMap.remove(other.getMatchCode());
						System.out.println("repl:("
								+ other.getFirstMark().getIndex() + " len:"
								+ other.getStmtCount() + " => ("
								+ match.getFirstMark().getIndex() + " len:"
								+ match.getStmtCount());
					}
				}
			}
		}
		if (add) {
			//log("add " + match);
			pairMatches.add(match);
			startMap.put(match.getMatchCode(), match);
		}
	}

	private boolean hasPreviousDupe(Statement mark1,	Statement mark2) {
		if (mark1.getIndex() == 0) {
			return false;
		}
		return !matchEnded(StmtsSliceMatcher4.stmtAt(-1, mark1), StmtsSliceMatcher4.stmtAt(-1, mark2));
	}

	private int countDuplicateStmts(Statement mark1, Statement mark2) {
		int index = 0;
		while (StmtsSliceMatcher4.statements.size() > mark1.getIndex()+index
			&& StmtsSliceMatcher4.statements.size() > mark2.getIndex()+index
			&& !matchEnded(StmtsSliceMatcher4.stmtAt(index, mark1), StmtsSliceMatcher4.stmtAt(index, mark2))) {
			index++;
		}
		return index;
	}

	private boolean matchEnded(Statement token1, Statement token2) {
		return token1.getHashCode() != token2.getHashCode();
		//|| token1 == TokenEntry.EOF || token2 == TokenEntry.EOF;
	}
	
	public static void logc(String str) {
		log("StmtsMatchCollector2::" + str);
	}
	
	public static void log(String str) {
		if( Config.DEBUG ) {
			System.out.println(str);
		}
	}
}