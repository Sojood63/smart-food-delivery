package edu.najah.stu.repository;

import edu.najah.stu.domain.Cuisine;
import edu.najah.stu.domain.Location;
import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.OpeningHours;
import edu.najah.stu.domain.Restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Demo data: real restaurants in Nablus taken from OpenStreetMap.
 *
 * <p>Names, locations, opening hours and cuisines come from OpenStreetMap
 * (map data &copy; OpenStreetMap contributors, ODbL). Each id is the
 * OpenStreetMap node id. The menus and prices are invented for testing.</p>
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

        Restaurant abuSeir = restaurant("osm-6381099710", "AbuSeir Shahrazad", "08:00", "21:00",
                32.2209258, 35.2591823, Cuisine.SWEETS);
        abuSeir.addMenuItem(item("osm-6381099710-1", "Knafeh Nabulsieh (portion)", "12.00", true));
        abuSeir.addMenuItem(item("osm-6381099710-2", "Baklava (box)", "30.00", true));
        repository.save(abuSeir);

        Restaurant malaky = restaurant("osm-11708855669", "Malaky Broast", "09:00", "01:00",
                32.2233358, 35.2602433, Cuisine.CHICKEN);
        malaky.addMenuItem(item("osm-11708855669-1", "Broasted chicken meal", "30.00", true));
        malaky.addMenuItem(item("osm-11708855669-2", "Chicken sandwich", "15.00", true));
        repository.save(malaky);

        Restaurant r1948 = restaurant("osm-13422150401", "1948", "09:00", "23:30",
                32.2288108, 35.2194877, Cuisine.PALESTINIAN);
        r1948.addMenuItem(item("osm-13422150401-1", "Musakhan", "40.00", true));
        r1948.addMenuItem(item("osm-13422150401-2", "Maqluba", "45.00", false));
        repository.save(r1948);

        Restaurant pizzaTime = restaurant("osm-4939629521", "Pizza Time", "11:00", "00:00",
                32.2241324, 35.2313647, Cuisine.PIZZA);
        pizzaTime.addMenuItem(item("osm-4939629521-1", "Margherita (medium)", "30.00", true));
        pizzaTime.addMenuItem(item("osm-4939629521-2", "Chicken pizza (medium)", "38.00", true));
        repository.save(pizzaTime);

        Restaurant seaCafe = restaurant("osm-4939634221", "Sea Cafe", "11:00", "00:00",
                32.2224212, 35.2403028, Cuisine.SEAFOOD);
        seaCafe.addMenuItem(item("osm-4939634221-1", "Fried fish plate", "55.00", true));
        seaCafe.addMenuItem(item("osm-4939634221-2", "Shrimp plate", "70.00", false));
        repository.save(seaCafe);

        Restaurant heartAttack = restaurant("osm-9897934617", "Heart Attack Burger", "16:00", "23:00",
                32.2234638, 35.2397614, Cuisine.BURGER);
        heartAttack.addMenuItem(item("osm-9897934617-1", "Double burger", "35.00", true));
        heartAttack.addMenuItem(item("osm-9897934617-2", "Fries", "10.00", true));
        repository.save(heartAttack);

        Restaurant altariq = restaurant("osm-4939629822", "Altariq", "07:00", "23:00",
                32.2243266, 35.2439182, Cuisine.CAFE);
        altariq.addMenuItem(item("osm-4939629822-1", "Arabic coffee", "5.00", true));
        altariq.addMenuItem(item("osm-4939629822-2", "Donut", "6.00", true));
        repository.save(altariq);

        Restaurant khamees = restaurant("osm-5347793623", "Khamees", "07:00", "01:00",
                32.2243506, 35.2434154, Cuisine.SHAWARMA);
        khamees.addMenuItem(item("osm-5347793623-1", "Shawarma sandwich", "12.00", true));
        khamees.addMenuItem(item("osm-5347793623-2", "Shawarma plate", "30.00", true));
        repository.save(khamees);

        Restaurant rosemary = restaurant("osm-4516173389", "Rosemary (Hayat Nablus)", "10:00", "00:00",
                32.2275038, 35.2372125, Cuisine.GRILL);
        rosemary.addMenuItem(item("osm-4516173389-1", "Mixed grill", "55.00", true));
        rosemary.addMenuItem(item("osm-4516173389-2", "Kebab", "45.00", true));
        repository.save(rosemary);

        Restaurant turkishPotato = restaurant("osm-9330595717", "Turkish Potato", "10:00", "00:00",
                32.2244247, 35.2467408, Cuisine.OTHER);
        turkishPotato.addMenuItem(item("osm-9330595717-1", "Stuffed potato (kumpir)", "18.00", true));
        repository.save(turkishPotato);

        return repository;
    }

    private static Restaurant restaurant(String id, String name, String opensAt, String closesAt,
                                         double latitude, double longitude, Cuisine cuisine) {
        OpeningHours hours = new OpeningHours(LocalTime.parse(opensAt), LocalTime.parse(closesAt));
        return new Restaurant(id, name, hours, new Location(latitude, longitude), cuisine);
    }

    private static MenuItem item(String id, String name, String price, boolean available) {
        return new MenuItem(id, name, new BigDecimal(price), available);
    }
}
