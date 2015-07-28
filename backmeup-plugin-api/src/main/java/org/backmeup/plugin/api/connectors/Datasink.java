package org.backmeup.plugin.api.connectors;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * 
 * The Datasink interface is the contract for an upload of files to a certain
 * datasink.
 * 
 */
public interface Datasink {

    /**
     * Upload the entire content of this datasink from the provided data
     * storage.
     * 
     * @param profile
     * @param storage
     * @param progressor
     * @return relative location for this backup upload without location prefix
     *         and the entire path up to the files e.g. if the location is
     *         http://localhost:8080/backmeup-storage-service/
     *         BMU_filegenerator_492_22_01_2015_21_14/file0.txt then
     *         BMU_filegenerator_492_22_01_2015_21_14 is returned value for this
     *         String
     * @throws DatasinkException,
     *             StorageException
     */
    String upload(PluginProfileDTO profile, Storage storage, Progressable progressor)
            throws DatasinkException, StorageException;
}
