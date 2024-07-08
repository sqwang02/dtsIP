/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.util.java;


import java.io.InputStream;
import java.io.Reader;

import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;

/**
 * This is an implementation of {@link softtest.util.java.TargetJDKVersion} for
 * JDK 1.3.
 *
 * @author Tom Copeland
 */
public class TargetJDK1_3 implements TargetJDKVersion {

    /**
     * @see softtest.util.java.TargetJDKVersion#createParser(InputStream)
     */
    public JavaParser createParser(InputStream in) {
        JavaParser jp = new JavaParser(new JavaCharStream(in));
        jp.setJDK13();
        return jp;
    }

    /**
     * @see softtest.util.java.TargetJDKVersion#createParser(Reader)
     */
    public JavaParser createParser(Reader in) {
        JavaParser jp = new JavaParser(new JavaCharStream(in));
        jp.setJDK13();
        return jp;
    }

    public String getVersionString() {
        return "1.3";
    }

}
