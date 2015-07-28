package org.backmeup.plugin.api;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * Interface for all datasource implementations
 * 
 */
public interface Datasource {

    /**
     * Download the entire content of this datasource to the provided data
     * storage.
     * 
     * @param profile
     * @param storage
     * @param progressor
     * @throws DatasinkException,
     *             StorageException
     */
    void downloadAll(PluginProfileDTO profile, Storage storage, Progressable progressor)
            throws DatasourceException, StorageException;
}
