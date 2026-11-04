package edu.najah.stu.service;

/**
 * Thrown when a login does not succeed.
 */
public class LoginFailedException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message what the user is told
     */
    public LoginFailedException(String message) {
        super(message);
    }
}
