package softtest.symboltable.java;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import softtest.util.java.Applier;

public class SourceFileScope extends AbstractScope implements Scope {

	protected Map classNames = new HashMap();

	private String packageImage;

	public SourceFileScope() {
		this("");
	}

	public SourceFileScope(String image) {
		this.packageImage = image;
	}

	@Override
	public ClassScope getEnclosingClassScope() {
		//added by xqing
		return null;
		//throw new RuntimeException("getEnclosingClassScope() called on SourceFileScope");
	}

	@Override
	public MethodScope getEnclosingMethodScope() {
		// added by xqing
		return null;
		//throw new RuntimeException("getEnclosingMethodScope() called on SourceFileScope");
	}

	public String getPackageName() {
		return packageImage;
	}

	@Override
	public SourceFileScope getEnclosingSourceFileScope() {
		return this;
	}

	@Override
	public void addDeclaration(ClassNameDeclaration classDecl) {
		classNames.put(classDecl, new ArrayList());
	}

	@Override
	public void addDeclaration(MethodNameDeclaration decl) {
		throw new RuntimeException("SourceFileScope.addDeclaration(MethodNameDeclaration decl) called");
	}

	public void addDeclaration(VariableNameDeclaration decl) {
		throw new RuntimeException("SourceFileScope.addDeclaration(VariableNameDeclaration decl) called");
	}

	@Override
	public Map getClassDeclarations() {
		return classNames;
	}


	public NameDeclaration addVariableNameOccurrence(NameOccurrence occ) {
		return null;
	}

	@Override
	public String toString() {
		return "SourceFileScope: " + glomNames(classNames.keySet().iterator());
	}

	@Override
	protected NameDeclaration findVariableHere(NameOccurrence occ) {
		ImageFinderFunction finder = new ImageFinderFunction(occ.getImage());
		Applier.apply(finder, classNames.keySet().iterator());
		return finder.getDecl();
	}

	//added by xqing
	@Override
	public String dump() {
		StringBuffer b = new StringBuffer();
		b.append("SourceFileScope: ");
		if (!classNames.isEmpty()) {
			b.append("(classes: ");
			Iterator i = classNames.keySet().iterator();
			while (i.hasNext()) {
				ClassNameDeclaration mnd = (ClassNameDeclaration) i.next();
				b.append(mnd.getImage().toString());
				if (i.hasNext()) {
					b.append(",");
				}
			}
			b.append(")");
		}
		return b.toString();
	}
}
