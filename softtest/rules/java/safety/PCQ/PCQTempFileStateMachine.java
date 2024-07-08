package softtest.rules.java.safety.PCQ;

import java.util.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.rules.java.AliasSet;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;
import softtest.fsm.java.*;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;



/** 低质量代码状态机动作类 Poor Code Quality ，使用完临时文件应尽快删除*/
public class PCQTempFileStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("低质量代码模式: %d 行创建的临时文件没有被尽快删除。临时文件可能会泄漏系统执行过程的信息或敏感信息，容易被攻击者轻易获得。", beginline);
		}else{
			f.format("Poor Code Quality: tempfile ceated on line %d should be deleted as soon as possible",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	public static List<FSMMachineInstance> createPCQStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		// 在句柄变量声明时，分配资源
		String xPath = ".//VariableDeclaratorId[../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.createTempFile)|(createTempFile))$\')]]";
		List evaluationResults = null;

		evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			AliasSet alias = new AliasSet();
			// 不添加别名集合，在created处添加
			// VariableNameDeclaration v = (VariableNameDeclaration)
			// id.getNameDeclaration();
			// alias.add(v);
			fsminstance.setResultString(id.getImage());
			alias.setResouceName("File");
			alias.setResource(id);

			fsminstance.setRelatedObject(alias);
			if (!id.hasLocalMethod(node)) {
				list.add(fsminstance);
			}
		}

		// 不在在句柄变量声明时，分配资源，表达式语句
		xPath="..//StatementExpression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.createTempFile)|(createTempFile))$\')]]/PrimaryExpression/PrimaryPrefix/Name";

		evaluationResults = node.findXpath(xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			AliasSet alias = new AliasSet();
			// / 不添加别名集合，在allocated处添加
			// VariableNameDeclaration v = (VariableNameDeclaration)
			// name.getNameDeclaration();
			// alias.add(v);
			fsminstance.setResultString(name.getImage());
			alias.setResouceName("File");
			alias.setResource(name);

			fsminstance.setRelatedObject(alias);
			if (!name.hasLocalMethod(node)) {
				list.add(fsminstance);
			}
		}
		// 表达式

		xPath = ".//Expression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.createTempFile)|(createTempFile))$\')]]/PrimaryExpression/PrimaryPrefix/Name";

		evaluationResults = node.findXpath(xPath);
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();

			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			AliasSet alias = new AliasSet();
			// / 不添加别名集合，在allocated处添加
			// VariableNameDeclaration v = (VariableNameDeclaration)
			// name.getNameDeclaration();
			// alias.add(v);
			fsminstance.setResultString(name.getImage());
			alias.setResouceName("File");
			alias.setResource(name);

			fsminstance.setRelatedObject(alias);
			if (!name.hasLocalMethod(node)) {
				list.add(fsminstance);
			}
		}

		return list;
	}

	public static boolean checkSameFile(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			AliasSet alias = (AliasSet) fsmin.getRelatedObject();
			if (alias.getResource() == o) {
				// 添加别名集合
				if (o instanceof ASTVariableDeclaratorId) {
					ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) o;
					VariableNameDeclaration v = id.getNameDeclaration();
					alias.add(v);
				} else if (o instanceof ASTName) {
					ASTName name = (ASTName) o;
					VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
					alias.add(v);
				}
				return true;
			}
		}
		return false;
	}

	public static boolean checkSameFileDelete(List nodes, FSMMachineInstance fsmin) {
		Iterator i = nodes.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			AliasSet alias = (AliasSet) fsmin.getRelatedObject();

			// 检察别名和释放函数
			ASTName name = (ASTName) o;
			List<NameDeclaration> decllist = name.getNameDeclarationList();
			VariableNameDeclaration v = null;
			String image = null;
			if (decllist.size() < 2 && decllist.size() > 0 && decllist.get(0) instanceof VariableNameDeclaration) {
				v = (VariableNameDeclaration) decllist.get(0);
				String[] a = name.getImage().split("\\.");
				image = a[a.length - 1];
			} else if (decllist.size() >= 2) {
				if ((decllist.get(decllist.size() - 1) instanceof MethodNameDeclaration)
						&& (decllist.get(decllist.size() - 2) instanceof VariableNameDeclaration)) {
					v = (VariableNameDeclaration) decllist.get(decllist.size() - 2);
					MethodNameDeclaration m = (MethodNameDeclaration) decllist.get(decllist.size() - 1);
					image = m.getImage();
				}
			}

			if (v == null || !alias.contains(v)) {
				continue;
			}
			if (image.equals("delete")) {
				return true;
			} else if (image.equals("deleteOnExit")) {
				ASTClassOrInterfaceDeclaration classdecl=(ASTClassOrInterfaceDeclaration)name.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
				if(classdecl!=null){
					List templist=classdecl.findXpath("./ExtendsList/ClassOrInterfaceType[1]");
					Iterator tempi = templist.iterator();
					while (tempi.hasNext()) {
						ASTClassOrInterfaceType parentclass = (ASTClassOrInterfaceType) tempi.next();
						image=parentclass.getImage();
						if(image==null||!(image.toLowerCase().contains("applet")||image.toLowerCase().contains("servlet"))){
							continue;
						}
						return false;
					}
				}
				return true;
			} 
		}
		return false;
	}

	public static boolean checkAliasNotEmpty(VexNode vex, FSMMachineInstance fsmin) {
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		if (!alias.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean checkAliasEmpty(VexNode vex, FSMMachineInstance fsmin) {
		return !checkAliasNotEmpty(vex, fsmin);
	}
	
	public static boolean checkAliasMemberIsNullPointer(VexNode vex, FSMMachineInstance fsmin) {
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		return alias.isAllMemberNullPointer(vex);
	}
	
}
