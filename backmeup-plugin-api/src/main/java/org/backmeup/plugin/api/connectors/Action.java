package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.storage.Storage;

public interface Action {

	public void doAction(Properties accessData, Properties properties, List<String> options, Storage storage, BackupJob job, Progressable progressor)
			throws ActionException;
	
}
