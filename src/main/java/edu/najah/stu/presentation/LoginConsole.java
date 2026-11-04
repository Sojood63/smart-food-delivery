package edu.najah.stu.presentation;

import edu.najah.stu.domain.User;
import edu.najah.stu.service.AuthService;
import edu.najah.stu.service.LoginFailedException;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

/**
 * The first screen of the program: log in, or create a customer account.
 */
public class LoginConsole {

    private static final String MENU = String.join(System.lineSeparator(),
            "",
            "=== Smart Food Delivery ===",
            "1) Log in",
            "2) Create a customer account",
            "0) Exit");

    private final AuthService authService;
    private final ConsoleIO io;

    /**
     * Creates the console.
     *
     * @param authService checks logins and creates accounts; must not be null
     * @param in          where answers are read from; must not be null
     * @param out         where text is printed; must not be null
     */
    public LoginConsole(AuthService authService, Scanner in, PrintStream out) {
        if (authService == null) {
            throw new IllegalArgumentException("Auth service must not be null");
        }
        this.authService = authService;
        this.io = new ConsoleIO(in, out);
    }

    /**
     * Shows the menu until someone logs in or chooses to exit.
     *
     * @return the logged-in user, or empty if the program should end
     */
    public Optional<User> run() {
        String choice = io.ask(MENU);
        while (choice != null && !choice.equals("0")) {
            if (choice.equals("1")) {
                Optional<User> user = logIn();
                if (user.isPresent()) {
                    return user;
                }
            } else if (choice.equals("2")) {
                register();
            } else {
                io.println("Unknown choice: " + choice);
            }
            choice = io.ask(MENU);
        }
        return Optional.empty();
    }

    private Optional<User> logIn() {
        String username = io.answer("Username:");
        String password = io.answer("Password:");
        try {
            User user = authService.login(username, password);
            io.println("Welcome, " + user.getUsername() + " (" + user.getRole().getDisplayName() + ")");
            return Optional.of(user);
        } catch (LoginFailedException e) {
            io.println("Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    private void register() {
        String username = io.answer("Choose a username (3-20 letters, digits, '_' or '.'):");
        String password = io.answer("Choose a password (at least 8 characters, with a letter and a digit):");
        String email = io.answer("Your email:");
        try {
            User customer = authService.registerCustomer(username, password, email);
            io.println("Account created for " + customer.getUsername() + ". You can log in now.");
        } catch (IllegalArgumentException e) {
            io.println("Error: " + e.getMessage());
        }
    }
}
