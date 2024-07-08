/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Keeps track of the types encountered in a ASTCompilationUnit
 */
public class TypeSet {//声明的类型名，类型信息放入哈希表

    /**
     * TODO should Resolver provide a canResolve() and a resolve()?
     * Requiring 2 calls seems clunky... but so does this
     * throwing an exception for flow control...
     */
    public interface Resolver {
        Class resolve(String name) throws ClassNotFoundException;
    }

    public static class ExplicitImportResolver implements Resolver {//明确的外部引入的类
        private Set importStmts;
        private ClassLoader classLoader;

        public ExplicitImportResolver(Set importStmts, ClassLoader classLoader) {
            this.importStmts = importStmts;
            this.classLoader = classLoader;
        }

        public Class resolve(String name) throws ClassNotFoundException {
            for (Iterator i = importStmts.iterator(); i.hasNext();) {
                String importStmt = (String) i.next();
                // BUGFIX 20090302 yangxiu
                //if (importStmt.endsWith(name)) {
                if (importStmt.endsWith("."+name)) {                    
                    try {
                    	return Class.forName(importStmt,false,classLoader);
                	} catch (SecurityException e) {
                		
                	}
                	try {
                		return Class.forName(importStmt);
                	} catch (SecurityException e) {
                		
                	}
        			throw new SecurityException(importStmt);                    
                }
            }
            throw new ClassNotFoundException("Type " + name + " not found");
        }
    }

    public static class CurrentPackageResolver implements Resolver {//当前文件的包名
        private String pkg;
        private ClassLoader classLoader;

        public CurrentPackageResolver(String pkg, ClassLoader classLoader) {
            this.pkg = pkg;
            this.classLoader = classLoader;
        }
    
        public Class resolve(String name) throws ClassNotFoundException {//
        	try {
        		return Class.forName(pkg+ "." + name,false,classLoader);
        	} catch (SecurityException e) {
        		
        	} catch (NoClassDefFoundError e) {
        		// BUGFOUND
        	}
        	try {
        		return Class.forName(pkg+ "." + name);
        	} catch (SecurityException e) {
        		
        	}
        	 catch (NoClassDefFoundError e) {
         		// BUGFOUND
         	}
			throw new SecurityException(pkg+ "." + name);
        }
    }

    // TODO cite the JLS section on implicit imports
    public static class ImplicitImportResolver implements Resolver {//java自带的基础类，object，string，class，
    	private ClassLoader classLoader;
    	public ImplicitImportResolver(ClassLoader classLoader) {
    		this.classLoader = classLoader;
    	}
        public Class resolve(String name) throws ClassNotFoundException {
            try {
            	return Class.forName("java.lang." + name,false,classLoader);
        	}
            catch (SecurityException e) {
        		
        	}
        	try {
        		return Class.forName("java.lang." + name);
        	} catch (SecurityException e) {
        		
        	}
			throw new SecurityException("java.lang." + name);    
        }
    }

    public static class ImportOnDemandResolver implements Resolver {//按需引入
        private Set importStmts;
        private ClassLoader classLoader;

        public ImportOnDemandResolver(Set importStmts,ClassLoader classLoader) {
            this.importStmts = importStmts;
            this.classLoader = classLoader;
        }

        public Class resolve(String name) throws ClassNotFoundException {
        	boolean sec = false;
            for (Iterator i = importStmts.iterator(); i.hasNext();) {
                String importStmt = (String) i.next();
                if (importStmt.endsWith("*")) {
                    try {
                        String importPkg = importStmt.substring(0, importStmt.indexOf('*') - 1);
                        return Class.forName(importPkg + '.' + name,false,classLoader);
                    } catch (ClassNotFoundException cnfe) {
                    } catch (SecurityException e) {                    	
                    }
                    try {
                        String importPkg = importStmt.substring(0, importStmt.indexOf('*') - 1);
                        return Class.forName(importPkg + '.' + name);
                    } catch (ClassNotFoundException cnfe) {
                    } catch (SecurityException e) {
                    	sec = true;
                    }
                }
            }
            if (sec) {
            	throw new SecurityException("Type " + name + " not found");
            } else {
            	throw new ClassNotFoundException("Type " + name + " not found");
            }
        }
    }

    public static class PrimitiveTypeResolver implements Resolver {//基本类型
        private Map primitiveTypes = new HashMap();
        private ClassLoader classLoader;
        
        public PrimitiveTypeResolver(ClassLoader classLoader) {
        	this.classLoader = classLoader;
            primitiveTypes.put("int", int.class);
            primitiveTypes.put("float", float.class);
            primitiveTypes.put("double", double.class);
            primitiveTypes.put("long", long.class);
            primitiveTypes.put("boolean", boolean.class);
            primitiveTypes.put("byte", byte.class);
            primitiveTypes.put("short", short.class);
            primitiveTypes.put("char", char.class);
        }

        public PrimitiveTypeResolver() {
            primitiveTypes.put("int", int.class);
            primitiveTypes.put("float", float.class);
            primitiveTypes.put("double", double.class);
            primitiveTypes.put("long", long.class);
            primitiveTypes.put("boolean", boolean.class);
            primitiveTypes.put("byte", byte.class);
            primitiveTypes.put("short", short.class);
            primitiveTypes.put("char", char.class);
        }

        public Class resolve(String name) throws ClassNotFoundException {
            if (!primitiveTypes.containsKey(name)) {
                throw new ClassNotFoundException();
            }
            return (Class) primitiveTypes.get(name);
        }
    }

    public static class VoidResolver implements Resolver {//void类
    	private ClassLoader classLoader;
        
        public VoidResolver(ClassLoader classLoader) {
        	this.classLoader = classLoader;
        }
        public Class resolve(String name) throws ClassNotFoundException {
            if (name.equals("void")) {
                return void.class;
            }
            throw new ClassNotFoundException();
        }
    }

    public static class FullyQualifiedNameResolver implements Resolver {//标识符名
    	private ClassLoader classLoader;
    	public FullyQualifiedNameResolver(ClassLoader classLoader) {
    		this.classLoader = classLoader;
        }
        public Class resolve(String name) throws ClassNotFoundException {
            try {
            	return Class.forName(name,false,classLoader);
        	}
            catch (SecurityException e) {
        		
        	}
        	try {
        		return Class.forName(name);
        	} catch (SecurityException e) {
        		
        	}
			throw new SecurityException("java.lang." + name);   
        }
    }

    private static String pkg;
    private static Set imports = new HashSet();
    private static Set staticimports =new HashSet();
    private static List resolvers = new ArrayList();
    private static ClassLoader classLoader;
    private static TypeSet currenttypeset;
    private static Stack<String> currentclassname=new Stack<String>();
    
    public static void clear() {
    	resolvers.clear();
    	currentclassname.clear();
    	imports.clear();
    	staticimports.clear();
    }
    
    /*
     * Win:
     * classpath == "D:\\tmp\\ant.jar;D:\\tmp"
     * 
     * Linux/Unix
     * classpath == "/home/lib/ant.jar:/usr/lib/classes"
     */
    public static void pushClassName(String classname){
    	currentclassname.push(classname);
    }
    public static String popClassName(){
    	return currentclassname.pop();
    }
    
    public static String getCurrentClassName() {
    	StringBuffer sb = new StringBuffer(TypeSet.pkg);
    	for (String s : currentclassname) {
    		sb.append(".");
    		sb.append(s);
    	}
    	return sb.toString();
    }


	public TypeSet(String classpath) {
    	if ( classpath == null ) {
    		TypeSet.classLoader = TypeSet.class.getClassLoader();
    	} else {
    		TypeSet.classLoader = (new Classpath(classpath)).getClassLoader();
    	}
    	currenttypeset=this;
    }
    
    private TypeSet() {
    	TypeSet.classLoader = TypeSet.class.getClassLoader();
    	currenttypeset=this;
    }
    
    public static TypeSet getCurrentTypeSet(){
    	return currenttypeset;
    }

    public void setASTCompilationUnitPackage(String pkg) {
        TypeSet.pkg = pkg;
    }

    public String getASTCompilationUnitPackage() {
        return pkg;
    }

    public void addImport(String importString) {
        imports.add(importString);
    }
    
    public void addStaticImport(String importString){
    	staticimports.add(importString);
    }
    
    public Class findStaticImportClass(String name) throws ClassNotFoundException{
    	String importStmt ="";
    	String classname="";
    	for (Iterator i = staticimports.iterator(); i.hasNext();) {
    		importStmt = (String) i.next();
            if (importStmt.endsWith(name)) {
            	classname =importStmt.substring(0,importStmt.lastIndexOf('.'));
                return Class.forName(classname,false,classLoader);
            }
        }
        throw new ClassNotFoundException("Type " + classname + " not found");
    }

    public int getImportsCount() {
        return imports.size();
    }

    public Class findClass(String name) throws ClassNotFoundException {
        // we don't build the resolvers until now since we first want to get all the imports
        if (resolvers.isEmpty()) {
            buildResolvers();
        }

        for (Iterator i = resolvers.iterator(); i.hasNext();) {
            Resolver resolver = (Resolver) i.next();
            try {
                return resolver.resolve(name);
            } catch (ClassNotFoundException cnfe) {
            }catch (NoClassDefFoundError e) {
    		}
        }
        
        //尝试查找内部类,从当前层向上层直到发现可用的classname
        for (int j = currentclassname.size(); j >= 0; --j) {

			// 尝试查找内部类
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < j; i++) {				
				b.append(currentclassname.get(i) + "$");
			}
			
			String completename = b.append(name).toString();
			for (Iterator i = resolvers.iterator(); i.hasNext();) {
				Resolver resolver = (Resolver) i.next();
				try {
					return resolver.resolve(completename);
				} catch (ClassNotFoundException cnfe) {
				}
				catch (NoClassDefFoundError e) {
				}
			}
		}

        throw new ClassNotFoundException("Type " + name + " not found");
    }
    
    public Class findClassWithoutEx(String name){
    	Class ret=null;
    	try{		
			ret = findClass(name);
		}catch (ClassNotFoundException e) {
		}catch (NoClassDefFoundError e) {
		}
    	return ret;
    }
    
    public Class findStaticImportClassWithoutEx(String name){
    	Class ret=null;
    	try{		
			ret = findStaticImportClass(name);
		}catch (ClassNotFoundException e) {
		}catch (NoClassDefFoundError e) {
		}
    	return ret;
    }    

    private void buildResolvers() {
        resolvers.add(new PrimitiveTypeResolver(classLoader));
        resolvers.add(new VoidResolver(classLoader));
        resolvers.add(new ExplicitImportResolver(imports,classLoader));
        resolvers.add(new CurrentPackageResolver(pkg,classLoader));
        resolvers.add(new ImplicitImportResolver(classLoader));
        resolvers.add(new ImportOnDemandResolver(imports,classLoader));
        resolvers.add(new FullyQualifiedNameResolver(classLoader));
    }

}


class Classpath {
	Set<File> _elements = new HashSet<File>();

	public Classpath() {
	}

	public Classpath(String initial) {
		addClasspath(initial);
	}

	public boolean addComponent(String component) {
		if ((component != null) && (component.length() > 0)) {
			try {
				File f = new File(component);
				if (f.exists()) {
					File key = f.getCanonicalFile();
					if (!_elements.contains(key)) {
						_elements.add(key);
						
						if ( key.isDirectory() ) {
							File [] jars = key.listFiles();
							for (File jar : jars) {
								if (jar.getName().endsWith(".jar")) {
									if (!_elements.contains(jar)) {
										_elements.add(jar);
									}
								}
							}
						}
						
						return true;
					}
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean addComponent(File component) {
		if (component != null) {
			try {
				if (component.exists()) {
					File key = component.getCanonicalFile();
					if (!_elements.contains(key)) {
						_elements.add(key);
						return true;
					}
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean addClasspath(String s) {
		boolean added = false;
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, File.pathSeparator);
			while (t.hasMoreTokens()) {
				added |= addComponent(t.nextToken());
			}
		}
		return added;
	}

	@Override
	public String toString() {
		StringBuffer cp = new StringBuffer(1024);
		boolean flag = false;
		for (File f : _elements) {
			if (flag)
				cp.append(File.pathSeparatorChar);
			else
				flag = true;
			cp.append(f.getPath());
		}
		return cp.toString();
	}

	public URL[] getUrls() {
		URL[] urls = new URL[_elements.size()];
		int cnt = 0;
		for (File f : _elements) {
			try {
				urls[cnt] = f.toURI().toURL();
			} catch (MalformedURLException e) {
				urls[cnt] = null;
			}
			++cnt;
		}
		return urls;
	}

	public ClassLoader getClassLoader() {
		URL[] urls = getUrls();

		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if (parent == null) {
			parent = Classpath.class.getClassLoader();
		}
		if (parent == null) {
			parent = ClassLoader.getSystemClassLoader();
		}
		return new URLClassLoader(urls, parent);
	}
}

