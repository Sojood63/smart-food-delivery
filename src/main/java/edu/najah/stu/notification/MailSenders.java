package edu.najah.stu.notification;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Chooses how emails go out, from a settings file and the environment variables.
 *
 * <p>The settings can be written in a {@code mail.properties} file next to
 * {@code pom.xml} (Git ignores it, so the key is never uploaded), or given as
 * environment variables, which win over the file:</p>
 *
 * <pre>
 * MAIL_HOST      SMTP server             (default smtp-relay.brevo.com)
 * MAIL_PORT      SMTP port               (default 587)
 * MAIL_USERNAME  SMTP login              (required)
 * MAIL_PASSWORD  SMTP key / App Password (required)
 * MAIL_FROM      sender address          (default: MAIL_USERNAME)
 * </pre>
 */
public final class MailSenders {

    /** Environment variable holding the SMTP server. */
    public static final String HOST_VARIABLE = "MAIL_HOST";

    /** Environment variable holding the SMTP port. */
    public static final String PORT_VARIABLE = "MAIL_PORT";

    /** Environment variable holding the SMTP login. */
    public static final String USERNAME_VARIABLE = "MAIL_USERNAME";

    /** Environment variable holding the SMTP key or App Password. */
    public static final String PASSWORD_VARIABLE = "MAIL_PASSWORD";

    /** Environment variable holding the sender address. */
    public static final String FROM_VARIABLE = "MAIL_FROM";

    /** Server used when {@value #HOST_VARIABLE} is not set. */
    public static final String DEFAULT_HOST = "smtp-relay.brevo.com";

    private MailSenders() {
    }

    /**
     * Reads the settings file, adds the environment variables on top, and returns the sender to use.
     * Prints one line saying whether emails are sent or only printed.
     *
     * @param environment  the environment variables, usually {@code System.getenv()}
     * @param settingsFile the optional settings file, usually {@code mail.properties}
     * @param out          where the status line and printed emails go
     * @return the sender to use
     * @throws UncheckedIOException     if the settings file exists but cannot be read
     * @throws IllegalArgumentException if a setting is invalid, such as a port that is not a number
     */
    public static MailSender fromSettings(Map<String, String> environment, Path settingsFile, PrintStream out) {
        Map<String, String> settings = new HashMap<>(readFile(settingsFile));
        environment.forEach((name, value) -> {
            if (isSet(value)) {
                settings.put(name, value);
            }
        });
        MailSender sender = fromEnvironment(settings, out);
        if (sender instanceof SmtpMailSender) {
            out.println("Email: sending real emails through "
                    + valueOr(settings.get(HOST_VARIABLE), DEFAULT_HOST));
        } else {
            out.println("Email: not set up (no " + settingsFile.getFileName() + "), emails will be printed here");
        }
        return sender;
    }

    /**
     * Reads {@code NAME=value} lines from a settings file.
     *
     * @param file the file; a missing file gives no settings
     * @return the settings found
     */
    static Map<String, String> readFile(Path file) {
        Map<String, String> settings = new HashMap<>();
        if (!Files.exists(file)) {
            return settings;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        properties.stringPropertyNames().forEach(name -> settings.put(name, properties.getProperty(name).trim()));
        return settings;
    }

    /**
     * Returns a real sender if the login and password are set, otherwise one that prints emails.
     *
     * @param environment the environment variables, usually {@code System.getenv()}
     * @param out         where printed emails go
     * @return the sender to use
     * @throws IllegalArgumentException if a variable is set but invalid, such as a port that is not a number
     */
    public static MailSender fromEnvironment(Map<String, String> environment, PrintStream out) {
        String username = environment.get(USERNAME_VARIABLE);
        String password = environment.get(PASSWORD_VARIABLE);
        if (!isSet(username) || !isSet(password)) {
            return new ConsoleMailSender(out);
        }
        String host = valueOr(environment.get(HOST_VARIABLE), DEFAULT_HOST);
        String from = valueOr(environment.get(FROM_VARIABLE), username);
        int port = port(environment.get(PORT_VARIABLE));
        return new SmtpMailSender(new SmtpSettings(host, port, username, password, from));
    }

    private static int port(String text) {
        if (!isSet(text)) {
            return SmtpSettings.STARTTLS_PORT;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PORT_VARIABLE + " must be a number: " + text);
        }
    }

    private static String valueOr(String value, String fallback) {
        return isSet(value) ? value : fallback;
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
