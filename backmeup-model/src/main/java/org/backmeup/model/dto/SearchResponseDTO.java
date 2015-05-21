package org.backmeup.model.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SearchResponseDTO {
    private List<SearchEntryDTO> files;
    private List<CountedEntryDTO> bySource;
    private List<CountedEntryDTO> byType;
    private List<CountedEntryDTO> byJob;
    private List<CountedEntryDTO> byOwner;
    private List<CountedEntryDTO> byTag;
    private String searchQuery;
    private int progress;

    public List<CountedEntryDTO> getBySource() {
        return this.bySource;
    }

    public void setBySource(List<CountedEntryDTO> bySource) {
        this.bySource = bySource;
    }

    public List<CountedEntryDTO> getByType() {
        return this.byType;
    }

    public void setByType(List<CountedEntryDTO> byType) {
        this.byType = byType;
    }

    public List<CountedEntryDTO> getByJob() {
        return this.byJob;
    }

    public void setByJob(List<CountedEntryDTO> byJob) {
        this.byJob = byJob;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public List<SearchEntryDTO> getFiles() {
        return this.files;
    }

    public void setFiles(List<SearchEntryDTO> files) {
        this.files = files;
    }

    public List<CountedEntryDTO> getByOwner() {
        return this.byOwner;
    }

    public void setByOwner(List<CountedEntryDTO> byOwner) {
        this.byOwner = byOwner;
    }

    public List<CountedEntryDTO> getByTag() {
        return this.byTag;
    }

    public void setByTag(List<CountedEntryDTO> byTag) {
        this.byTag = byTag;
    }

}
