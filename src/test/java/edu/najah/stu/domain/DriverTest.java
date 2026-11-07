package edu.najah.stu.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Driver types and their own rules (US5.3, US5.4).
 */
class DriverTest {

    private static final Location CAMPUS = Landmark.OLD_CAMPUS.getLocation();

    @Test
    void bicyclesOnlyDoShortTripsAndSmallOrders() {
        Driver bicycle = new BicycleDriver("D1", "Ahmad", CAMPUS);

        assertEquals("Bicycle", bicycle.vehicle());
        assertTrue(bicycle.canDeliver(3, 5));
        assertFalse(bicycle.canDeliver(3.1, 1));
        assertFalse(bicycle.canDeliver(1, 6));
    }

    @Test
    void motorcyclesDoMediumDistances() {
        Driver motorcycle = new MotorcycleDriver("D2", "Khaled", CAMPUS);

        assertEquals("Motorcycle", motorcycle.vehicle());
        assertTrue(motorcycle.canDeliver(8, 10));
        assertFalse(motorcycle.canDeliver(8.5, 1));
        assertFalse(motorcycle.canDeliver(1, 11));
    }

    @Test
    void carsSupportLargerOrders() {
        Driver car = new CarDriver("D4", "Yousef", CAMPUS);

        assertEquals("Car", car.vehicle());
        assertTrue(car.canDeliver(15, 40));
        assertFalse(car.canDeliver(21, 1));
    }

    @Test
    void electricVehiclesNeedEnoughBatteryForTheTripAndBack() {
        ElectricVehicleDriver electric = new ElectricVehicleDriver("D5", "Rami", CAMPUS, 10);

        assertEquals("Electric vehicle", electric.vehicle());
        assertEquals(10, electric.getRangeKm());
        assertTrue(electric.canDeliver(5, 3));
        assertFalse(electric.canDeliver(6, 3));
        assertFalse(electric.canDeliver(1, 31));
        assertThrows(IllegalArgumentException.class, () -> new ElectricVehicleDriver("D6", "Ali", CAMPUS, -1));
    }

    @Test
    void driversBecomeBusyAndFreeAgainWhereTheyDelivered() {
        Driver driver = new CarDriver("D4", "Yousef", CAMPUS);
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
    void fasterVehiclesNeedFewerMinutes() {
        Location restaurant = new Location(32.2234, 35.2397);
        Location customer = new Location(32.2276, 35.2206);

        long bicycle = new BicycleDriver("D1", "Ahmad", CAMPUS).minutesToDeliver(restaurant, customer);
        long motorcycle = new MotorcycleDriver("D2", "Khaled", CAMPUS).minutesToDeliver(restaurant, customer);

        assertTrue(motorcycle < bicycle, motorcycle + " < " + bicycle);
        assertEquals(0, new CarDriver("D4", "Y", restaurant).minutesToDeliver(restaurant, restaurant));
    }

    @Test
    void driverNeedsIdNameAndLocation() {
        assertThrows(IllegalArgumentException.class, () -> new CarDriver(" ", "Yousef", CAMPUS));
        assertThrows(IllegalArgumentException.class, () -> new CarDriver("D1", null, CAMPUS));
        assertThrows(IllegalArgumentException.class, () -> new CarDriver("D1", "Yousef", null));
        Driver car = new CarDriver("D1", "Yousef", CAMPUS);
        assertEquals("D1", car.getId());
        assertEquals("Yousef", car.getName());
    }
}
