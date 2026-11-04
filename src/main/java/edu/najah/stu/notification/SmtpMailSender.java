package edu.najah.stu.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Sends real emails through an SMTP server such as Brevo or Gmail.
 *
 * <p>The login details come from environment variables, see
 * {@link MailSenders#fromEnvironment}, and are never written in the code.
 * The connection is encrypted with STARTTLS.</p>
 */
public class SmtpMailSender implements MailSender {

    static final String SENDER_NAME = "Smart Food Delivery";

    private static final String TIMEOUT_MILLIS = "10000";
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final Session session;
    private final String from;

    /**
     * Creates a sender for one SMTP account.
     *
     * @param settings the server and login details; must not be null
     */
    public SmtpMailSender(SmtpSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Mail settings must not be null");
        }
        this.from = settings.from();
        Properties properties = new Properties();
        properties.put("mail.smtp.host", settings.host());
        properties.put("mail.smtp.port", Integer.toString(settings.port()));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        properties.put("mail.smtp.connectiontimeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.timeout", TIMEOUT_MILLIS);
        String username = settings.username();
        String password = settings.password();
        this.session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            Transport.send(toMessage(to, subject, body));
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailException("Could not send email to " + to, e);
        }
    }

    /**
     * Builds the email without sending it, so tests can check it without a network.
     */
    MimeMessage toMessage(String to, String subject, String body)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, SENDER_NAME, CHARSET));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
        message.setSubject(subject, CHARSET);
        message.setText(body, CHARSET);
        return message;
    }
}
