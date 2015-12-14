package xyz.kandrac.library.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import xyz.kandrac.library.utils.DateUtils;

public class DateUtilsTest {

    @Test
    public void testFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();

        Assert.assertEquals("1. 1. 1970", DateUtils.dateFormat.format(calendar.getTimeInMillis()));

        calendar.set(Calendar.YEAR, 2000);
        Assert.assertEquals("1. 1. 2000", DateUtils.dateFormat.format(calendar.getTimeInMillis()));
        Assert.assertEquals("1. 1. 2000 0:00:00", DateUtils.fullDateFormat.format(calendar.getTimeInMillis()));

        calendar.set(Calendar.MONTH, Calendar.MARCH);
        Assert.assertEquals("1. 3. 2000", DateUtils.dateFormat.format(calendar.getTimeInMillis()));
        Assert.assertEquals("1. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(calendar.getTimeInMillis()));

        calendar.set(Calendar.DAY_OF_MONTH, 15);
        Assert.assertEquals("15. 3. 2000", DateUtils.dateFormat.format(calendar.getTimeInMillis()));
        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(calendar.getTimeInMillis()));
    }

    @Test
    public void testZeroTime() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);

        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH, 15);

        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(DateUtils.getZeroTime(calendar.getTimeInMillis())));

        calendar.set(Calendar.HOUR_OF_DAY, 20);
        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(DateUtils.getZeroTime(calendar.getTimeInMillis())));

        calendar.set(Calendar.MINUTE, 20);
        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(DateUtils.getZeroTime(calendar.getTimeInMillis())));

        calendar.set(Calendar.SECOND, 20);
        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(DateUtils.getZeroTime(calendar.getTimeInMillis())));

        calendar.set(Calendar.MILLISECOND, 20);
        Assert.assertEquals("15. 3. 2000 0:00:00", DateUtils.fullDateFormat.format(DateUtils.getZeroTime(calendar.getTimeInMillis())));
    }

    @Test
    public void testTimeAt() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);

        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH, 15);
        calendar.set(Calendar.HOUR_OF_DAY, 15);

        Assert.assertEquals("15. 3. 2000 18:00:00", DateUtils.fullDateFormat.format(DateUtils.getHourTime(calendar.getTimeInMillis(), 18)));
        Assert.assertEquals("15. 3. 2000 12:00:00", DateUtils.fullDateFormat.format(DateUtils.getHourTime(calendar.getTimeInMillis(), 12)));
        Assert.assertEquals("15. 3. 2000 2:00:00", DateUtils.fullDateFormat.format(DateUtils.getHourTime(calendar.getTimeInMillis(), 2)));
    }
}
