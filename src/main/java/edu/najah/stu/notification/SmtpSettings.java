package edu.najah.stu.notification;

/**
 * Where and how to log in to send emails.
 *
 * @param host     the SMTP server, for example {@code smtp-relay.brevo.com} or {@code smtp.gmail.com}
 * @param port     the server port; 587 with STARTTLS
 * @param username the login name for the server
 * @param password the SMTP key or App Password
 * @param from     the address the emails come from; must be allowed by the server
 */
public record SmtpSettings(String host, int port, String username, String password, String from) {

    /** The usual port for sending mail with STARTTLS encryption. */
    public static final int STARTTLS_PORT = 587;

    private static final int MAX_PORT = 65_535;

    /**
     * Checks the settings and removes extra spaces.
     *
     * @param host     the SMTP server
     * @param port     the server port, from 1 to 65535
     * @param username the login name for the server
     * @param password the SMTP key or App Password; spaces inside it are removed
     * @param from     the address the emails come from
     *
     * @throws IllegalArgumentException if a value is missing or the port is out of range
     */
    public SmtpSettings {
        host = required(host, "Mail host");
        username = required(username, "Mail username");
        password = required(password, "Mail password").replace(" ", "");
        from = required(from, "Sender address");
        if (port < 1 || port > MAX_PORT) {
            throw new IllegalArgumentException("Mail port must be between 1 and " + MAX_PORT + ": " + port);
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value.trim();
    }

    /**
     * Hides the password, so it never shows up in logs.
     *
     * @return the settings without the password
     */
    @Override
    public String toString() {
        return "SmtpSettings[host=" + host + ", port=" + port + ", username=" + username + ", from=" + from + "]";
    }
}
