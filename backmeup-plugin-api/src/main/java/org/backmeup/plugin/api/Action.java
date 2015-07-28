package org.backmeup.plugin.api;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public interface Action {
    /**
     * Execute an action on all elements passed in the storage.
     * 
     * @param profile
     * @param storage
     * @param progressor
     * @throws DatasinkException,
     *             StorageException
     */
    void doAction(PluginProfileDTO profile, Storage storage, Progressable progressor) throws ActionException, StorageException;
}
