package edu.najah.stu;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.DeliveryAddress;
import edu.najah.stu.domain.HoursSource;
import edu.najah.stu.domain.Landmark;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.domain.Role;
import edu.najah.stu.domain.User;
import edu.najah.stu.domain.WeeklySchedule;
import edu.najah.stu.repository.InMemoryRestaurantRepository;
import edu.najah.stu.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared test data for the order tests: three restaurants, a customer and a
 * manager, with the clock at Monday 16:00 (not a peak hour).
 */
public final class OrderTestData {

    /** Monday afternoon, outside peak hours. */
    public static final LocalDateTime MONDAY_4PM = LocalDateTime.of(2026, 11, 2, 16, 0);

    /** Where Jaltah is, the same point as the old campus so the delivery fee is the base fee. */
    public static final Location CAMPUS = Landmark.OLD_CAMPUS.getLocation();

    private static final String HASH = "pbkdf2$1$c2FsdA==$aGFzaA==";

    private OrderTestData() {
    }

    /**
     * Returns a customer called layan.
     *
     * @return a customer called layan
     */
    public static User customer() {
        return new User("layan", HASH, Role.CUSTOMER, null, "layan@example.com");
    }

    /**
     * Returns the manager of Jaltah.
     *
     * @return the manager of Jaltah
     */
    public static User jaltahManager() {
        return new User("sojood", HASH, Role.MANAGER, "manual-04", "sojood@example.com");
    }

    /**
     * Returns an address at the old campus.
     *
     * @return an address at the old campus
     */
    public static DeliveryAddress campusAddress() {
        return new DeliveryAddress("An-Najah Old Campus", CAMPUS);
    }

    /**
     * Jaltah (open, burger 28, fries 12, shake 15 unavailable), Night Grill
     * (closed at 16:00) and Nafas (open, no location).
     *
     * @return the repository
     */
    public static InMemoryRestaurantRepository restaurants() {
        InMemoryRestaurantRepository repository = new InMemoryRestaurantRepository();
        Restaurant jaltah = new Restaurant("manual-04", "Jaltah", WeeklySchedule.allDay(HoursSource.MANUAL), CAMPUS,
                Cuisine.BURGER);
        jaltah.addMenuItem(new MenuItem("manual-04-1", "Burger", new BigDecimal("28"), true));
        jaltah.addMenuItem(new MenuItem("manual-04-2", "Fries", new BigDecimal("12"), true));
        jaltah.addMenuItem(new MenuItem("manual-04-3", "Shake", new BigDecimal("15"), false));
        Restaurant night = new Restaurant("manual-05", "Night Grill", WeeklySchedule.everyDay(
                new OpeningHours(LocalTime.of(19, 0), LocalTime.of(2, 0)), HoursSource.MANUAL), CAMPUS, Cuisine.GRILL);
        night.addMenuItem(new MenuItem("manual-05-1", "Kebab", new BigDecimal("40"), true));
        Restaurant nafas = new Restaurant("manual-06", "Nafas", WeeklySchedule.allDay(HoursSource.MANUAL), null,
                Cuisine.CAFE);
        nafas.addMenuItem(new MenuItem("manual-06-1", "Latte", new BigDecimal("25"), true));
        repository.save(jaltah);
        repository.save(night);
        repository.save(nafas);
        return repository;
    }

    /**
     * A small user store holding the given users.
     *
     * @param users the users
     * @return the repository
     */
    public static UserRepository users(User... users) {
        List<User> all = new ArrayList<>(List.of(users));
        return new UserRepository() {
            @Override
            public void save(User user) {
                all.add(user);
            }

            @Override
            public Optional<User> findByUsername(String username) {
                return all.stream().filter(user -> user.getUsername().equalsIgnoreCase(username)).findFirst();
            }

            @Override
            public List<User> findAll() {
                return List.copyOf(all);
            }
        };
    }
}
