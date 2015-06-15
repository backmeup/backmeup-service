package org.backmeup.model.constants;

public enum JobFrequency {
    ONCE    (1 * 1000),
    DAILY   (24 * 60 * 60 * 1000),
    WEEKLY  (24 * 60 * 60 * 1000 * 7),
    MONTHLY ((long) (24 * 60 * 60 * 1000 * 365.242199 / 12.0)),
    YEARLY  ((long) (24 * 60 * 60 * 1000 * 365.242199));

    // Delay time for scheduler in milliseconds
    private final long delayTime;

    private JobFrequency(long delayTime) {
        this.delayTime = delayTime;
    }

    public long getDelayTime() {
        return this.delayTime;
    }
}
