package org.sarge.textrpg.text;

import org.sarge.lib.util.Check;

/**
 * Simple string node.
 * @author Sarge
 */
public class StringTextNode implements TextNode {
    private final String value;

    /**
     * Constructor.
     * @param value String value
     */
    public StringTextNode(String value) {
        this.value = Check.notEmpty(value);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean matches(String str) {
        return value.equals(str);
    }

    @Override
    public TextNode getChild(String key) {
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
