package edu.najah.stu.repository;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;

import java.math.BigDecimal;

/**
 * Builds an in-memory repository filled with demo restaurants and menus.
 */
public final class SampleData {

    private SampleData() {
    }

    /**
     * Creates a repository containing open and closed demo restaurants.
     *
     * @return a repository filled with sample data
     */
    public static InMemoryRestaurantRepository createRepository() {
        InMemoryRestaurantRepository repo = new InMemoryRestaurantRepository();

        Restaurant grill = new Restaurant(1, "Al-Quds Grill", true, new BigDecimal("20"), 5);
        grill.addMenuItem(new MenuItem(1, "Shawarma", new BigDecimal("15.50"), true));
        grill.addMenuItem(new MenuItem(2, "Falafel", new BigDecimal("8"), false));
        grill.addMenuItem(new MenuItem(3, "Hummus", new BigDecimal("12"), true));
        grill.addMenuItem(new MenuItem(4, "Mixed Grill", new BigDecimal("45"), true));
        grill.addMenuItem(new MenuItem(5, "Kebab Plate", new BigDecimal("38"), true));
        repo.save(grill);

        Restaurant pizza = new Restaurant(2, "Nablus Pizza", true, new BigDecimal("25"), 7);
        pizza.addMenuItem(new MenuItem(1, "Margherita", new BigDecimal("30"), true));
        pizza.addMenuItem(new MenuItem(2, "Pepperoni", new BigDecimal("35"), true));
        pizza.addMenuItem(new MenuItem(3, "Vegetarian Pizza", new BigDecimal("32"), true));
        pizza.addMenuItem(new MenuItem(4, "Garlic Bread", new BigDecimal("10"), true));
        pizza.addMenuItem(new MenuItem(5, "Four Cheese", new BigDecimal("40"), false));
        repo.save(pizza);

        Restaurant burger = new Restaurant(3, "Burger House", true, new BigDecimal("22"), 6);
        burger.addMenuItem(new MenuItem(1, "Classic Burger", new BigDecimal("25"), true));
        burger.addMenuItem(new MenuItem(2, "Cheese Burger", new BigDecimal("28"), true));
        burger.addMenuItem(new MenuItem(3, "Chicken Burger", new BigDecimal("27"), true));
        burger.addMenuItem(new MenuItem(4, "French Fries", new BigDecimal("9"), true));
        burger.addMenuItem(new MenuItem(5, "Onion Rings", new BigDecimal("11"), false));
        repo.save(burger);

        Restaurant sweets = new Restaurant(4, "Knafeh Nabulsiyeh", true, new BigDecimal("15"), 4);
        sweets.addMenuItem(new MenuItem(1, "Knafeh Portion", new BigDecimal("14"), true));
        sweets.addMenuItem(new MenuItem(2, "Baklava", new BigDecimal("18"), true));
        sweets.addMenuItem(new MenuItem(3, "Qatayef", new BigDecimal("12"), true));
        sweets.addMenuItem(new MenuItem(4, "Basbousa", new BigDecimal("10"), false));
        repo.save(sweets);

        Restaurant sushi = new Restaurant(5, "Sakura Sushi", true, new BigDecimal("40"), 8);
        sushi.addMenuItem(new MenuItem(1, "Salmon Roll", new BigDecimal("42"), true));
        sushi.addMenuItem(new MenuItem(2, "Tuna Nigiri", new BigDecimal("36"), true));
        sushi.addMenuItem(new MenuItem(3, "Miso Soup", new BigDecimal("15"), true));
        repo.save(sushi);

        repo.save(new Restaurant(6, "Closed Place", false, new BigDecimal("20"), 5));
        repo.save(new Restaurant(7, "Night Shawarma (Closed)", false, new BigDecimal("18"), 5));

        return repo;
    }
}