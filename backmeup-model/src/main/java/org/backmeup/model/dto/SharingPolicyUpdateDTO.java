package org.backmeup.model.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SharingPolicyUpdateDTO {

    private Long policyID;
    private String name;
    private String description;
    private Date lifespanstart;
    private Date lifespanend;

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

    public Long getPolicyID() {
        return this.policyID;
    }
}
