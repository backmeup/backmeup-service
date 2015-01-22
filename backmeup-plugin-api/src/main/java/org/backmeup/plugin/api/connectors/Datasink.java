package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * 
 * The Datasink interface is the contract for an upload of files to a certain datasink.
 * 
 * 
 * @author fschoeppl
 *
 */
public interface Datasink {

    /**
     * 
     * @param accessData
     * @param properties
     * @param options
     * @param storage
     * @param progressor
     * @return relative location for this backup upload without location prefix and the entire path up to the files e.g.
     *         if the location is
     *         http://localhost:8080/backmeup-storage-service/BMU_filegenerator_492_22_01_2015_21_14/file0.txt then
     *         BMU_filegenerator_492_22_01_2015_21_14 is returned TODO need to clarify if this is the proper return
     *         value for this String
     * @throws StorageException
     */
    public String upload(Properties accessData, Properties properties, List<String> options, Storage storage,
            Progressable progressor) throws StorageException;

}
