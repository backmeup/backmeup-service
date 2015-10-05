package org.backmeup.model.exceptions;

/**
 * An operation might fail because the given worker is unknown to the system.
 * This exception should be thrown then.
 *
 */

public class UnknownWorkerException extends BackMeUpException {
    private static final long serialVersionUID = 1L;
    private static final String UNKNOWN_WORKER = "Unknown Worker";

    private final String workerId;

    public UnknownWorkerException(String workerId) {
        super(UNKNOWN_WORKER);
        this.workerId = workerId;
    }

    public String getWorkerId() {
        return this.workerId;
    }
}
