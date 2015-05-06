package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("unused")
public class SharingPolicyHandshakeDTO {

    private Long policyID;
    private boolean approve;

    public Long getPolicyID() {
        return this.policyID;
    }

    public void setPolicyID(Long policyID) {
        this.policyID = policyID;
    }

    public boolean getApprove() {
        return this.approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

}
