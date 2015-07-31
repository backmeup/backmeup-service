package org.backmeup.model.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SharingPolicyDTO {

    public enum SharingPolicyTypeEntryDTO {
        Document, DocumentGroup, Backup, AllFromNow, AllInklOld, TaggedCollection
    }

    private Long withUserId;
    private SharingPolicyTypeEntryDTO policyType;
    private String policyValue;
    private String name;
    private String description;
    private Date lifespanstart;
    private Date lifespanend;

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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLifespanstart() {
        return this.lifespanstart;
    }

    public void setLifespanstart(Date lifespanstart) {
        this.lifespanstart = lifespanstart;
    }

    public Date getLifespanend() {
        return this.lifespanend;
    }

    public void setLifespanend(Date lifespanend) {
        this.lifespanend = lifespanend;
    }
}
