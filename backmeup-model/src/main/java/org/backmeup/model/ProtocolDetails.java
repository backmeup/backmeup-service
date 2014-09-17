package org.backmeup.model;

import java.util.List;

import org.backmeup.index.model.FileInfo;

/**
 * This class contains information about a certain
 * file. E.g. on which sinks is the file, when
 * has the file been uploaded. 
 * 
 * @author fschoeppl
 */
public class ProtocolDetails {
    private FileInfo fileInfo;
    private List<Sink> sinks;
    private List<FileInfo> similar;

    public ProtocolDetails() {
    }

    public ProtocolDetails(FileInfo fileInfo, List<Sink> sinks, List<FileInfo> similar) {
        this.fileInfo = fileInfo;
        this.sinks = sinks;
        this.similar = similar;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public List<Sink> getSinks() {
        return sinks;
    }

    public void setSinks(List<Sink> sinks) {
        this.sinks = sinks;
    }

    public List<FileInfo> getSimilar() {
        return similar;
    }

    public void setSimilar(List<FileInfo> similar) {
        this.similar = similar;
    }

    public static class Sink {
        private String title;
        private String timeStamp;
        private String path;

        public Sink() {
        }

        public Sink(String title, String timeStamp, String path) {
            this.title = title;
            this.timeStamp = timeStamp;
            this.path = path;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
