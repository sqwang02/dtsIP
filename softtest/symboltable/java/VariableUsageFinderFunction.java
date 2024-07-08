/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.HashMap;
import java.util.Map;

import softtest.util.java.UnaryFunction;

public class VariableUsageFinderFunction implements UnaryFunction {

    private Map results = new HashMap();
    private Map decls;

    public VariableUsageFinderFunction(Map decls) {
        this.decls = decls;
    }

    public void applyTo(Object o) {
        results.put(o, decls.get(o));
    }

    public Map getUsed() {
        return results;
    }
}
