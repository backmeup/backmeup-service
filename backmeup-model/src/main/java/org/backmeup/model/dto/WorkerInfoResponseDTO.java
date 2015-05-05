package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkerInfoResponseDTO {
    public enum DistributionMechnaism {
        QUEUE;
    }
    private DistributionMechnaism distributionMechanism;
    private String connectionInfo;
    
    public WorkerInfoResponseDTO() {
        
    }
    
    public WorkerInfoResponseDTO(DistributionMechnaism mechanism,String connInfo) {
        this.distributionMechanism = mechanism;
        this.connectionInfo = connInfo;
    }

    public DistributionMechnaism getDistributionMechanism() {
        return distributionMechanism;
    }
    
    public void setDistributionMechanism(DistributionMechnaism distributionMechanism) {
        this.distributionMechanism = distributionMechanism;
    }
    
    public String getConnectionInfo() {
        return connectionInfo;
    }
    
    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}
