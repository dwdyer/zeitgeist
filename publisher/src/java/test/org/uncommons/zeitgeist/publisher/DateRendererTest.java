package org.uncommons.zeitgeist.publisher;

import java.util.Date;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link DateRenderer} class.
 * @author Daniel Dyer
 */
public class DateRendererTest
{
    @Test
    public void testFormatSupplied()
    {
        Date date = new Date(0);
        String string = new DateRenderer().toString(date, "dd/MM/yyyy");
        assert "01/01/1970".equals(string) : "Incorrect format: " + string;
    }
}
