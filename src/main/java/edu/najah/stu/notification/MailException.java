package edu.najah.stu.notification;

/**
 * Thrown when an email cannot be sent.
 */
public class MailException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
