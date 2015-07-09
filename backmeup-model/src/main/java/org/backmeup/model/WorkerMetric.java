package org.backmeup.model;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class WorkerMetric {
    @Id
    @GeneratedValue
    private Long id;
    
    private String workerId;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    private String metric;
    
    private double value;

    public WorkerMetric() {
    
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    
    public Date getTimestamp() {
        if (this.timestamp == null) {
            return null;
        }
        return (Date) timestamp.clone();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = (Date) timestamp.clone();
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Metric: timestamp=" + timestamp + ", value=" + value;
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
        if (!(other instanceof WorkerMetric)) {
            return false;
        }
        WorkerMetric entity = (WorkerMetric) other;
        if (id == null || entity.getId() == null) {
            return false;
        }
        return id.equals(entity.getId());
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        
        int result = id.hashCode();
        result = 31 * result + (int) (timestamp.getTime() ^ (timestamp.getTime() >>> 32));
        result = (int)(31 * result + value % 71);
        return result;
    }
}

