package org.sarge.textrpg.text;

import java.util.Map;

import org.sarge.lib.util.StrictMap;

/**
 * Group node.
 * @author Sarge
 */
public class GroupTextNode extends StringTextNode {
    private final Map<String, TextNode> children;

    /**
     * Constructor.
     * @param value         Group name
     * @param children      Child nodes
     */
    public GroupTextNode(String value, Map<String, TextNode> children) {
        super(value);
        this.children = new StrictMap<>(children);
    }

    @Override
    public TextNode getChild(String key) {
        return children.get(key);
    }
}
