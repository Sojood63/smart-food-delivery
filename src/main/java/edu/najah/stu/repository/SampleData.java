package edu.najah.stu.repository;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Demo data: well-known restaurants in Nablus.
 *
 * <p>The restaurant names are real. Their opening hours, menus and prices
 * are invented for testing the system and do not come from the restaurants.</p>
 */
public final class SampleData {

    private SampleData() {
    }

    /**
     * Creates a repository filled with the demo restaurants.
     *
     * @return a new in-memory repository holding the demo restaurants
     */
    public static InMemoryRestaurantRepository createRepository() {
        InMemoryRestaurantRepository repository = new InMemoryRestaurantRepository();

        Restaurant aqsa = restaurant("R1", "Al-Aqsa Sweets", "08:00", "22:00");
        aqsa.addMenuItem(item("R1-1", "Knafeh Nabulsieh (portion)", "12.00", true));
        aqsa.addMenuItem(item("R1-2", "Knafeh tray (1 kg)", "60.00", true));
        aqsa.addMenuItem(item("R1-3", "Harissa", "8.00", false));
        repository.save(aqsa);

        Restaurant tanoreen = restaurant("R2", "Tanoreen", "12:00", "23:00");
        tanoreen.addMenuItem(item("R2-1", "Tanoor chicken (half)", "35.00", true));
        tanoreen.addMenuItem(item("R2-2", "Tanoor fish", "55.00", true));
        tanoreen.addMenuItem(item("R2-3", "Fattoush salad", "15.00", true));
        repository.save(tanoreen);

        Restaurant zodiac = restaurant("R3", "Zodiac", "10:00", "01:00");
        zodiac.addMenuItem(item("R3-1", "Mixed grill", "50.00", true));
        zodiac.addMenuItem(item("R3-2", "Zaatar manakeesh", "6.00", true));
        zodiac.addMenuItem(item("R3-3", "Shish tawook", "38.00", false));
        repository.save(zodiac);

        Restaurant rozana = restaurant("R4", "Rozana", "11:00", "23:30");
        rozana.addMenuItem(item("R4-1", "Stuffed vine leaves", "25.00", true));
        rozana.addMenuItem(item("R4-2", "Kubbeh", "20.00", true));
        rozana.addMenuItem(item("R4-3", "Kebab", "40.00", true));
        repository.save(rozana);

        Restaurant afandi = restaurant("R5", "Saleem Afandi", "13:00", "00:00");
        afandi.addMenuItem(item("R5-1", "Fried sea fish", "60.00", true));
        afandi.addMenuItem(item("R5-2", "Prawns", "75.00", false));
        afandi.addMenuItem(item("R5-3", "Hummus", "12.00", true));
        repository.save(afandi);

        Restaurant zadona = restaurant("R6", "Zadona", "12:00", "22:00");
        zadona.addMenuItem(item("R6-1", "Lamb with rice", "45.00", true));
        zadona.addMenuItem(item("R6-2", "Chicken with rice", "35.00", true));
        repository.save(zadona);

        return repository;
    }

    private static Restaurant restaurant(String id, String name, String opensAt, String closesAt) {
        OpeningHours hours = new OpeningHours(LocalTime.parse(opensAt), LocalTime.parse(closesAt));
        return new Restaurant(id, name, hours);
    }

    private static MenuItem item(String id, String name, String price, boolean available) {
        return new MenuItem(id, name, new BigDecimal(price), available);
    }
}
