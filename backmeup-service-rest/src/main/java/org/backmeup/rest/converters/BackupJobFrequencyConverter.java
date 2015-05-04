package org.backmeup.rest.converters;

import org.backmeup.model.constants.JobFrequency;
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
                return JobFrequency.DAILY;

            case "weekly":
                return JobFrequency.WEEKLY;

            case "monthly":
                return JobFrequency.MONTHLY;

            case "realtime":
                return JobFrequency.ONCE;
            default:
                throw new IllegalStateException();
            }

        } else if (source instanceof JobFrequency) {
            JobFrequency jobFrequency = (JobFrequency) source;
            switch (jobFrequency) {
            case DAILY:
                return "daily";

            case WEEKLY:
                return "weekly";

            case MONTHLY:
                return "monthly";

            case ONCE:
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
