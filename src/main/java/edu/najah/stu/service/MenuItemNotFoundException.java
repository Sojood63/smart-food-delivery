package edu.najah.stu.service;

/**
 * Thrown when a menu item id is not on a restaurant's menu.
 */
public class MenuItemNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param restaurantId the restaurant that was searched
     * @param itemId       the item id that was not found
     */
    public MenuItemNotFoundException(String restaurantId, String itemId) {
        super("Menu item " + itemId + " is not on the menu of " + restaurantId);
    }
}
