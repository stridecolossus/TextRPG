package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class TopicTest {
    private Topic topic;

    @Before
    public void before() {
        topic = new Topic("name", "text");
    }

    @Test
    public void constructor() {
        assertEquals("name", topic.name());
    }

    @Test
    public void script() {
        final Script script = topic.script();
        assertNotNull(script);

        final Actor actor = mock(Actor.class);
        script.execute(actor);
        verify(actor).alert(new Message("text"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shop() {
        topic.shop();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void trainer() {
        topic.trainer();
    }
}
