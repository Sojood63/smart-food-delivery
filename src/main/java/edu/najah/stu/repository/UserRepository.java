package edu.najah.stu.repository;

import edu.najah.stu.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Stores and finds users.
 */
public interface UserRepository {

    /**
     * Saves a user, replacing any user with the same username.
     *
     * @param user the user to save; must not be null
     */
    void save(User user);

    /**
     * Finds a user by username, ignoring upper and lower case.
     *
     * @param username the username
     * @return the user, or empty if there is none
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns every stored user in the order they were first saved.
     *
     * @return an unmodifiable list of all users
     */
    List<User> findAll();
}
