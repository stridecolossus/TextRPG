package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Parent;

public class PortalScriptTest extends ActionTest {
	private PortalScript script;
	private WorldObject portal;
	
	@Before
	public void before() {
		portal = new Portal(new Portal.Descriptor(new ObjectDescriptor.Builder("portal").reset(1).build(), Openable.UNLOCKABLE), mock(Parent.class));
		script = new PortalScript(portal, Operation.OPEN);
	}
	
	@Test
	public void execute() {
		script.execute(actor);
		assertEquals(true, portal.getOpenableModel().get().isOpen());
	}
}
