package org.sarge.textrpg.runner;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RunnerAdapterTest {
    private RunnerAdapter runner;
    private int before;

    @Before
    public void before() {
        runner = new RunnerAdapter(() -> {});
        before = count();
    }

    @After
    public void after() {
        if(runner.isRunning()) {
            runner.stop();
        }
    }

    private static int count() {
        return Thread.currentThread().getThreadGroup().activeCount();
    }

    @Test
    public void start() {
        runner.start();
        assertEquals(true, runner.isRunning());
        assertEquals(before + 1, count());
    }

    @Test
    public void stop() {
        runner.start();
        runner.stop();
        assertEquals(false, runner.isRunning());
    }
}
