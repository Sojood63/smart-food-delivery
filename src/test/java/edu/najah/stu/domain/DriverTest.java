package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drivers become busy and free again (US3.1).
 */
class DriverTest {

    private static final Location CAMPUS = Landmark.OLD_CAMPUS.getLocation();

    @Test
    void driversBecomeBusyAndFreeAgainWhereTheyDelivered() {
        Driver driver = new Driver("D4", "Yousef", CAMPUS, 30);
        Location customer = new Location(32.2276, 35.2206);

        assertTrue(driver.isAvailable());
        driver.markBusy();
        assertFalse(driver.isAvailable());
        assertEquals(DriverStatus.BUSY, driver.getStatus());

        driver.markAvailable(customer);
        assertTrue(driver.isAvailable());
        assertEquals(customer, driver.getLocation());
        driver.markAvailable(null);
        assertEquals(customer, driver.getLocation());
    }

    @Test
    void fasterDriversNeedFewerMinutes() {
        Location restaurant = new Location(32.2234, 35.2397);
        Location customer = new Location(32.2276, 35.2206);

        long slow = new Driver("D1", "Ahmad", CAMPUS, 15).minutesToDeliver(restaurant, customer);
        long fast = new Driver("D2", "Khaled", CAMPUS, 35).minutesToDeliver(restaurant, customer);

        assertTrue(fast < slow, fast + " < " + slow);
        assertEquals(0, new Driver("D4", "Y", restaurant, 30).minutesToDeliver(restaurant, restaurant));
    }

    @Test
    void driverNeedsIdNameLocationAndSpeed() {
        assertThrows(IllegalArgumentException.class, () -> new Driver(" ", "Yousef", CAMPUS, 30));
        assertThrows(IllegalArgumentException.class, () -> new Driver("D1", null, CAMPUS, 30));
        assertThrows(IllegalArgumentException.class, () -> new Driver("D1", "Yousef", null, 30));
        assertThrows(IllegalArgumentException.class, () -> new Driver("D1", "Yousef", CAMPUS, 0));
        Driver driver = new Driver("D1", "Yousef", CAMPUS, 30);
        assertEquals("D1", driver.getId());
        assertEquals("Yousef", driver.getName());
        assertEquals(30, driver.speedKmh());
    }
}
