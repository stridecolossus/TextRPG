package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class PropertyTest {
	private byte props;

	@BeforeEach
	public void before() {
		props = Property.toBitField(Set.of(Property.FISH, Property.WATER));
	}

	@ParameterizedTest
	@EnumSource(value=Property.class, mode=EnumSource.Mode.EXCLUDE, names={"FISH", "WATER"})
	public void isProperty(Property p) {
		assertEquals(false, p.isProperty(props));
	}

	@ParameterizedTest
	@EnumSource(value=Property.class, names={"FISH", "WATER"})
	public void isNotProperty(Property p) {
		assertEquals(true, p.isProperty(props));
	}

	@ParameterizedTest
	@EnumSource(value=Property.class, names={"FISH", "WATER", "BUSY"})
	public void isAreaProperty(Property p) {
		assertEquals(true, p.isAreaProperty());
	}
}
