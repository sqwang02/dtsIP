/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;
import java.util.*;
public class Search {
    private static final boolean TRACE = false;

    private NameOccurrence occ;
    private NameDeclaration decl;
    private String image;

    public Search(NameOccurrence occ) {
        if ( TRACE && occ!=null) System.out.println("new search for " + (occ.isMethodOrConstructorInvocation() ? "method" : "variable") + " " + occ);
        this.occ = occ;
    }

    public void execute() {
    	if(occ!=null){
    		decl = searchUpward(occ, occ.getLocation().getScope());
    	}
        if (TRACE) System.out.println("found " + decl);
    }
    
    public Search(String image) {
    	this.image=image;
    }
    
    public void execute(Scope startingScope) {
    	if(occ!=null){
    		decl = searchUpward(occ, startingScope);
    		if (TRACE) System.out.println("found " + decl);
    	}else{
    		decl = searchUpward(image, startingScope);
    	}
    }

    public NameDeclaration getResult() {
        return decl;
    }

    private NameDeclaration searchUpward(NameOccurrence nameOccurrence, Scope scope) {
        if (TRACE) System.out.println("checking scope " + scope + " for name occurrence " + nameOccurrence);
        if (!scope.contains(nameOccurrence) && scope.getParent() != null) {
            if (TRACE) System.out.println("moving up fm " + scope + " to " + scope.getParent());
            return searchUpward(nameOccurrence, scope.getParent());
        }
        if (scope.contains(nameOccurrence)) {
            if (TRACE) System.out.println("found it!");
            return scope.addVariableNameOccurrence(nameOccurrence);
        }
        return null;
    }
    
    //added by xqing
    /** 用于非Occurrence的字符串查找 */
    public static NameDeclaration searchUpward(String image, Scope scope) {
    	Map names=null;
    	NameDeclaration decl =null;
    	if(image==null){
    		return null;
    	}
    	names=scope.getClassDeclarations();
    	decl=searchNames(image,names);
    	if(decl==null){
    		names=scope.getVariableDeclarations();
    		decl=searchNames(image,names);
    	}
    	if(decl==null){
    		names=scope.getMethodDeclarations();
    		decl=searchNames(image,names);
    	}
    	if(decl==null&&scope.getParent()!=null){
    		decl=searchUpward(image,scope.getParent());
    	}
    	return decl;
    }
    
    public static  NameDeclaration searchNames(String image,Map names){
    	if(names!=null){
    		Iterator i=names.keySet().iterator();
    		while(i.hasNext()){
    			NameDeclaration decl=(NameDeclaration)i.next();
    			if(image.equals(decl.getImage())){
    				return decl;
    			}
    		}
    	}
    	return null;
    }
}
