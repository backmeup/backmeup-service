package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Map;

import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * An abstract base class for all datasource implementations
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>, w.eibner
 */
public interface Datasource {
    /**
     * Downloads the entire content of this datasource to the provided
     * data storage.
     * @param storage the datastorage
     */
    void downloadAll(Map<String, String> authData, Map<String, String> properties, List<String> options, 
            Storage storage, Progressable progressor) throws DatasourceException, StorageException;
}
