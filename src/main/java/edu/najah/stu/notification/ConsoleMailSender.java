package edu.najah.stu.notification;

import java.io.PrintStream;

/**
 * Prints emails instead of sending them. Used when no mail account is set
 * up, so the program still works on any computer and in CI.
 */
public class ConsoleMailSender implements MailSender {

    private final PrintStream out;

    /**
     * Creates the sender.
     *
     * @param out where the emails are printed; must not be null
     */
    public ConsoleMailSender(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("Output must not be null");
        }
        this.out = out;
    }

    @Override
    public void send(String to, String subject, String body) {
        out.println("----- Email (printed, not sent: mail is not set up) -----");
        out.println("To: " + to);
        out.println("Subject: " + subject);
        out.println(body);
        out.println("---------------------------------------------------------");
    }
}
