package org.backmeup.model.constants;

public enum WorkerState {
    // Not connected to dependent services
    OFFLINE,

    // No jobs to execute
    IDLE,

    // Jobs are currently running on worker
    BUSY
}
