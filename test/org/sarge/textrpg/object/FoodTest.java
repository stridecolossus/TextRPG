package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.object.Food.Descriptor;
import org.sarge.textrpg.object.Food.Type;

public class FoodTest extends ActionTest {
	private Food food;
	
	@Before
	public void before() throws ActionException {
		food = new Food(new Descriptor(new ObjectDescriptor("food"), Type.RAW, 1, 2));
		food.setParent(actor);
	}
	
	@After
	public void after() {
		Food.QUEUE.reset();
	}
	
	@Test
	public void constructor() {
		assertEquals("raw.food", food.getName());
		assertEquals(false, food.isEdible());
		assertEquals(false, food.isDead());
		assertEquals(1, Food.QUEUE.stream().count());
	}
	
	@Test
	public void consume() throws ActionException {
		food.cook();
		food.consume();
		assertEquals(true, food.isDead());
		assertEquals(2, Food.QUEUE.stream().count());
	}

	@Test
	public void consumeNotCooked() throws ActionException {
		expect("consume.not.cooked");
		food.consume();
	}
	
	@Test
	public void cook() throws ActionException {
		food.verifyCook();
		food.cook();
		assertEquals("food", food.getName());
		assertEquals(true, food.isEdible());
		assertEquals(false, food.isDead());
		assertEquals(2, Food.QUEUE.stream().count());
	}

	@Test
	public void cookCannotCook() throws ActionException {
		food = new Food(new Descriptor(new ObjectDescriptor("food"), Type.FOOD, 1, 2));
		expect("cook.cannot.cook");
		food.verifyCook();
	}
	
	@Test
	public void cookAlreadyCooked() throws ActionException {
		food.cook();
		expect("cook.already.cooked");
		food.verifyCook();
	}
	
	@Test
	public void decay() {
		Food.QUEUE.update(2);
		assertEquals(true, food.isDead());
		verify(actor).alert(new Message("food.decayed", food));
		assertEquals(0, Food.QUEUE.stream().count());
	}
}
