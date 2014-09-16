package org.backmeup.model.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SearchResponseDTO {
    private List<SearchEntryDTO> files;
    private List<CountedEntryDTO> bySource;
    private List<CountedEntryDTO> byType;
    private List<CountedEntryDTO> byJob;
    private String searchQuery;
    private int progress;

    public List<CountedEntryDTO> getBySource() {
        return bySource;
    }

    public void setBySource(List<CountedEntryDTO> bySource) {
        this.bySource = bySource;
    }

    public List<CountedEntryDTO> getByType() {
        return byType;
    }

    public void setByType(List<CountedEntryDTO> byType) {
        this.byType = byType;
    }

    public List<CountedEntryDTO> getByJob() {
        return byJob;
    }

    public void setByJob(List<CountedEntryDTO> byJob) {
        this.byJob = byJob;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public List<SearchEntryDTO> getFiles() {
        return files;
    }

    public void setFiles(List<SearchEntryDTO> files) {
        this.files = files;
    }

}
