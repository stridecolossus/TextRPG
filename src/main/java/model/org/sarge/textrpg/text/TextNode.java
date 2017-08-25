package org.sarge.textrpg.text;

/**
 * Text-node.
 * @author Sarge
 */
public interface TextNode {
    /**
     * @return This text node
     */
    String getValue();

    /**
     * Looks up a child of this node.
     * @param key Key
     * @return Child or <tt>null</tt> if none
     */
    TextNode getChild(String key);

    /**
     * @param str String to match
     * @return Whether this node matches the given string
     */
    boolean matches(String str);
}
