package org.sarge.textrpg.text;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class TextStoreTest {
    private TextStore store;

    @Before
    public void before() {
        final GroupTextNode root = new GroupTextNode("parent", Collections.singletonMap("child", new StringTextNode("value")));
        store = new TextStore(root);
    }

    @Test
    public void get() {
        assertEquals("value", store.get("parent.child"));
        assertEquals("parent", store.get("parent"));
        assertEquals(null, store.get("cobblers"));
        assertEquals(null, store.get("parent.cobblers"));
        assertEquals(null, store.get("parent.child.cobblers"));
    }
}
