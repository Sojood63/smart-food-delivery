package edu.najah.stu;



import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private Map<String, user> usersDatabase = new HashMap<>();

    public AuthService() {

        usersDatabase.put("customer1", new user("customer1", "pass123", "CUSTOMER"));
        usersDatabase.put("manager1", new user("manager1", "admin123", "MANAGER"));
    }

    public boolean login(String username, String password) {
        user u = usersDatabase.get(username);
        if (u != null && u.getPassword().equals(password)) {
            System.out.println("Login Successful! Welcome, " + u.getRole() + " " + u.getUsername());
            return true;
        } else {
            System.out.println("Error: Invalid credentials. Please try again.");
            return false;
        }
    }
}
