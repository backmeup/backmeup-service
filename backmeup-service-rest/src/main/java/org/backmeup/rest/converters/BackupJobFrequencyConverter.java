package org.backmeup.rest.converters;

import org.backmeup.model.dto.BackupJobDTO.JobFrequency;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class BackupJobFrequencyConverter implements CustomConverter {

    @Override
    @SuppressWarnings("rawtypes")
    public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
        if (source == null) {
            return null;
        }

        if (source instanceof String) {
            String timeExpression = (String) source;

            switch (timeExpression) {
            case "daily":
                return JobFrequency.daily;

            case "weekly":
                return JobFrequency.weekly;

            case "monthly":
                return JobFrequency.montly;

            case "realtime":
                return JobFrequency.once;
            default:
                throw new IllegalStateException();
            }

        } else if (source instanceof JobFrequency) {
            JobFrequency jobFrequency = (JobFrequency) source;
            switch (jobFrequency) {
            case daily:
                return "daily";

            case weekly:
                return "weekly";

            case montly:
                return "monthly";

            case once:
                return "realtime";

            default:
                throw new IllegalStateException();

            }
        } else {
            throw new MappingException("Converter BackupJobFrequencyConverter used incorrectly. +" + "Arguments passed in were: "
                    + destination + " and " + source);
        }
    }
}
