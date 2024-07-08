package softtest.symboltable.java;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
//added by xqing
import softtest.callgraph.java.*;

/**
 * Implementation of Scope for source types that are simpler than java sources.
 * It implements the methods only when necessary not to break at runtime
 * when Violations are handled.
 *
 * @author pieter_van_raemdonck - Application Engineers NV/SA - www.ae.be
 */
public class DummyScope implements Scope {
    private Map emptyMap = new HashMap();

    private Scope parent;

    public Map getVariableDeclarations() {
        return emptyMap;
    }

    public Map getClassDeclarations() {
        return emptyMap;
    }
    
    //added by xqing 
    public Map getMethodDeclarations(){
    	return emptyMap;
    }

    public void addDeclaration(ClassNameDeclaration decl) {
    }

    public void addDeclaration(VariableNameDeclaration decl) {
    }

    public void addDeclaration(MethodNameDeclaration decl) {
    }

    public boolean contains(NameOccurrence occ) {
        return false;
    }

    public NameDeclaration addVariableNameOccurrence(NameOccurrence occ) {
        return null;
    }

    public void setParent(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public ClassScope getEnclosingClassScope() {
        return new ClassScope();
    }

    public SourceFileScope getEnclosingSourceFileScope() {
        return new SourceFileScope();
    }

    public MethodScope getEnclosingMethodScope() {
        return null;
    }
    //added by xqing 
    public List getChildrens(){
		return null;
	}
    public void resolveTypes(TypeSet typeset){
    	
    }
    
    public void resolveCallRelation(CGraph g){
    	
    }
    
    public void initDomains(){
    	
    }
    public void initDefUse(){
    	
    }
    
    public String print(){
    	return "";
    }
    
    public boolean isSelfOrAncestor(Scope ancestor){
    	return false;
    }
}
