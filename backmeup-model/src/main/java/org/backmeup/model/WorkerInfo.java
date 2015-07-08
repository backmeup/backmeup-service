package org.backmeup.model;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.backmeup.model.constants.WorkerState;

@Entity
public class WorkerInfo {

    @Id
    private String workerId;
    
    private String workerName;
    
    private String osName;
    
    private String osVersion;
    
    private String osArchitecture;
    
    private int totalCPUCores;
    
    private long totalMemory;
    
    private long totalSpace;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastHeartbeat;
    
    @Enumerated(EnumType.STRING)
    private WorkerState state;
    
    public WorkerInfo() {
    
    }
    
    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArchitecture() {
        return osArchitecture;
    }

    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public int getTotalCPUCores() {
        return totalCPUCores;
    }

    public void setTotalCPUCores(int totalCPUCores) {
        this.totalCPUCores = totalCPUCores;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public Date getLastHeartbeat() {
        if (this.lastHeartbeat == null) {
            return null;
        }
        return (Date) lastHeartbeat.clone();
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = (Date) lastHeartbeat.clone();
    }
    
    public void setLastHeartbeatNow() {
        setLastHeartbeat(new Date());
    }

    public WorkerState getState() {
        return state;
    }

    public void setState(WorkerState state) {
        this.state = state;
    }
    
    @Override
    public String toString() {
        return String.format("%s: id=%s Name=%s", "Worker", workerId, workerName);
    }

    /**
     * Attempt to establish identity based on id if both exist. 
     * If either id does not exist use Object.equals().
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof BackupJob)) {
            return false;
        }
        WorkerInfo entity = (WorkerInfo) other;
        if (workerId == null || entity.getWorkerId() == null) {
            return false;
        }
        return workerId.equals(entity.getWorkerId());
    }

    /**
     * Use ID if it exists to establish hash code, otherwise fall back to
     * Object.hashCode(). 
     */
    @Override
    public int hashCode() {
        if (workerId == null) {
            return super.hashCode();
        }
        return 101 * workerId.hashCode();
    }
}

