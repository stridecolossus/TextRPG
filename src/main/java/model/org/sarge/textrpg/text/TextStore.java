package org.sarge.textrpg.text;

import org.sarge.lib.util.Check;

/**
 * Store of text strings indexed by key.
 * @author Sarge
 */
public final class TextStore {
    private final GroupTextNode root;

    public TextStore(GroupTextNode root) {
        Check.notNull(root);
        this.root = root;
    }

    public String get(String key) {
        final TextNode node = find(key);
        if(node == null) {
            return null;
        }
        else {
            return node.getValue();
        }
    }

    public boolean matches(String key, String value) {
        final TextNode node = find(key);
        if(node == null) {
            return false;
        }
        else {
            return node.matches(value);
        }
    }

    private TextNode find(String key) {
        // Tokenize path
        final String[] path = key.split("\\.");

        // Walk to specified node
        TextNode node = root;
        for(String p : path) {
            node = node.getChild(p);
            if(node == null) return null;
        }

        // Get text
        return node;
    }
}
