package org.backmeup.logic.impl.helper;

import java.util.Date;

import org.backmeup.model.constants.DelayTimes;
import org.backmeup.model.dto.ExecutionTime;
import org.backmeup.model.dto.JobCreationRequest;

public class BackUpJobCreationHelper {

    public static ExecutionTime getExecutionTimeFor(JobCreationRequest request) {
        String timeExpression = request.getTimeExpression();
        Date now = new Date();

        if (timeExpression.equalsIgnoreCase("daily")) {
            return new ExecutionTime(now, DelayTimes.DELAY_DAILY, true);

        } else if (timeExpression.equalsIgnoreCase("weekly")) {
            return new ExecutionTime(now, DelayTimes.DELAY_WEEKLY, true);

        } else if (timeExpression.equalsIgnoreCase("monthly")) {
            return new ExecutionTime(now, DelayTimes.DELAY_MONTHLY, true);

        } else if (timeExpression.equalsIgnoreCase("yearly")) {
            return new ExecutionTime(now, DelayTimes.DELAY_YEARLY, true);

        } else if (timeExpression.equalsIgnoreCase("realtime")) {
            return new ExecutionTime(now, DelayTimes.DELAY_REALTIME, false);

        } else {
            return new ExecutionTime(now, DelayTimes.DELAY_MONTHLY, false);
        }
    }
}
