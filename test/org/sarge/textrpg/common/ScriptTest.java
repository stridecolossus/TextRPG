package org.sarge.textrpg.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class ScriptTest extends ActionTest {
	private Script script;

	@Before
	public void before() {
		script = mock(Script.class);
	}
	
	@Test
	public void message() {
		final Script script = Script.message("text");
		script.execute(actor);
		verify(actor).alert(new Message("text"));
	}
	
	@Test
	public void compound() {
		final Script compound = Script.compound(Collections.singletonList(script));
		compound.execute(actor);
		verify(script).execute(actor);
	}
	
	@Test
	public void conditionalTrue() {
		final Script conditional = Script.condition(Condition.TRUE, script, null);
		conditional.execute(actor);
		verify(script).execute(actor);
	}
	
	@Test
	public void conditionalFalse() {
		final Script conditional = Script.condition(Condition.invert(Condition.TRUE), null, script);
		conditional.execute(actor);
		verify(script).execute(actor);
	}
}
