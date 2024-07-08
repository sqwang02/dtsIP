/**
 * 
 */
package softtest.rules.java.safety.TD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.IntervalAnalysis.java.DomainSet;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTUnaryExpressionNotPlusMinus;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.Edge;
import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author xiaoqin
 *
 */
/** ����Ⱦ�ı������� */
public class TaintedSet extends FSMRelatedCalculation {
	
	private SensitiveUseChecker useChecker;
	
	
	public TaintedSet() {
		
		/*
		 * 1 ����ʹ�ü���߹���
		 */
		SensitiveUseCheckerFactory factory = new SensitiveUseCheckerFactory();
		
		/*
		 * 2 ��������ʹ�ü����
		 */
		useChecker = factory.createSensitiveUseChecker();
		
		/*
		 * 3 ����˫���ϵ
		 */
		useChecker.setTaintedSet(this);
	}

	public TaintedSet(FSMRelatedCalculation o) {
		
		super(o);
		
		if (!(o instanceof TaintedSet)) {
			return;
		}
		TaintedSet t = (TaintedSet) o;
		for (Enumeration<VariableNameDeclaration> e = t.table.elements(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			table.put(v, v);
		}
		this.useChecker = t.getUseChecker();
	}

	/** ���� */
	public FSMRelatedCalculation copy() {
		FSMRelatedCalculation r = new TaintedSet(this);
		return r;
	}

	/** ����Ⱦ������ */
	private Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();

	/** ��ӱ���Ⱦ������ֻ����������ֵ�ͺ�String���ͱ��� */
	public void add(VariableNameDeclaration v) {
		
		switch (DomainSet.getDomainType(v.getDomain())) {
		case INT:
			table.put(v, v);
			break;
		case REF:
			table.put(v, v);
			break;
		case ARRAY:
			table.put(v, v);
			break;
		}
	}

	/** �Ӽ������Ƴ����� */
	public void remove(VariableNameDeclaration v) {
		table.remove(v);
	}
	
	public void removeAll() {
		table.clear();
	}

	public boolean isEmpty() {
		return table.isEmpty();
	}

	public boolean contains(VariableNameDeclaration v) {
		return table.containsKey(v);
	}

	public Hashtable<VariableNameDeclaration, VariableNameDeclaration> getTable() {
		return table;
	}

	public void setTable(Hashtable<VariableNameDeclaration, VariableNameDeclaration> table) {
		this.table = table;
	}

	/** ���������������е�IN */
	public void calculateIN(FSMMachineInstance fsmin, VexNode n, Object data) {
		if (fsmin.getRelatedObject() != this) {
			throw new RuntimeException("TaintedSet error");
		}
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);

		boolean bfirst = true;
		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			VexNode pre = edge.getTailNode();

			if (pre.getFSMMachineInstanceSet() != null) {
				FSMMachineInstance prefsmin = pre.getFSMMachineInstanceSet().getTable().get(fsmin);
				if (prefsmin != null) {
					if (bfirst) {
						bfirst = false;
						Hashtable<VariableNameDeclaration, VariableNameDeclaration> table1, newtable;
						TaintedSet s1 = (TaintedSet) prefsmin.getRelatedObject();
						table1 = s1.getTable();
						newtable = new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();

						for (Enumeration<VariableNameDeclaration> e = table1.elements(); e.hasMoreElements();) {
							VariableNameDeclaration v = e.nextElement();
							newtable.put(v, v);
						}
						setTable(newtable);
					} else {
						Hashtable<VariableNameDeclaration, VariableNameDeclaration> table1, table2, newtable;
						TaintedSet s1 = (TaintedSet) fsmin.getRelatedObject();
						TaintedSet s2 = (TaintedSet) prefsmin.getRelatedObject();
						table1 = s1.getTable();
						table2 = s2.getTable();
						newtable = new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();
						// �󲢼�
						for (Enumeration<VariableNameDeclaration> e = table1.elements(); e.hasMoreElements();) {
							VariableNameDeclaration v = e.nextElement();
							newtable.put(v, v);
						}
						for (Enumeration<VariableNameDeclaration> e = table2.elements(); e.hasMoreElements();) {
							VariableNameDeclaration v = e.nextElement();
							newtable.put(v, v);
						}
						setTable(newtable);
					}
				}
			}
		}
	}

	/** �жϱ���Ⱦ�ı����Ƿ�����ڽڵ�node�� */
	private boolean hasTainedOccurenceIn(SimpleNode node) {
		List list = findTreeNodes(node, ".//Name");
		Iterator i = list.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			
			//add by limppa
			/*
			 * ������������� 
			 * 1 String [] dst = src.split(...);
			 * 2 String dst = sb.toString();
			 * 3 String dst = src.g().subString(...);
			 */
//			if(name.getType() instanceof Method)
//			{
//				//Scope scope = name.getScope();
//				String [] nms = name.getImage().split("\\.");
//				String nm = nms[0];
//				
//				Set varDels = table.keySet();
//				Iterator iter = varDels.iterator();
//				
//				while(iter.hasNext())
//				{
//					VariableNameDeclaration v = (VariableNameDeclaration)iter.next();
//					if(v.getImage().equals(nm))
//					{
//						return true;
//					}
//				}
//			}
			//add end
			
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			
			if (contains(v)) {
				return true;
			}
		}
		return false;
	}
	
	/** ���ʹ�� */
	//vexNode n �ǿ�����ͼ��һ�����ڵ㣬FSMMachineInstance fsmin�Զ���ʵ��
	public boolean checkUsed(VexNode n,FSMMachineInstance fsmin) {
		String xpath = "";
		StringBuffer buffer = null;
		List list = null;
		Iterator i = null;
		if (n.isBackNode()) {
			return false;
		}
		
		Iterator it = useChecker.getSensitiveCheckers().iterator();
		while(it.hasNext())
		{
			ISensitiveChecker checker = (ISensitiveChecker) it.next();
			if(checker.checkUsed(n, fsmin))
				return true;
		}
		return false;
	}

	/** �����飬�Ӽ������Ƴ���Щ�����ı��� */
	public void check(VexNode n) {
		String xpath = "";
		StringBuffer buffer = null;
		List list = null;
		Iterator i = null;
		for (Enumeration<VariableNameDeclaration> e = table.elements(); e.hasMoreElements();) {
			
			VariableNameDeclaration v = e.nextElement();
			boolean bchecked = false;//��������Ƿ�check����
			switch (DomainSet.getDomainType(v.getDomain())) {
			case INT:
				if (n.getInedges().size() == 1) {
					for (Edge edge : n.getInedges().values()) {
						VexNode pre = edge.getTailNode();
						if (edge.getName().startsWith("T") || edge.getName().startsWith("F")) {
							//pre������Ϊbacknode
							SimpleNode treenode = pre.getTreeNode().getConcreteNode();
							if (treenode != null) {
								/**
								 * ���磺 if(v<10){}
								 */
								xpath = ".//RelationalExpression//PrimaryExpression/PrimaryPrefix/Name";
								list = findTreeNodes(treenode, xpath);
								i = list.iterator();
								while (i.hasNext()) {
									ASTName name = (ASTName) i.next();
									if (v == name.getNameDeclaration()) {
										bchecked = true;
										break;
									}
								}
							}
						}
					}
				}
				break;
			case REF:
				if (n.getInedges().size() == 1) {
					for (Edge edge : n.getInedges().values()) {
						VexNode pre = edge.getTailNode();
						SimpleNode treenode = pre.getTreeNode().getConcreteNode();
						boolean btruecheck = false, bfalsecheck = false;
						if ((treenode != null) && (edge.getName().startsWith("T") || edge.getName().startsWith("F")))
						{	
							//������addStringTrueCheck����ķ�֧
							buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix/Arguments]/PrimaryPrefix/Name[matches(@Image,\'^(");
							xpath = addStringTrueCheck(buffer);
							xpath += ")$\')]";
							list = findTreeNodes(treenode, xpath);
							i = list.iterator();
							while (i.hasNext()) {
								ASTName name = (ASTName) i.next();
								if (v == name.getNameDeclaration()) {
									ASTUnaryExpressionNotPlusMinus not = (ASTUnaryExpressionNotPlusMinus) name.getFirstParentOfType(
											ASTUnaryExpressionNotPlusMinus.class, treenode);
									if (not != null && not.getImage().equals("!")) {
										bfalsecheck = true;
									} else {
										btruecheck = true;
									}
								}
							}

							//������addStringFalseCheck,�ٵķ�֧
							buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix/Arguments]/PrimaryPrefix/Name[matches(@Image,\'^(");
							xpath = addStringFalseCheck(buffer);
							xpath += ")$\')]";
							list = findTreeNodes(treenode, xpath);
							i = list.iterator();
							while (i.hasNext()) {
								ASTName name = (ASTName) i.next();
								ASTUnaryExpressionNotPlusMinus not = (ASTUnaryExpressionNotPlusMinus) name.getFirstParentOfType(
										ASTUnaryExpressionNotPlusMinus.class, treenode);
								if (not != null && not.getImage().equals("!")) {
									btruecheck = true;
								} else {
									bfalsecheck = true;
								}
							}
							if (edge.getName().startsWith("T") && btruecheck) {
								bchecked = true;
							}
							if (edge.getName().startsWith("F") && bfalsecheck) {
								bchecked = true;

							}
						}		
					}
				}
				//
				SimpleNode treenode = n.getTreeNode().getConcreteNode();
				if (!n.isBackNode()&&treenode != null) {
					buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix/Arguments]/PrimaryPrefix/Name[matches(@Image,\'^(");
					xpath = addStringCheck(buffer);
					xpath += ")$\')]";
					list = findTreeNodes(treenode, xpath);
					i = list.iterator();
					while (i.hasNext()) {
						ASTName name = (ASTName) i.next();
						if (v == name.getNameDeclaration()) {
							bchecked = true;
						}
					}
				}
				break;
			}
			if (bchecked) {
				removeAll();
			}
		}
	}

	private static String[] STRING_TRUE_CHECK = { "equals", "endsWith", "matches", "startsWith", "regionMatches", };

	private static String addStringTrueCheck(StringBuffer buffer) {
		for (String s : STRING_TRUE_CHECK) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_TRUE_CHECK.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static String[] STRING_FALSE_CHECK = { "contains", "matches", "regionMatches" };

	private static String addStringFalseCheck(StringBuffer buffer) {
		for (String s : STRING_FALSE_CHECK) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_FALSE_CHECK.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
	
	private static String[] STRING_CHECK = { /**"replace", "replaceAll",**/ };

	private static String addStringCheck(StringBuffer buffer) {
		for (String s : STRING_CHECK) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_CHECK.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	/** ���������������е�OUT */
	public void calculateOUT(FSMMachineInstance fsmin, VexNode n, Object data) {
		if (fsmin.getRelatedObject() != this) {
			throw new RuntimeException("TaintedSet error");
		}
		List evaluationResults = new LinkedList();
		SimpleNode treenode = n.getTreeNode();

		// xpath��������Щβ�ڵ�
		if (!n.isBackNode()) {
			treenode = treenode.getConcreteNode();
			// ����ֵ
			if (treenode != null) {
				evaluationResults = findTreeNodes(treenode, ".//AssignmentOperator[@Image=\'=\']");
			}
			Iterator i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTAssignmentOperator assign = (ASTAssignmentOperator) i.next();
				SimpleJavaNode parent = (SimpleJavaNode) assign.jjtGetParent();//��ø��ڵ�
				if (parent.jjtGetNumChildren() != 3) {
					continue;
				}
				SimpleJavaNode left = (SimpleJavaNode) parent.jjtGetChild(0);//��ø�ֵ������ߵĽڵ�
				SimpleJavaNode right = (SimpleJavaNode) parent.jjtGetChild(2);//��ø�ֵ�����ұߵĽڵ�
				ASTName leftname = (ASTName) left.getSingleChildofType(ASTName.class);//�����߽ڵ��name

				VariableNameDeclaration leftv = null;
				if (leftname == null || !(leftname.getNameDeclaration() instanceof VariableNameDeclaration)) {
					continue;
				}
				leftv = (VariableNameDeclaration) leftname.getNameDeclaration();
				if (hasTainedOccurenceIn(right)) {
					add(leftv);
				} else {
					remove(leftv);
				}
			}

			// �����ʼ����ֵ
			if (treenode != null) {
				evaluationResults = findTreeNodes(treenode, ".//VariableDeclarator/VariableInitializer");
			}
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableInitializer initializer = (ASTVariableInitializer) i.next();
				SimpleJavaNode parent = (SimpleJavaNode) initializer.jjtGetParent();

				ASTVariableDeclaratorId left = (ASTVariableDeclaratorId) parent.jjtGetChild(0);
				SimpleJavaNode right = initializer;

				VariableNameDeclaration leftv = null;
				if (left == null || !(left.getNameDeclaration() instanceof VariableNameDeclaration)) {
					continue;
				}
				leftv = (VariableNameDeclaration) left.getNameDeclaration();
				if (hasTainedOccurenceIn(right)) {
					add(leftv);
				} else {
					remove(leftv);
				}
			}
		}
		// ����������

		ArrayList<VariableNameDeclaration> todelete = new ArrayList<VariableNameDeclaration>();
		Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = getTable();
		for (Enumeration<VariableNameDeclaration> e = table.keys(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			Scope delscope = v.getDeclareScope();
			SimpleJavaNode astnode = n.getTreeNode();

			boolean b = false;
			if (!astnode.getScope().isSelfOrAncestor(delscope)) {
				// �����������Ѿ����ǵ�ǰ�������Լ�������
				b = true;
			} else if (delscope.isSelfOrAncestor(astnode.getScope()) && n.isBackNode()) {
				// ��ǰ�������������������Լ����߸��ף����ǵ�ǰ�ڵ���Ҫ��ֹ��ǰ������
				b = true;
			} else {
				b = false;
			}
			if (b) {
				todelete.add(v);
			}
		}

		for (VariableNameDeclaration v : todelete) {
			remove(v);
		}

		// ������
		check(n);
	}

	private static List findTreeNodes(SimpleNode node, String xPath) {
		List evaluationResults = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}

	public SensitiveUseChecker getUseChecker()
	{
		return useChecker;
	}

	public void setUseCheckers(SensitiveUseChecker useChecker)
	{
		this.useChecker = useChecker;
	}
}
