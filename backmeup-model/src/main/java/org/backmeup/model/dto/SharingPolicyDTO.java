package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("unused")
public class SharingPolicyDTO {

    public enum SharingPolicyTypeEntryDTO {
        Document, DocumentGroup, Backup, AllFromNow, AllInklOld
    }

    private Long withUserId;
    private SharingPolicyTypeEntryDTO policyType;
    private String policyValue;

    public Long getWithUserId() {
        return this.withUserId;
    }

    public void setWithUserId(Long withUserId) {
        this.withUserId = withUserId;
    }

    public SharingPolicyTypeEntryDTO getPolicyType() {
        return this.policyType;
    }

    public void setPolicyType(SharingPolicyTypeEntryDTO policyType) {
        this.policyType = policyType;
    }

    public String getPolicyValue() {
        return this.policyValue;
    }

    public void setPolicyValue(String policyValue) {
        this.policyValue = policyValue;
    }

}
