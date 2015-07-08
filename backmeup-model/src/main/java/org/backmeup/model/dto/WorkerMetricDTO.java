package org.backmeup.model.dto;


import java.util.Date;

import javax.persistence.Entity;

@Entity
public class WorkerMetricDTO {
    
    private Date timestamp;
    
    private String metric;
    
    private double value;

    public WorkerMetricDTO() {
    
    }
    
    public WorkerMetricDTO(Date timestamp, String metric, double value) {
        super();
        this.timestamp = timestamp;
        this.metric = metric;
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WorkerMetricDTO)) {
            return false;
        }
        WorkerMetricDTO m = (WorkerMetricDTO) obj;
        return metric.equals(m.getMetric())
            && timestamp == m.getTimestamp()
            && (Double.compare(value, m.getValue()) == 0);
    }

    @Override
    public int hashCode() {
        int result = metric.hashCode();
        result = 31 * result + (int) (timestamp.getTime() ^ (timestamp.getTime() >>> 32));
        result = (int)(31 * result + value % 71);
        return result;
    }
}

