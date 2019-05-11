package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.util.EnumerationTestHelper;

public class SlotTest {
	@ParameterizedTest
	@EnumSource(value=Slot.class, names={"BELT", "KEYRING"})
	public void isContainer(Slot slot) {
		assertEquals(true, slot.isContainer());
	}

	@ParameterizedTest
	@EnumSource(value=Slot.class, mode=EnumSource.Mode.EXCLUDE, names={"BELT", "KEYRING"})
	public void isContainerNotContainer(Slot slot) {
		assertEquals(false, slot.isContainer());
	}

	@ParameterizedTest
	@EnumSource(value=Slot.class, names={"MAIN", "OFF"})
	public void isHanded(Slot slot) {
		assertEquals(true, slot.isHanded());
	}

	@ParameterizedTest
	@EnumSource(value=Slot.class, mode=EnumSource.Mode.EXCLUDE, names={"MAIN", "OFF"})
	public void isHandedNotHandedSlot(Slot slot) {
		assertEquals(false, slot.isHanded());
	}

	@Test
	public void verb() {
		final EnumerationTestHelper<Slot, String> test = new EnumerationTestHelper<>(Slot.class, Slot::verb);
		test.assertEquals("put", Slot.BACK, Slot.SHOULDER, Slot.POCKET, Slot.RING);
		test.assertEquals("wield", Slot.MAIN);
		test.assertEquals("fasten", Slot.BELT, Slot.KEYRING);
		test.assertEquals("wear");
	}

	@Test
	public void placement() {
		final EnumerationTestHelper<Slot, String> test = new EnumerationTestHelper<>(Slot.class, Slot::placement);
		test.assertEquals("across", Slot.BACK);
		test.assertEquals("around", Slot.NECK);
		test.assertEquals("about", Slot.WAIST, Slot.BODY);
		test.assertEquals("over", Slot.SHOULDER);
		test.assertEquals("in", Slot.POCKET);
		test.assertEquals("as", Slot.BELT);
		test.assertEquals("on");
	}
}
