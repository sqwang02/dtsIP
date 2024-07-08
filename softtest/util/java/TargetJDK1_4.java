/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.util.java;


import java.io.InputStream;
import java.io.Reader;

import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserTokenManager;

/**
 * This is an implementation of {@link softtest.util.java.TargetJDKVersion} for
 * JDK 1.4.
 *
 * @author Tom Copeland
 */
public class TargetJDK1_4 implements TargetJDKVersion {

    /**
     * @see softtest.util.java.TargetJDKVersion#createParser(InputStream)
     */
    public JavaParser createParser(InputStream in) {
        return new JavaParser(new JavaCharStream(in));
    }

    /**
     * @see softtest.util.java.TargetJDKVersion#createParser(Reader)
     */
    public JavaParser createParser(Reader in) {
        return new JavaParser(new JavaCharStream(in));
    }

    /**
     * Creates a token manager for the parser.
     *
     * @param in the reader for which to create a token manager
     * @return a token manager
     */
    public JavaParserTokenManager createJavaParserTokenManager(Reader in) {
        return new JavaParserTokenManager(new JavaCharStream(in));
    }

    public String getVersionString() {
        return "1.4";
    }

}
