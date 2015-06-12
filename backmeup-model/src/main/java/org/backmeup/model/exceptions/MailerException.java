package org.backmeup.model.exceptions;

public class MailerException extends BackMeUpException {
    private static final long serialVersionUID = 1L;
    private final String to;
    private final String subject;
    private final String message;
    private final String mimeType;

    public MailerException(String to, String subject, String message, String mimeType, Exception innerException) {
        super("Failed to send email!", innerException);
        this.to = to;
        this.message = message;
        this.mimeType = mimeType;
        this.subject = subject;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getMimeType() {
        return mimeType;
    }
}
