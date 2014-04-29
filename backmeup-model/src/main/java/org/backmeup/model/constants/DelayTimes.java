package org.backmeup.model.constants;

public class DelayTimes {
    // TODO PK type safety/duplication - create enum with times and timeExpressions and conversions in multiple places
    // constants for scheduler in milliseconds
    public static final long DELAY_REALTIME = 1 * 1000;
    public static final long DELAY_DAILY = 24 * 60 * 60 * 1000;
    public static final long DELAY_WEEKLY = 24 * 60 * 60 * 1000 * 7;
    public static final long DELAY_MONTHLY = (long) (24 * 60 * 60 * 1000 * 365.242199 / 12.0);
    public static final long DELAY_YEARLY = (long) (24 * 60 * 60 * 1000 * 365.242199);
}
