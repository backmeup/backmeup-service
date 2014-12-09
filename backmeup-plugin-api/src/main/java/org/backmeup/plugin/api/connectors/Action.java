package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.plugin.api.storage.Storage;

public interface Action {

    public void doAction(Properties accessData, Properties properties, List<String> options, Storage storage,
            BackupJobDTO job, Progressable progressor) throws ActionException;

}
