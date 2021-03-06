package org.backmeup.plugin.api;

import java.net.URI;

public class FilesystemURI {
    private final URI uri;
    private URI mappedUri;
    private final boolean isDirectory;
    private final MetainfoContainer metainfoContainer = new MetainfoContainer();

    public FilesystemURI(URI uri, boolean isDirectory) {
        this.uri = uri;
        this.isDirectory = isDirectory;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    public URI getMappedUri() {
        return mappedUri;
    }

    public void setMappedUri(URI mappedUri) {
        this.mappedUri = mappedUri;
    }

    public void addMetainfo(Metainfo metainfo) {    
        this.metainfoContainer.addMetainfo(metainfo);
    }

    public MetainfoContainer getMetainfoContainer() {
        return this.metainfoContainer;
    }
}
