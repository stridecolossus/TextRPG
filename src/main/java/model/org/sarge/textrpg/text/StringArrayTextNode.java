package org.sarge.textrpg.text;

import java.util.Arrays;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;

/**
 * String-array node.
 * @author chris
 */
public class StringArrayTextNode implements TextNode {
    private final String[] array;

    /**
     * Constructor.
     * @param array Array of strings
     */
    public StringArrayTextNode(String[] array) {
        Check.notEmpty(array);
        if(Arrays.stream(array).anyMatch(StringUtil::isEmpty)) throw new IllegalArgumentException("Array value(s) cannot be empty");
        this.array = Arrays.copyOf(array, array.length);
    }

    @Override
    public String getValue() {
        return array[0];
    }

    @Override
    public boolean matches(String str) {
        return Arrays.stream(array).anyMatch(str::equals);
    }

    @Override
    public TextNode getChild(String key) {
        return null;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
