/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.jaxen.java;

import org.jaxen.DefaultNavigator;
import org.jaxen.XPath;
import org.jaxen.util.SingleObjectIterator;

import softtest.ast.java.CompilationUnit;
import softtest.ast.java.Node;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author daniels
 */
public class DocumentNavigator extends DefaultNavigator {

    private final static Iterator EMPTY_ITERATOR = new ArrayList().iterator();

    @Override
	public String getAttributeName(Object arg0) {
        return ((Attribute) arg0).getName();
    }

    @Override
	public String getAttributeNamespaceUri(Object arg0) {
        return "";
    }

    @Override
	public String getAttributeQName(Object arg0) {
        return ((Attribute) arg0).getName();
    }

    @Override
	public String getAttributeStringValue(Object arg0) {
        return ((Attribute) arg0).getValue();
    }

    @Override
	public String getCommentStringValue(Object arg0) {
        return "";
    }

    @Override
	public String getElementName(Object node) {
        return node.toString();
    }

    @Override
	public String getElementNamespaceUri(Object arg0) {
        return "";
    }

    @Override
	public String getElementQName(Object arg0) {
        return getElementName(arg0);
    }

    @Override
	public String getElementStringValue(Object arg0) {
        return "";
    }

    @Override
	public String getNamespacePrefix(Object arg0) {
        return "";
    }

    @Override
	public String getNamespaceStringValue(Object arg0) {
        return "";
    }

    @Override
	public String getTextStringValue(Object arg0) {
        return "";
    }

    @Override
	public boolean isAttribute(Object arg0) {
        return arg0 instanceof Attribute;
    }

    @Override
	public boolean isComment(Object arg0) {
        return false;
    }

    @Override
	public boolean isDocument(Object arg0) {
        return arg0 instanceof CompilationUnit;
    }

    @Override
	public boolean isElement(Object arg0) {
        return arg0 instanceof Node;
    }

    @Override
	public boolean isNamespace(Object arg0) {
        return false;
    }

    @Override
	public boolean isProcessingInstruction(Object arg0) {
        return false;
    }

    @Override
	public boolean isText(Object arg0) {
        return false;
    }

    @Override
	public XPath parseXPath(String arg0) {
        return null;
    }

    @Override
	public Object getParentNode(Object arg0) {
        if (arg0 instanceof Node) {
            return ((Node) arg0).jjtGetParent();
        }
        return ((Attribute) arg0).getParent();
    }

    @Override
	public Iterator getAttributeAxisIterator(Object arg0) {
        return new AttributeAxisIterator((Node) arg0);
    }

    /**
     * Get an iterator over all of this node's children.
     *
     * @param contextNode The context node for the child axis.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getChildAxisIterator(Object contextNode) {
        return new NodeIterator((Node) contextNode) {
            @Override
			protected Node getFirstNode(Node node) {
                return getFirstChild(node);
            }

            @Override
			protected Node getNextNode(Node node) {
                return getNextSibling(node);
            }
        };
    }

    /**
     * Get a (single-member) iterator over this node's parent.
     *
     * @param contextNode the context node for the parent axis.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getParentAxisIterator(Object contextNode) {
        if (isAttribute(contextNode)) {
            return new SingleObjectIterator(((Attribute) contextNode).getParent());
        }
        Node parent = ((Node) contextNode).jjtGetParent();
        if (parent != null) {
            return new SingleObjectIterator(parent);
        } else {
            return EMPTY_ITERATOR;
        }
    }

    /**
     * Get an iterator over all following siblings.
     *
     * @param contextNode the context node for the sibling iterator.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getFollowingSiblingAxisIterator(Object contextNode) {
        return new NodeIterator((Node) contextNode) {
            @Override
			protected Node getFirstNode(Node node) {
                return getNextNode(node);
            }

            @Override
			protected Node getNextNode(Node node) {
                return getNextSibling(node);
            }
        };
    }

    /**
     * Get an iterator over all preceding siblings.
     *
     * @param contextNode The context node for the preceding sibling axis.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getPrecedingSiblingAxisIterator(Object contextNode) {
        return new NodeIterator((Node) contextNode) {
            @Override
			protected Node getFirstNode(Node node) {
                return getNextNode(node);
            }

            @Override
			protected Node getNextNode(Node node) {
                return getPreviousSibling(node);
            }
        };
    }

    /**
     * Get an iterator over all following nodes, depth-first.
     *
     * @param contextNode The context node for the following axis.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getFollowingAxisIterator(Object contextNode) {
        return new NodeIterator((Node) contextNode) {
            @Override
			protected Node getFirstNode(Node node) {
                if (node == null)
                    return null;
                else {
                    Node sibling = getNextSibling(node);
                    if (sibling == null)
                        return getFirstNode(node.jjtGetParent());
                    else
                        return sibling;
                }
            }

            @Override
			protected Node getNextNode(Node node) {
                if (node == null)
                    return null;
                else {
                    Node n = getFirstChild(node);
                    if (n == null)
                        n = getNextSibling(node);
                    if (n == null)
                        return getFirstNode(node.jjtGetParent());
                    else
                        return n;
                }
            }
        };
    }

    /**
     * Get an iterator over all preceding nodes, depth-first.
     *
     * @param contextNode The context node for the preceding axis.
     * @return A possibly-empty iterator (not null).
     */
    @Override
	public Iterator getPrecedingAxisIterator(Object contextNode) {
        return new NodeIterator((Node) contextNode) {
            @Override
			protected Node getFirstNode(Node node) {
                if (node == null)
                    return null;
                else {
                    Node sibling = getPreviousSibling(node);
                    if (sibling == null)
                        return getFirstNode(node.jjtGetParent());
                    else
                        return sibling;
                }
            }

            @Override
			protected Node getNextNode(Node node) {
                if (node == null)
                    return null;
                else {
                    Node n = getLastChild(node);
                    if (n == null)
                        n = getPreviousSibling(node);
                    if (n == null)
                        return getFirstNode(node.jjtGetParent());
                    else
                        return n;
                }
            }
        };
    }

    @Override
	public Object getDocumentNode(Object contextNode) {
        if (isDocument(contextNode)) {
            return contextNode;
        }
        return getDocumentNode(getParentNode(contextNode));
    }
}
