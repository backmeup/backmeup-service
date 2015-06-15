package org.backmeup.model.exceptions;

public class UnknownUserPropertyException extends BackMeUpException {
    private static final long serialVersionUID = 1L;

    private final String property;

    public UnknownUserPropertyException(String property) {
        super("Unknown user property!");
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
