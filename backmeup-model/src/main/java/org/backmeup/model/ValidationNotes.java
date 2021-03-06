package org.backmeup.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.backmeup.model.spi.ValidationExceptionType;

public class ValidationNotes {
    public static final String AUTH_EXCEPTION = "Error during authentication";
    public static final String API_EXCEPTION = "Error during API call";
    public static final String CONFIG_EXCEPTION = "Error in plugin configuration";
    public static final String ERROR = "Plugin threw an unhandled error during the validation";
    public static final String NO_VALIDATOR_AVAILABLE = "Plugin doesn't provide a validator";
    public static final String PLUGIN_UNAVAILABLE = "Plugin is not available";

    private final List<ValidationEntry> validationNotes = new ArrayList<>();
    private BackupJob job;

    private static Map<ValidationExceptionType, String> exceptionText;

    static {
        exceptionText = new HashMap<>();
        exceptionText.put(ValidationExceptionType.AuthException, AUTH_EXCEPTION);
        exceptionText.put(ValidationExceptionType.APIException, API_EXCEPTION);
        exceptionText.put(ValidationExceptionType.ConfigException, CONFIG_EXCEPTION);
        exceptionText.put(ValidationExceptionType.Error, ERROR);
        exceptionText.put(ValidationExceptionType.NoValidatorAvailable, NO_VALIDATOR_AVAILABLE);
        exceptionText.put(ValidationExceptionType.PluginUnavailable, NO_VALIDATOR_AVAILABLE);
    }

    public void addValidationEntry(ValidationExceptionType type, String pluginId, Exception cause) {
        this.validationNotes.add(new ValidationEntry(type, pluginId, exceptionText.get(type), cause));
    }

    public void addValidationEntry(ValidationExceptionType type, Exception e) {
        this.addValidationEntry(type, null, e);
    }

    public void addValidationEntry(ValidationExceptionType type, String pluginId, String message) {
        this.validationNotes.add(new ValidationEntry(type, pluginId, message, null));
    }

    public void addAll(ValidationNotes notes) {
        if (notes != null) {
            this.validationNotes.addAll(notes.getValidationEntries());
        }
    }

    public boolean hasEntries() {
        return this.validationNotes != null && !this.validationNotes.isEmpty();
    }

    public List<ValidationEntry> getValidationEntries() {
        return this.validationNotes;
    }

    public BackupJob getJob() {
        return job;
    }

    public void setJob(BackupJob job) {
        this.job = job;
    }

    public static ValidationNotes createExceptionNotes(ValidationExceptionType type, String pluginId, Exception cause) {
        ValidationNotes notes = new ValidationNotes();
        notes.addValidationEntry(type, pluginId, cause);
        return notes;
    }

    public static class ValidationEntry {
        private ValidationExceptionType type;
        private String message;
        private String pluginId;
        private String cause;
        private String stackTrace;

        public ValidationEntry(ValidationExceptionType type, String pluginId, String message, Exception cause) {
            this.type = type;
            this.pluginId = pluginId;
            this.message = message;
            if (cause != null) {
                this.setCause(cause.getMessage());
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    cause.printStackTrace(pw); // NOSONAR log the stack trace
                                               // into pw
                    this.setStackTrace(sw.toString());
                } catch (IOException e) {
                    // can never happen because StringWriter has no IO
                }
            }
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ValidationExceptionType getType() {
            return type;
        }

        public void setType(ValidationExceptionType type) {
            this.type = type;
        }

        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public String getCause() {
            return cause;
        }

        public void setCause(String cause) {
            this.cause = cause;
        }
    }
}
