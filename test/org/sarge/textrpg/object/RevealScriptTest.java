package org.sarge.textrpg.object;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;

public class RevealScriptTest {
	private Script reveal;
	private Thing obj;
	private Actor actor;
	
	@Before
	public void before() {
		obj = new ObjectDescriptor.Builder("object").visibility(Percentile.HALF).build().create();
		reveal = new RevealScript(obj, "key");
		actor = mock(Actor.class);
	}
	
	@Test
	public void execute() {
		reveal.execute(actor);
		verify(actor).alert(new RevealNotification("key", obj));
	}
	
	@Test
	public void executeAlreadyKnown() {
		when(actor.perceives(obj)).thenReturn(true);
		reveal.execute(actor);
		verify(actor).perceives(obj);
		verifyNoMoreInteractions(actor);
	}
}
