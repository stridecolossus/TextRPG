package org.sarge.textrpg.loader;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Test;

public class LoaderHelperTest {
    @Test
    public void parseDurationColonDelimited() {
        // Hours/mins/secs
        assertEquals(Duration.parse("PT1H2M3S"), LoaderHelper.parseDuration("01:02:03"));
        assertEquals(Duration.parse("PT1H2M3S"), LoaderHelper.parseDuration("1:2:3"));

        // Mins/secs
        assertEquals(Duration.parse("PT2M3S"), LoaderHelper.parseDuration("02:03"));

        // Seconds only
        assertEquals(Duration.parse("PT3S"), LoaderHelper.parseDuration("3"));
    }

    @Test
    public void parseDurationColonISO() {
        final String str = "P1DT2H3M4S";
        assertEquals(Duration.parse(str), LoaderHelper.parseDuration(str));
    }

    @Test(expected = NumberFormatException.class)
    public void parseDurationEmpty() {
        LoaderHelper.parseDuration("");
    }

    @Test(expected = NumberFormatException.class)
    public void parseDurationInvalid() {
        LoaderHelper.parseDuration("cobblers");
    }
}
