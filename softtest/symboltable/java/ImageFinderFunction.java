/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import softtest.util.java.UnaryFunction;

public class ImageFinderFunction implements UnaryFunction {

    private Set images = new HashSet();
    private NameDeclaration decl;

    public ImageFinderFunction(String img) {
        images.add(img);
    }

    public ImageFinderFunction(List imageList) {
        images.addAll(imageList);
    }

    public void applyTo(Object o) {
        NameDeclaration nameDeclaration = (NameDeclaration) o;
        if (images.contains(nameDeclaration.getImage())) {
            decl = nameDeclaration;
        }
    }

    public NameDeclaration getDecl() {
        return this.decl;
    }
}
