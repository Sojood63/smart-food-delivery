package edu.najah.stu.service;


public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(int id) {
        super("Restaurant not found: " + id);
    }
}