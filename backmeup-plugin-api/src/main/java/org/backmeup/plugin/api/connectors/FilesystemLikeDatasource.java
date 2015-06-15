package org.backmeup.plugin.api.connectors;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.backmeup.plugin.api.MetainfoContainer;
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
    public void downloadAll(Map<String, String> authData, Map<String, String> properties, List<String> options, Storage storage, Progressable progressor) throws StorageException {
        List<FilesystemURI> files = list(authData, options);
        for (int i=0; i < files.size(); i++) {
            FilesystemURI uri = files.get(i);			
            download(authData, properties, options, uri, storage, progressor);			
        }
    }

    private void download(Map<String, String> authData, Map<String, String> properties, List<String> options, FilesystemURI uri, Storage storage, Progressable progressor) throws StorageException {
        MetainfoContainer metainfo = uri.getMetainfoContainer();	  
        if (uri.isDirectory()) {
            LOGGER.info("Downloading contents of directory " + uri);
            for (FilesystemURI child : list(authData, options, uri)) {
                download(authData, properties, options, child, storage, progressor);
            }
        } else {
            LOGGER.info("Downloading file " + uri);
            progressor.progress(String.format("Downloading file %s ...", uri.toString()));
            InputStream is = getFile(authData, options, uri);
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

    public List<FilesystemURI> list(Map<String, String> accessData, List<String> options) {
        return list(accessData, options, null);
    }

    public abstract List<FilesystemURI> list(Map<String, String> accessData, List<String> options, FilesystemURI uri);

    public abstract InputStream getFile(Map<String, String> accessData, List<String> options, FilesystemURI uri);

}
