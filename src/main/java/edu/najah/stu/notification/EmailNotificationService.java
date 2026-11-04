package edu.najah.stu.notification;

import edu.najah.stu.domain.User;
import edu.najah.stu.service.AuthService;
import edu.najah.stu.service.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes the account emails and hands them to a {@link MailSender}.
 *
 * <p>If sending fails, the problem is logged and the program carries on,
 * because an email is less important than the action that caused it.</p>
 */
public class EmailNotificationService implements NotificationService {

    static final String WELCOME_SUBJECT = "Welcome to Smart Food Delivery";
    static final String LOCKED_SUBJECT = "Your Smart Food Delivery account was locked";

    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("HH:mm 'on' yyyy-MM-dd");
    private static final String SIGNATURE = "\n\nSmart Food Delivery - Nablus";

    private final MailSender mailSender;

    /**
     * Creates the service.
     *
     * @param mailSender sends the emails; must not be null
     */
    public EmailNotificationService(MailSender mailSender) {
        if (mailSender == null) {
            throw new IllegalArgumentException("Mail sender must not be null");
        }
        this.mailSender = mailSender;
    }

    @Override
    public void welcome(User customer) {
        send(customer, WELCOME_SUBJECT, "Hi " + customer.getUsername() + ",\n\n"
                + "Your customer account is ready. Log in to find restaurants that are open now"
                + " near An-Najah, with their distance and walking time." + SIGNATURE);
    }

    @Override
    public void accountLocked(User user, LocalDateTime lockedUntil) {
        send(user, LOCKED_SUBJECT, "Hi " + user.getUsername() + ",\n\n"
                + "Someone typed a wrong password for your account " + AuthService.MAX_FAILED_LOGINS
                + " times in a row, so we locked it.\n"
                + "You can log in again after " + lockedUntil.format(WHEN) + ".\n"
                + "If this was not you, choose a stronger password when you can." + SIGNATURE);
    }

    private void send(User user, String subject, String body) {
        try {
            mailSender.send(user.getEmail(), subject, body);
        } catch (MailException e) {
            LOGGER.log(Level.WARNING, e, () -> "Email '" + subject + "' was not sent to " + user.getUsername());
        }
    }
}
