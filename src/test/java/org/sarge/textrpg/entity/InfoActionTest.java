package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.entity.InfoAction.Operation;
import org.sarge.textrpg.runner.ConsoleRunner.Device;
import org.sarge.textrpg.util.MutableIntegerMap;

public class InfoActionTest extends ActionTest {
	private Player player;
	private Race race;
	
	@Before
	public void before() {
		final MutableIntegerMap<Attribute> attrs = new MutableIntegerMap<>(Attribute.class);
		race = new Race.Builder("race").build();
		player = new Player("player", race, attrs, Gender.FEMALE, Alignment.EVIL, mock(Device.class));
		player.setParentAncestor(loc);
	}
	
	@Test
	public void info() throws ActionException {
		final InfoAction action = new InfoAction(Operation.INFO);
		action.info(player);
	}
}
