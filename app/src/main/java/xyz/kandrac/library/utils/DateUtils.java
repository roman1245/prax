package xyz.kandrac.library.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utils used to work with dates, including
 * <ul>
 * <li>static point to obtain default date formats</li>
 * <li>computational methods for dates</li>
 * </ul>
 * Please see static methods inside the class
 * <p>
 * Created by kandrac on 13/12/15.
 */
@SuppressWarnings("unused")
public final class DateUtils {

    /**
     * Default constructor is private to disable instantiation
     */
    private DateUtils() {

    }

    /**
     * Default formatting for date that in result contains these fields:
     * <ol>
     *     <li>Day</li>
     *     <li>Month</li>
     *     <li>Year</li>
     * </ol>
     */
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("d. M. yyyy", Locale.getDefault());


    /**
     * Default formatting for date that in result contains these fields:
     * <ol>
     *     <li>Seconds</li>
     *     <li>Minutes</li>
     *     <li>Hours</li>
     *     <li>Day</li>
     *     <li>Month</li>
     *     <li>Year</li>
     * </ol>
     */
    public static SimpleDateFormat fullDateFormat = new SimpleDateFormat("d. M. yyyy H:mm:ss", Locale.getDefault());

    /**
     * @return 00:00:00 time for current date
     */
    public static long getTodayZeroTime() {
        return getZeroTime(System.currentTimeMillis());
    }

    /**
     * @param hour to be get for current date
     * @return 00:hour:00 time for current date
     */
    public static long getTodayHourTime(int hour) {
        return getHourTime(System.currentTimeMillis(), hour);
    }

    /**
     * @param timeInDay date to get zero time from
     * @return 00:00:00 time for specified date
     */
    public static long getZeroTime(long timeInDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInDay);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * @param timeInDay any date to get hour time from
     * @param hour      to be get
     * @return 00:hour:00 time for specified date
     */
    public static long getHourTime(long timeInDay, int hour) {
        return getZeroTime(timeInDay) + TimeUnit.HOURS.toMillis(hour);
    }
}
