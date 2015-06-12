package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Map;

import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.plugin.api.storage.Storage;

public interface Action {

    void doAction(Map<String, String> authData, Map<String, String> properties,
            List<String> options, Storage storage, BackupJobExecutionDTO job,
            Progressable progressor) throws ActionException;
}
