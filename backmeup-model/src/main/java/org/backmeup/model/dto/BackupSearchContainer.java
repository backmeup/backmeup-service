package org.backmeup.model.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BackupSearchContainer {

    private long searchId;

    public BackupSearchContainer() {
    }

    public BackupSearchContainer(long searchId) {
        this.searchId = searchId;
    }

    public long getSearchId() {
        return searchId;
    }

    public void setSearchId(long searchId) {
        this.searchId = searchId;
    }
}
