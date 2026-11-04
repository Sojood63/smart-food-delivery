package edu.najah.stu.notification;

import edu.najah.stu.event.EventListener;
import edu.najah.stu.event.SystemEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Turns every system event into an email to the person it concerns
 * (an observer in the Observer pattern).
 *
 * <p>If sending fails, the problem is logged and the program carries on,
 * because an email is less important than the action that caused it.</p>
 */
public class EmailEventListener implements EventListener {

    static final String SIGNATURE = "\n\nSmart Food Delivery - Nablus";

    private static final Logger LOGGER = Logger.getLogger(EmailEventListener.class.getName());

    private final MailSender mailSender;

    /**
     * Creates the listener.
     *
     * @param mailSender sends the emails; must not be null
     */
    public EmailEventListener(MailSender mailSender) {
        if (mailSender == null) {
            throw new IllegalArgumentException("Mail sender must not be null");
        }
        this.mailSender = mailSender;
    }

    @Override
    public void onEvent(SystemEvent event) {
        String body = "Hi " + event.recipientName() + ",\n\n" + event.message() + SIGNATURE;
        try {
            mailSender.send(event.recipientEmail(), event.subject(), body);
        } catch (MailException e) {
            LOGGER.log(Level.WARNING, e, () -> "Email '" + event.subject() + "' was not sent to "
                    + event.recipientName());
        }
    }
}
