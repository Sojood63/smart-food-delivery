package edu.najah.stu.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Choosing a real or a printing sender, and printing emails.
 */
class MailSendersTest {

    private final ByteArrayOutputStream printed = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(printed, true, StandardCharsets.UTF_8);

    @TempDir
    Path folder;

    private Path settingsFile(String... lines) throws IOException {
        Path file = folder.resolve("mail.properties");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void settingsFileTurnsOnRealEmails() throws IOException {
        Path file = settingsFile("# my Brevo details", "MAIL_USERNAME = 9a1b2c001@smtp-brevo.com ",
                "MAIL_PASSWORD=xsmtpsib-key", "MAIL_FROM=smartfood.najah@gmail.com");

        MailSender sender = MailSenders.fromSettings(Map.of(), file, out);

        assertInstanceOf(SmtpMailSender.class, sender);
        assertTrue(printed.toString(StandardCharsets.UTF_8)
                .contains("Email: sending real emails through smtp-relay.brevo.com"));
    }

    @Test
    void readsNamesAndTrimsValues() throws IOException {
        Map<String, String> settings = MailSenders.readFile(settingsFile("MAIL_USERNAME = login ", "MAIL_PORT=2525"));

        assertEquals(Map.of("MAIL_USERNAME", "login", "MAIL_PORT", "2525"), settings);
    }

    @Test
    void noFileAndNoVariablesPrintsEmails() {
        MailSender sender = MailSenders.fromSettings(Map.of(), folder.resolve("mail.properties"), out);

        assertInstanceOf(ConsoleMailSender.class, sender);
        assertTrue(printed.toString(StandardCharsets.UTF_8).contains("Email: not set up (no mail.properties)"));
    }

    @Test
    void environmentVariablesWinOverTheFileButEmptyOnesDoNot() throws IOException {
        Path file = settingsFile("MAIL_USERNAME=login", "MAIL_PASSWORD=key", "MAIL_PORT=five");

        assertThrows(IllegalArgumentException.class, () -> MailSenders.fromSettings(Map.of(), file, out));
        assertInstanceOf(SmtpMailSender.class, MailSenders.fromSettings(
                Map.of(MailSenders.PORT_VARIABLE, "587", MailSenders.USERNAME_VARIABLE, " "), file, out));
    }

    @Test
    void unreadableSettingsFileIsReported() throws IOException {
        Path notAFile = Files.createDirectory(folder.resolve("mail.properties"));

        assertThrows(UncheckedIOException.class, () -> MailSenders.readFile(notAFile));
    }

    @Test
    void loginAndPasswordGiveARealSender() {
        Map<String, String> environment = Map.of(
                MailSenders.USERNAME_VARIABLE, "9a1b2c001@smtp-brevo.com",
                MailSenders.PASSWORD_VARIABLE, "xsmtpsib-key");

        assertInstanceOf(SmtpMailSender.class, MailSenders.fromEnvironment(environment, out));
    }

    @Test
    void everySettingCanBeChosen() {
        Map<String, String> environment = Map.of(
                MailSenders.HOST_VARIABLE, "smtp.gmail.com",
                MailSenders.PORT_VARIABLE, " 2525 ",
                MailSenders.USERNAME_VARIABLE, "smartfood.najah@gmail.com",
                MailSenders.PASSWORD_VARIABLE, "abcd efgh ijkl mnop",
                MailSenders.FROM_VARIABLE, "smartfood.najah@gmail.com");

        assertInstanceOf(SmtpMailSender.class, MailSenders.fromEnvironment(environment, out));
    }

    @Test
    void portThatIsNotANumberIsReported() {
        Map<String, String> environment = Map.of(
                MailSenders.PORT_VARIABLE, "five",
                MailSenders.USERNAME_VARIABLE, "login",
                MailSenders.PASSWORD_VARIABLE, "key");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> MailSenders.fromEnvironment(environment, out));
        assertTrue(error.getMessage().contains("MAIL_PORT"));
    }

    @Test
    void missingOrEmptyLoginGivesAPrintingSender() {
        assertInstanceOf(ConsoleMailSender.class, MailSenders.fromEnvironment(Map.of(), out));
        assertInstanceOf(ConsoleMailSender.class, MailSenders.fromEnvironment(
                Map.of(MailSenders.USERNAME_VARIABLE, "login"), out));
        assertInstanceOf(ConsoleMailSender.class, MailSenders.fromEnvironment(
                Map.of(MailSenders.USERNAME_VARIABLE, " ", MailSenders.PASSWORD_VARIABLE, "key"), out));
    }

    @Test
    void printingSenderShowsTheWholeEmail() {
        new ConsoleMailSender(out).send("layan@example.com", "Hello", "Body text");

        String text = printed.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("To: layan@example.com"));
        assertTrue(text.contains("Subject: Hello"));
        assertTrue(text.contains("Body text"));
        assertTrue(text.contains("not sent"));
    }

    @Test
    void printingSenderNeedsAnOutput() {
        assertThrows(IllegalArgumentException.class, () -> new ConsoleMailSender(null));
    }
}
