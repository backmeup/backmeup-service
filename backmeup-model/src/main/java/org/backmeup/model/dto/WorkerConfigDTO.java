package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkerConfigDTO {
    public enum DistributionMechanism {
        QUEUE;
    }
    private DistributionMechanism distributionMechanism;
    private String connectionInfo;
    private String backupNameTemplate;
    
    public WorkerConfigDTO() {
        
    }
    
    public DistributionMechanism getDistributionMechanism() {
        return distributionMechanism;
    }
    
    public void setDistributionMechanism(DistributionMechanism distributionMechanism) {
        this.distributionMechanism = distributionMechanism;
    }
    
    public String getConnectionInfo() {
        return connectionInfo;
    }
    
    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getBackupNameTemplate() {
        return backupNameTemplate;
    }

    public void setBackupNameTemplate(String backupNameTemplate) {
        this.backupNameTemplate = backupNameTemplate;
    }
}
