package org.backmeup.model;

import java.util.Set;

import org.backmeup.index.model.FileItem;

public class StatusWithFiles {

    private final Status status;

    private Set<FileItem> files;

    public StatusWithFiles(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Set<FileItem> getFiles() {
        return files;
    }

    public void setFiles(Set<FileItem> files) {
        this.files = files;
    }

}
