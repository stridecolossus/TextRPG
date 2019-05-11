package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class RiddleModelTest extends ActionTestBase {
	private Riddle.Model model;
	private Entity other;
	private Riddle riddle;

	@BeforeEach
	public void before() {
		other = mock(Entity.class);
		model = new Riddle.Model(actor, other, 1);
		riddle = new Riddle("riddle", "riddle.answer");
	}

	@Test
	public void constructor() {
		assertEquals(0, model.round());
		assertEquals(1, model.limit());
		assertEquals(Riddle.Model.State.OPEN, model.state());
		assertEquals(other, model.who());
	}

	@Test
	public void accept() throws ActionException {
		model.accept();
		assertEquals(0, model.round());
		assertEquals(Riddle.Model.State.PENDING, model.state());
		assertEquals(actor, model.who());
	}

	@Test
	public void acceptAlreadyAccepted() throws ActionException {
		model.accept();
		TestHelper.expect("accept.already.accepted", () -> model.accept());
	}

	@Test
	public void riddle() throws ActionException {
		model.accept();
		model.riddle(riddle);
		assertEquals(0, model.round());
		assertEquals(Riddle.Model.State.WAITING, model.state());
		assertEquals(other, model.who());
	}

	@Test
	public void riddleNotPending() {
		TestHelper.expect("riddle.not.pending", () -> model.riddle(riddle));
	}

	@Test
	public void riddleDuplicate() throws ActionException {
		model.accept();
		model.riddle(riddle);
		model.answer(riddle.answer());
		TestHelper.expect("riddle.already.used", () -> model.riddle(riddle));
	}

	@Test
	public void answer() throws ActionException {
		model.accept();
		model.riddle(riddle);
		final var result = model.answer(riddle.answer());
		assertEquals(Riddle.Model.Result.ACTIVE, result);
		assertEquals(Riddle.Model.State.PENDING, model.state());
		assertEquals(other, model.who());
	}

	@Test
	public void draw() throws ActionException {
		final Riddle another = new Riddle("other", "other.answer");
		model.accept();
		model.riddle(riddle);
		model.answer(riddle.answer());
		model.riddle(another);
		final Riddle.Model.Result result = model.answer(another.answer());
		assertEquals(Riddle.Model.Result.DRAWN, result);
		assertEquals(Riddle.Model.State.FINISHED, model.state());
		assertEquals(1, model.round());
	}

	@Test
	public void answerNotWaiting() throws ActionException {
		model.accept();
		TestHelper.expect("riddle.not.waiting", () -> model.answer(riddle.answer()));
	}

	@Test
	public void answerIncorrect() throws ActionException {
		final Riddle another = new Riddle("other", "other.answer");
		model.accept();
		model.riddle(riddle);
		final Riddle.Model.Result result = model.answer(another.answer());
		assertEquals(Riddle.Model.Result.FIRST, result);
		assertEquals(Riddle.Model.State.FINISHED, model.state());
	}

	@Test
	public void interrupt() throws ActionException {
		model.accept();
		final var result = model.interrupt();
		assertEquals(Riddle.Model.Result.SECOND, result);
		assertEquals(Riddle.Model.State.FINISHED, model.state());
	}

	@Test
	public void interruptNotAccepted() {
		final var result = model.interrupt();
		assertEquals(Riddle.Model.Result.DRAWN, result);
		assertEquals(Riddle.Model.State.FINISHED, model.state());
	}

	@Test
	public void interruptAlreadyFinished() {
		model.interrupt();
		assertThrows(IllegalStateException.class, () -> model.interrupt());
	}
}
