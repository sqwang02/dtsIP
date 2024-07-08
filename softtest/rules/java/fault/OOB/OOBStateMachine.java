package softtest.rules.java.fault.OOB;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.DomainSet;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTClassOrInterfaceBody;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.OOBPreconditionListener;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.VexNode;
import softtest.domain.java.ArrayDomain;
import softtest.domain.java.ConvertDomain;
import softtest.domain.java.IntegerDomain;
import softtest.domain.java.IntegerInterval;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.jaxen.java.DocumentNavigator;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class OOBStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("数组越界:  %d 行上数组 \'%s\' 的下标可能超过其合法的长度导致一个数组越界异常", errorline,fsmmi.getResultString());
		}else{
			f.format("Out of Bound: the index of array \'%s\' at line %d can be greater than its size.", fsmmi.getResultString(),errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		//listeners.addListener(OOBPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static List getEvaluationResults(SimpleJavaNode node, String xPath) {
		List evaluationResults = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}

	private static boolean isIndexFixed(SimpleJavaNode array, String xPath) {
		boolean b = false;
		List list = getEvaluationResults(array, xPath);
		if (list.size() > 0) {
			ASTExpression indexexp = (ASTExpression) list.get(0);
			VexNode vex = indexexp.getCurrentVexNode();
			DomainSet old = null;
			if (vex != null) {
				old = vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
			}
			DomainData expdata = new DomainData();
			expdata.sideeffect = false;
			// 计算下标表达式
			indexexp.jjtAccept(new ExpressionDomainVisitor(), expdata);
			if (vex != null) {
				vex.setDomainSet(old);
			}
			IntegerDomain idomain = (IntegerDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
			if (idomain.isCanonical()) {
				b = true;
			}
		} else {
			b = true;
		}
		return b;
	}

	// 每个数组内存分配产生一个状态机
	public static List<FSMMachineInstance> createOOBStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
		List evaluationResults = null;

		// 数组变量在声明时，通过new 分配内存
		xPath = ".//VariableDeclaratorId[( @Array=\'true\' or ../../Type/ReferenceType[@Array=\'true\']) and ../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits]";
		evaluationResults = getEvaluationResults(node, xPath);

		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v = id.getNameDeclaration();

			if (!isIndexFixed(id, "../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits/Expression")) {
				continue;
			}

			if (!table.containsKey(v) && !id.hasLocalMethod(node)) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedVariable(v);
				table.put(v, fsminstance);
			}
		}

		// 不在声明处，数组变量通过new分配内存
		xPath = ".//PrimaryExpression/PrimaryPrefix/Name[not(../../PrimarySuffix) and ../../../AssignmentOperator[@Image=\'=\'] and ../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits]";
		evaluationResults = getEvaluationResults(node, xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();

			if (!isIndexFixed(name, "../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits/Expression")) {
				continue;
			}

			List<NameDeclaration> decllist = name.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (decl instanceof VariableNameDeclaration) {
					VariableNameDeclaration v = (VariableNameDeclaration) decl;
					if (!table.containsKey(v) && !name.hasLocalMethod(node)) {
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedVariable(v);
						table.put(v, fsminstance);
					}
					break;//只处理第一个变量 
				}
			}
		}

		// 在数组声明初静态分配内存
		xPath = ".//VariableDeclaratorId[../VariableInitializer/ArrayInitializer]";
		evaluationResults = getEvaluationResults(node, xPath);

		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v = id.getNameDeclaration();
			if (!table.containsKey(v) && !id.hasLocalMethod(node)) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedVariable(v);
				table.put(v, fsminstance);
			}
		}

		// 成员数组变量在声明时，通过new 分配内存
		xPath = ".//FieldDeclaration//VariableDeclaratorId[( @Array=\'true\' or ../../Type/ReferenceType[@Array=\'true\']) and ../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits]";
		ASTClassOrInterfaceBody classnode = (ASTClassOrInterfaceBody) node.getFirstParentOfType(ASTClassOrInterfaceBody.class);
		if (classnode != null) {
			evaluationResults = getEvaluationResults(classnode, xPath);

			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
				VariableNameDeclaration v = id.getNameDeclaration();

				if (!isIndexFixed(id, "../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits/Expression")) {
					continue;
				}

				if (!table.containsKey(v) && !id.hasLocalMethod(node)) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setRelatedVariable(v);
					table.put(v, fsminstance);
				}
			}
		}

		// 成员数组声明初静态分配内存
		xPath = ".//FieldDeclaration//VariableDeclaratorId[../VariableInitializer/ArrayInitializer]";
		if (classnode != null) {
			evaluationResults = getEvaluationResults(classnode, xPath);

			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
				VariableNameDeclaration v = id.getNameDeclaration();
				if (!table.containsKey(v) && !id.hasLocalMethod(node)) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setRelatedVariable(v);
					table.put(v, fsminstance);
				}
			}
		}

		for (Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		return list;
	}
	
	public static boolean checkAllocated(VexNode vex, FSMMachineInstance fsmin) {
		VariableNameDeclaration v=fsmin.getRelatedVariable();
		if(v!=null){		
			ArrayDomain a=(ArrayDomain)ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v),ClassType.ARRAY);

			for (IntegerDomain i : a.getAllDimensions()) {
				if (!i.getUnknown()) {
					return true;
				}
			}
	
		}
		return false;
	}

	public static boolean checkSameVariable(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof ASTName) {
				ASTName name = (ASTName) o;
				List<NameDeclaration> decllist = name.getNameDeclarationList();
				Iterator<NameDeclaration> decliter = decllist.iterator();
				while (decliter.hasNext()) {
					NameDeclaration decl = decliter.next();
					if (decl instanceof VariableNameDeclaration) {
						VariableNameDeclaration v = (VariableNameDeclaration) decl;
						if (v == fsmin.getRelatedVariable()) {
							return true;
						}
						break;//只处理第一个变量 
					}
				}
			} else if (o instanceof ASTVariableDeclaratorId) {
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) o;
				VariableNameDeclaration v = id.getNameDeclaration();
				if (v == fsmin.getRelatedVariable()) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean checkSameVariableAndOOB(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			List<NameDeclaration> decllist = name.getNameDeclarationList();
			Iterator<NameDeclaration> decliter = decllist.iterator();
			while (decliter.hasNext()) {
				NameDeclaration decl = decliter.next();
				if (decl instanceof VariableNameDeclaration) {
					VariableNameDeclaration v = (VariableNameDeclaration) decl;
					if (v == fsmin.getRelatedVariable()) {
						// 检察是否越界
						Object domain = name.findCurrentDomain(v);
						if (domain instanceof ArrayDomain) {
							ArrayDomain a = (ArrayDomain) domain;
							ASTPrimaryExpression priexpr = (ASTPrimaryExpression) name.jjtGetParent().jjtGetParent();
							List suffixlist = priexpr.findDirectChildOfType(ASTPrimarySuffix.class);
							Iterator siter = suffixlist.iterator();
							int count = 0;
							while (siter.hasNext()) {
								ASTPrimarySuffix suffix = (ASTPrimarySuffix) siter.next();
								if (!suffix.isArrayDereference()) {
									continue;
								}
								VexNode vex = name.getCurrentVexNode();
								DomainSet old = vex.getDomainSet();
								vex.setDomainSet(vex.getLastDomainSet());
								DomainData expdata = new DomainData();
								expdata.sideeffect = false;
								// 计算下标表达式
								SimpleJavaNode indexexp = (SimpleJavaNode) suffix.jjtGetChild(0);
								indexexp.jjtAccept(new ExpressionDomainVisitor(), expdata);
								vex.setDomainSet(old);

								IntegerDomain idomain = (IntegerDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
								if(idomain.getUnknown()){
									continue;
								}
								IntegerInterval interval = idomain.jointoOneInterval();
								if (!interval.isEmpty()) {
									// 去除了那些缺省范围下标的情况
									if (!a.getDimension(count).getUnknown()){
										IntegerInterval ti=a.getDimension(count).jointoOneInterval();
										if((ti.getMax() <= interval.getMax()||(softtest.config.java.Config.OOB_NEG&&interval.getMin()<0))) {
											return true;
										}
									}
								}
								count++;
							}
						}
					}
					break;//只处理第一个变量 
				}
			}
		}
		return false;
	}
	
	public static List<FSMMachineInstance> createConOOBStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
		List evaluationResults = null;
		
		
		//长度检查
		xPath = ".//Expression[(parent::IfStatement or parent::WhileStatement or parent::DoStatement or parent::ForStatement)]//RelationalExpression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'^.+\\.length$')]";
		evaluationResults = getEvaluationResults(node, xPath);

		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!(v.getDomain() instanceof ArrayDomain)) {
				continue;
			}
			
			ASTPrimaryExpression pri =(ASTPrimaryExpression)name.jjtGetParent().jjtGetParent();
			ASTRelationalExpression relation=(ASTRelationalExpression)pri.jjtGetParent();
			if(relation.jjtGetNumChildren()!=2){
				continue;
			}
			SimpleJavaNode temp=null;
			if(relation.jjtGetChild(0)==pri&&relation.getImage().equals(">=")){
				temp=(SimpleJavaNode)relation.jjtGetChild(1);
			}else if(relation.jjtGetChild(1)==pri&&relation.getImage().equals("<=")){
				temp=(SimpleJavaNode)relation.jjtGetChild(0);
			}else {
				continue;
			}
			name=(ASTName)temp.getSingleChildofType(ASTName.class);
			if(name==null){
				continue;
			}
			
//			SimpleJavaNode parent=(SimpleJavaNode)relation.jjtGetParent().jjtGetParent();
//			if(parent instanceof ASTIfStatement){
//				parent=(SimpleJavaNode)parent.jjtGetChild(1);
//			}
			SimpleJavaNode parent=(SimpleJavaNode)relation.jjtGetParent();
			while(null != parent && !(parent instanceof ASTIfStatement || parent instanceof ASTWhileStatement || parent instanceof ASTDoStatement || parent instanceof ASTForStatement)){
				parent  = (SimpleJavaNode) parent.jjtGetParent();
			}
			xPath=".//PrimaryExpression[(./PrimaryPrefix/Name[@Image=\'";
			xPath=xPath+v.getImage()+"\']) and (./PrimarySuffix[@ArrayDereference=\'true\']/Expression/PrimaryExpression/PrimaryPrefix/Name[@Image=\'";
			VariableNameDeclaration v1 = (VariableNameDeclaration) name.getNameDeclaration();
			xPath=xPath+v1.getImage()+"\'])]";
			List tlist=getEvaluationResults(parent,xPath);
			if(!tlist.isEmpty()){
				ASTPrimaryExpression p=(ASTPrimaryExpression)tlist.get(0);
				if(!p.hasLocalMethod(node)) {
					FSMMachineInstance fsminstance = fsm.creatInstance();				
					fsminstance.setRelatedObject(new FSMRelatedCalculation(p));
					fsminstance.setResultString(v.getImage());
					list.add(fsminstance);
				}
			}
		}
		return list;
	}
	
}
