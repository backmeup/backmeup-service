package org.backmeup.model.exceptions;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.ValidationExceptionType;

/**
 * When a plugin returns false when calling InputBased#isValid, this exception
 * will be thrown by the BusinessLogic.
 * 
 * @author fschoeppl, w.eibner
 *
 */
public class ValidationException extends BackMeUpException {
    private static final long serialVersionUID = 1L;

    private final ValidationExceptionType type;
    private final ValidationNotes notes;

    public ValidationException(ValidationExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.notes = new ValidationNotes();
    }

    public ValidationException(ValidationExceptionType type, String message) {
        this(type, message, null);
    }

    public ValidationException(ValidationExceptionType type, ValidationNotes notes) {
        super("Validation failed", null);
        this.type = type;
        if (notes != null) {
            this.notes = notes;
        } else {
            this.notes = new ValidationNotes();
        }
    }

    public ValidationExceptionType getType() {
        return type;
    }

    public ValidationNotes getNotes() {
        return notes;
    }
}
