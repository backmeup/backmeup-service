package org.backmeup.plugin.api;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for datasources following a filesystem-like paradigm.
 * These datasources are arranged in a hierarchical structure of folders and files. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class FilesystemLikeDatasource implements Datasource { 

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemLikeDatasource.class);

    @Override
    public void downloadAll(PluginProfileDTO profile, Storage storage, Progressable progressor) throws StorageException {
        List<FilesystemURI> files = list(profile);
        for (int i=0; i < files.size(); i++) {
            FilesystemURI uri = files.get(i);
            download(profile, uri, storage, progressor);
        }
    }

    private void download(PluginProfileDTO profile, FilesystemURI uri, Storage storage, Progressable progressor) throws StorageException {
        MetainfoContainer metainfo = uri.getMetainfoContainer();
        if (uri.isDirectory()) {
            LOGGER.info("Downloading contents of directory " + uri);
            for (FilesystemURI child : list(profile, uri)) {
                download(profile, child, storage, progressor);
            }
        } else {
            LOGGER.info("Downloading file " + uri);
            progressor.progress(String.format("Downloading file %s ...", uri.toString()));
            InputStream is = getFile(profile, uri);
            if (is == null) {
                LOGGER.warn("Got a null input stream for " + uri.getUri().getPath().toString());
                progressor.progress(String.format("Downloading file %s failed!", uri.toString()));
            } else {
                URI destination = uri.getMappedUri();
                if (destination == null) {
                    destination = uri.getUri();
                }
                storage.addFile(is, destination.getPath().toString(), metainfo);
            }
        }
    }

    public List<FilesystemURI> list(PluginProfileDTO profile) {
        return list(profile, null);
    }

    public abstract List<FilesystemURI> list(PluginProfileDTO profile, FilesystemURI uri);

    public abstract InputStream getFile(PluginProfileDTO profile, FilesystemURI uri);

}
