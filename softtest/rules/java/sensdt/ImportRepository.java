package softtest.rules.java.sensdt;

import java.util.Hashtable;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;


public class ImportRepository {

	private Package repository; //  java.lang.*; java.lang.System.out; java.lang.System

	public ImportRepository(SimpleJavaNode node) {
		SimpleJavaNode parent = node;
		while (!(parent instanceof ASTCompilationUnit)) {
			parent = (SimpleJavaNode) parent.jjtGetParent();
			if (parent == null) {
				throw new RuntimeException("No ASTCompilationUnit found.");
			}
		}
		repository = new Package("root");
		ASTCompilationUnit root = (ASTCompilationUnit) parent;
		int chcnt = root.jjtGetNumChildren();
		for (int i = 0; i < chcnt; i++) {
			SimpleJavaNode ch = (SimpleJavaNode) root.jjtGetChild(i);
			if (ch instanceof ASTImportDeclaration) {
				ASTImportDeclaration astImptDecl = (ASTImportDeclaration) ch;
				ASTName astName = (ASTName) astImptDecl.jjtGetChild(0);
				String imptedName = astImptDecl.getImportedName();
				String names[] = imptedName.split("\\.");
				Package level = repository;
				/**  import [static] java.lang.*  **/
				if (astImptDecl.isImportOnDemand()) {

					/**  import java.lang.*  **/
					if (!astImptDecl.isStatic()) {
						for (int j = 0; j < names.length; j++) {
							level = level.getPkgAndCreateIfNeed(names[j]);
						}
						level.toImportOnDemand();
					}
					/**  import static java.lang.System.*  **/
					else {
						for (int j = 0; j < names.length - 1; j++) {
							level = level.getPkgAndCreateIfNeed(names[j]);
						}
						level.addClassAndMember(names[names.length - 1], "*");
					}
				}
				/**  import [static] java.lang.System[.out]  **/
				else {
					/**  import java.lang.System  **/
					if (!astImptDecl.isStatic()) {
						for (int j = 0; j < names.length - 1; j++) {
							level = level.getPkgAndCreateIfNeed(names[j]);
						}
						level.addClass(names[names.length - 1]);
					}
					/**  import static java.lang.System.out  **/
					else {
						for (int j = 0; j < names.length - 2; j++) {
							level = level.getPkgAndCreateIfNeed(names[j]);
						}
						int idx = names.length - 2;
						level.addClassAndMember(names[idx], names[idx + 1]);
					}
				}
			}
		}
	}

	static class Package {
		String name; //  java io lang etc

		Hashtable<String, Package> incPkgs;

		Hashtable<String, Cls> incCls;

		boolean importOnDemand; //  java.lang.*

		public Package(String pkg) {
			name = pkg;
			importOnDemand = false;
			incPkgs = new Hashtable<String, Package>();
			incCls = new Hashtable<String, Cls>();
		}

		public void toImportOnDemand() {
			importOnDemand = true;
		}

		public void addClassAndMember(String cname, String member) {
			Cls cls = incCls.get(cname);
			if (cls == null) {
				cls = new Cls(cname);
				if (member.compareTo("*") != 0) {
					cls.addStaticMember(member);
				} else {
					cls.tobeImportOnDemand();
				}
				incCls.put(cname, cls);
			}
		}

		public void addClass(String cname) {
			Cls cls = incCls.get(cname);
			if (cls == null) {
				incCls.put(cname, new Cls(cname));
			}
		}

		public Cls getClass(String cls) {
			return incCls.get(cls);
		}

		public Package getPkg(String name) {
			return incPkgs.get(name);
		}

		public Package getPkgAndCreateIfNeed(String name) {
			Package pk = incPkgs.get(name);
			if (pk == null) {
				pk = new Package(name);
				incPkgs.put(name, pk);
			}
			return pk;
		}

		class Cls {
			String name;

			Hashtable<String, String> staticMember;

			boolean importOnDemand; //  java.lang.*

			public Cls(String cname) {
				name = cname;
				staticMember = new Hashtable<String, String>();
				importOnDemand = false;
			}

			public void addStaticMember(String mname) {
				staticMember.put(mname, mname);
			}

			public String getStaticMember(String mname) {
				return staticMember.get(mname);
			}

			public boolean hasStaticMemberObviously(String mname) {
				if (staticMember.get(mname) != null) {
					return true;
				} else {
					return false;
				}
			}

			public void tobeImportOnDemand() {
				importOnDemand = true;
			}

			public boolean isImportOnDemand() {
				return importOnDemand;
			}
		}
	}
}
