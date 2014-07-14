package org.backmeup.rest.converters;

import org.backmeup.model.constants.BackupJobStatus;
import org.backmeup.model.dto.BackupJobDTO.JobStatus;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class BackupJobStatusConverter implements CustomConverter {

	@SuppressWarnings("rawtypes")
	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}

		if (source instanceof BackupJobStatus) {
			BackupJobStatus status = (BackupJobStatus) source;

			switch (status) {
			case queued:
				return JobStatus.queued;

			case running:
				return JobStatus.running;

			case successful:
				return JobStatus.successful;
				
			case error:
				return JobStatus.error;
				
			default:
				throw new IllegalStateException();
			}

		} else if (source instanceof JobStatus) {
			JobStatus status = (JobStatus) source;
			switch (status) {
			case queued:
				return BackupJobStatus.queued;

			case running:
				return BackupJobStatus.running;

			case successful:
				return BackupJobStatus.successful;
				
			case error:
				return BackupJobStatus.error;

			default:
				throw new IllegalStateException();
			}
		} else {
			throw new MappingException("Converter BackupJobStatusConverter used incorrectly. +"
					+ "Arguments passed in were: " + destination + " and " + source);
		}
	}
}
