package kandrac.xyz.library.test;

import org.junit.Assert;
import org.junit.Test;

import xyz.kandrac.library.utils.DateUtils;

/**
 * Created by kandrac on 14/12/15.
 */
public class DateUtilsTest {

    @Test
    public void testZeroTime() {
        Assert.assertEquals(0, DateUtils.getZeroTime(2345));
        Assert.assertEquals(86400000, DateUtils.getZeroTime(123000000));
        Assert.assertEquals(172800000, DateUtils.getZeroTime(234567890));
        Assert.assertEquals(345600000, DateUtils.getZeroTime(380067890));
    }
}
