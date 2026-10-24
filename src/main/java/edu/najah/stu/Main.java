package edu.najah.stu;

import edu.najah.stu.presentation.RestaurantFrame;
import edu.najah.stu.repository.SampleData;
import edu.najah.stu.service.RestaurantService;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        RestaurantService service = new RestaurantService(SampleData.createRepository());
        SwingUtilities.invokeLater(() -> new RestaurantFrame(service).setVisible(true));
    }
}