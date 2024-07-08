/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;
//added by xqing
import softtest.util.java.Applier;

public class ClassScope extends AbstractScope {

	protected Map classNames = new HashMap();

	protected Map methodNames = new HashMap();

	protected Map variableNames = new HashMap();

	// FIXME - this breaks given sufficiently nested code
	private int anonymousInnerClassCounter = 1;

	private String className;
	
	private boolean isanonymous=false;

	public ClassScope(String className) {
		this.className = className;
	}

	/**
	 * This is only for anonymous inner classes
	 * <p/>
	 * FIXME - should have name like Foo$1, not Anonymous$1
	 * to get this working right, the parent scope needs
	 * to be passed in when instantiating a ClassScope
	 */
	public ClassScope() {
		//this.className = getParent().getEnclosingClassScope().getClassName() + "$" + String.valueOf(anonymousInnerClassCounter);
		isanonymous=true;
		this.className = "Anonymous$" + anonymousInnerClassCounter;
	}
	
	@Override
	public void setParent(Scope s) {
		super.setParent(s);
		if (this.isanonymous) {
			this.className = String.valueOf((s.getEnclosingClassScope()).getAnonymousCounter());
			(s.getEnclosingClassScope()).incAnonymousCounter();
		}
	}
	
	public boolean isAnonymousInnerClass(){
		return isanonymous;
	}

	public void addDeclaration(VariableNameDeclaration variableDecl) {
		if (variableNames.containsKey(variableDecl)) {
			throw new RuntimeException(variableDecl + " is already in the symbol table");
		}
		variableNames.put(variableDecl, new ArrayList());
	}

	public NameDeclaration addVariableNameOccurrence(NameOccurrence occurrence) {
		NameDeclaration decl = findVariableHere(occurrence);
		if (decl != null && occurrence.isMethodOrConstructorInvocation()) {
			List nameOccurrences = (List) methodNames.get(decl);
			if (nameOccurrences == null) {
				// TODO may be a class name: Foo.this.super();
			} else {
				//add by xqing
				nameOccurrences.add(occurrence);
				occurrence.setDeclaration(decl);
				SimpleNode n = occurrence.getLocation();
				if (n instanceof ASTName) {
					((ASTName) n).setNameDeclaration(decl);
				} // TODO what to do with PrimarySuffix case?
				//added by xqing
				else if (n instanceof ASTPrimarySuffix) {
					//this.i
					((ASTPrimarySuffix) n).setNameDeclaration(decl);
				}
			}

		} else if (decl != null && !occurrence.isThisOrSuper()) {
			List nameOccurrences = (List) variableNames.get(decl);
			if (nameOccurrences == null) {
				// TODO may be a class name
			} else {
				nameOccurrences.add(occurrence);
				occurrence.setDeclaration(decl);
				SimpleNode n = occurrence.getLocation();
				if (n instanceof ASTName) {
					((ASTName) n).setNameDeclaration(decl);
				} // TODO what to do with PrimarySuffix case?
				// added by xqing
				else if (n instanceof ASTPrimarySuffix) {
					((ASTPrimarySuffix) n).setNameDeclaration(decl);
				}
			}
		}
		return decl;
	}

	@Override
	public Map getVariableDeclarations() {
		VariableUsageFinderFunction f = new VariableUsageFinderFunction(variableNames);
		Applier.apply(f, variableNames.keySet().iterator());
		return f.getUsed();
	}

	@Override
	public Map getMethodDeclarations() {
		return methodNames;
	}

	@Override
	public Map getClassDeclarations() {
		return classNames;
	}

	@Override
	public ClassScope getEnclosingClassScope() {
		return this;
	}

	public String getClassName() {
		return this.className;
	}

	@Override
	public void addDeclaration(MethodNameDeclaration decl) {
		methodNames.put(decl, new ArrayList());
	}

	@Override
	public void addDeclaration(ClassNameDeclaration decl) {
		classNames.put(decl, new ArrayList());
	}

	@Override
	protected NameDeclaration findVariableHere(NameOccurrence occurrence) {
		// BUGFOUND 20090408
		// how about class x { int a; void f() { this.a = 10; } }
		// this <-> VariableNameDeclaration ?
		if (occurrence.getImage().equals("super")) {
			return null;
		}
		
		//if (occurrence.isThisOrSuper() || occurrence.getImage().equals(className)) {
		if (occurrence.getImage().equals(className)||occurrence.getImage().equals("this")) {
			if(getParent()!=null){
				return Search.searchUpward(className,getParent());
			}
			return null;
		}

		if (occurrence.isMethodOrConstructorInvocation()) {
			SimpleNode n = occurrence.getLocation();
			Class[] parameterTypes = null;
			if (n instanceof ASTName) {
				parameterTypes=((ASTName)n).getParameterTypes();
			} 
			else if (n instanceof ASTPrimarySuffix) {
				parameterTypes=((ASTPrimarySuffix)n).getParameterTypes();
			}
			
			
			MethodNameDeclaration firstmnd=null;
			for (Iterator i = methodNames.keySet().iterator(); i.hasNext();) {
				MethodNameDeclaration mnd = (MethodNameDeclaration) i.next();
				if (mnd.getImage().equals(occurrence.getImage())) {
					int args = occurrence.getArgumentCount();
					if (args == mnd.getParameterCount()) {
						// FIXME if several methods have the same name
						// and parameter count, only one will get caught here
						// we need to make some attempt at type lookup and discrimination
						// or, failing that, mark this as a usage of all those methods
						if(firstmnd==null){
							firstmnd=mnd;
						}
						if(parameterTypes!=null){
							Class[] cs=mnd.getParameterClasses();
							boolean match=true;
							for(int j=0;j<parameterTypes.length;j++){
								Class c1=parameterTypes[j];
								Class c2=cs[j];
								if(c1!=null){
									if(!isSelfOrSuperClass(c1,c2)){
										match=false;
										break;
									}
								}else{
									if(c2==null||c2.isPrimitive()){
										match=false;
										break;
									}
								}
							}
							if(match){
								return mnd;
							}else{
								continue;
							}
						}					
						//return mnd;
					}
				}
			}
			//return null;
			return firstmnd;
		}

		List images = new ArrayList();
		images.add(occurrence.getImage());
		if (occurrence.getImage().startsWith(className)) {
			images.add(clipClassName(occurrence.getImage()));
		}
		ImageFinderFunction finder = new ImageFinderFunction(images);
		Applier.apply(finder, variableNames.keySet().iterator());
		//added by xqing
		if (finder.getDecl() == null) {
			finder = new ImageFinderFunction(occurrence.getImage());
			Applier.apply(finder, classNames.keySet().iterator());
		}
		return finder.getDecl();
	}

	@Override
	public String toString() {
		String res = "ClassScope (" + className + "): ";
		if (!classNames.isEmpty())
			res += "(" + glomNames(classNames.keySet().iterator()) + ")";
		if (!methodNames.isEmpty()) {
			Iterator i = methodNames.keySet().iterator();
			while (i.hasNext()) {
				MethodNameDeclaration mnd = (MethodNameDeclaration) i.next();
				res += mnd.toString();
				int usages = ((List) methodNames.get(mnd)).size();
				res += "(begins at line " + mnd.getNode().getBeginLine() + ", " + usages + " usages)";
				res += ",";
			}
		}
		if (!variableNames.isEmpty())
			res += "(" + glomNames(variableNames.keySet().iterator()) + ")";
		return res;
	}

	private String clipClassName(String in) {
		return in.substring(in.indexOf('.') + 1);
	}

	//added by xqing
	@Override
	public String dump() {
		StringBuffer b = new StringBuffer();
		b.append("ClassScope (" + className + "): ");
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
		if (!methodNames.isEmpty()) {
			b.append("(methods: ");
			Iterator i = methodNames.keySet().iterator();
			while (i.hasNext()) {
				MethodNameDeclaration mnd = (MethodNameDeclaration) i.next();
				b.append(mnd.getImage().toString());
				if (i.hasNext()) {
					b.append(",");
				}
			}
			b.append(")");
		}
		if (!variableNames.isEmpty()) {
			b.append("(variables: ");
			Iterator i = variableNames.keySet().iterator();
			while (i.hasNext()) {
				VariableNameDeclaration mnd = (VariableNameDeclaration) i.next();
				b.append(mnd.getImage().toString());
				if (i.hasNext()) {
					b.append(",");
				}
			}
			b.append(")");
		}
		return b.toString();
	}
	
	private boolean isSelfOrSuperClass(Class child,Class c){
		boolean ret=false;
		while(!ret&&child!=null){
			if(child==c){
				ret=true;
			}
			child=child.getSuperclass();
		}
		return ret;
	}
	
	public int getAnonymousCounter() {
		return anonymousInnerClassCounter;
	}
	
	public void incAnonymousCounter() {
		++anonymousInnerClassCounter;
	}
}
