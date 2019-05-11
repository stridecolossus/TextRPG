package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.util.ValueModifier;

public class TransactionTest {
	private Transaction tx;
	private ValueModifier mod;

	@BeforeEach
	public void before() {
		mod = mock(ValueModifier.class);
		tx = new Transaction(mod, 1, "message");
	}

	@Test
	public void complete() throws ActionException {
		when(mod.get()).thenReturn(1);
		assertEquals(true, tx.isValid());
		tx.check();
		tx.complete();
		verify(mod).modify(-1);
	}

	@Test
	public void completeAlreadyCompleted() throws ActionException {
		when(mod.get()).thenReturn(1);
		tx.complete();
		assertThrows(IllegalStateException.class, tx::complete);
	}

	@Test
	public void invalid() throws ActionException {
		assertEquals(false, tx.isValid());
		TestHelper.expect("message", tx::check);
		assertThrows(IllegalStateException.class, tx::complete);
	}
}
