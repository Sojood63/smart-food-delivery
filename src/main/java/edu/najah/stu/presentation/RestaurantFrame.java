package edu.najah.stu.presentation;

import edu.najah.stu.domain.MenuItem;
import edu.najah.stu.domain.Restaurant;
import edu.najah.stu.service.RestaurantService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Swing window that shows open restaurants, the menu of the selected
 * restaurant, and the details of the selected menu item.
 */
public class RestaurantFrame extends JFrame {

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private final transient RestaurantService service;
    private final DefaultListModel<Restaurant> restaurantModel = new DefaultListModel<>();
    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final JList<Restaurant> restaurantList = new JList<>(restaurantModel);
    private final JList<MenuItem> menuList = new JList<>(menuModel);
    private final JLabel menuTitle = new JLabel("Menu");
    private final JLabel detailName = new JLabel(" ");
    private final JLabel detailPrice = new JLabel(" ");
    private final JLabel detailRestaurant = new JLabel(" ");
    private final JLabel detailStatus = new JLabel(" ");

    /**
     * Builds the window.
     *
     * @param service service used to read restaurants and menus
     */
    public RestaurantFrame(RestaurantService service) {
        super("Smart Food Delivery - Restaurants");
        this.service = service;
        buildUi();
        loadRestaurants();
        setSize(850, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        restaurantList.setFont(TEXT_FONT);
        restaurantList.setFixedCellHeight(30);
        restaurantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restaurantList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setText("  " + ((Restaurant) value).getName());
                return this;
            }
        });
        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showMenu(restaurantList.getSelectedValue());
            }
        });

        menuList.setFont(TEXT_FONT);
        menuList.setFixedCellHeight(30);
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                MenuItem item = (MenuItem) value;
                setText("  " + item.getName() + "   -   " + item.getPrice().setScale(2) + " NIS");
                return this;
            }
        });
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showItemDetails(menuList.getSelectedValue());
            }
        });

        menuTitle.setFont(TITLE_FONT);
        JPanel left = titledPanel(new JLabel("Open Restaurants"), restaurantList);
        JPanel middle = titledPanel(menuTitle, menuList);

        JPanel center = new JPanel(new GridLayout(1, 3, 10, 0));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        center.add(left);
        center.add(middle);
        center.add(buildDetailsPanel());
        add(center, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadRestaurants());
        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel titledPanel(JLabel title, JList<?> list) {
        title.setFont(TITLE_FONT);
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailsPanel() {
        JLabel title = new JLabel("Item Details");
        title.setFont(TITLE_FONT);

        JPanel info = new JPanel(new GridLayout(8, 1, 0, 4));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        info.add(new JLabel("Name:"));
        detailName.setFont(TITLE_FONT);
        info.add(detailName);
        info.add(new JLabel("Price:"));
        detailPrice.setFont(TITLE_FONT);
        info.add(detailPrice);
        info.add(new JLabel("Restaurant:"));
        info.add(detailRestaurant);
        info.add(new JLabel("Status:"));
        info.add(detailStatus);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(title, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    private void loadRestaurants() {
        restaurantModel.clear();
        menuModel.clear();
        menuTitle.setText("Menu");
        clearDetails();
        for (Restaurant r : service.getOpenRestaurants()) {
            restaurantModel.addElement(r);
        }
    }

    private void showMenu(Restaurant restaurant) {
        menuModel.clear();
        clearDetails();
        if (restaurant == null) {
            return;
        }
        menuTitle.setText(restaurant.getName() + " - Menu");
        List<MenuItem> items = service.getAvailableMenu(restaurant.getId());
        for (MenuItem item : items) {
            menuModel.addElement(item);
        }
    }

    private void showItemDetails(MenuItem item) {
        Restaurant restaurant = restaurantList.getSelectedValue();
        if (item == null || restaurant == null) {
            clearDetails();
            return;
        }
        detailName.setText(item.getName());
        detailPrice.setText(item.getPrice().setScale(2) + " NIS");
        detailRestaurant.setText(restaurant.getName());
        detailStatus.setText(item.isAvailable() ? "Available" : "Unavailable");
    }

    private void clearDetails() {
        detailName.setText(" ");
        detailPrice.setText(" ");
        detailRestaurant.setText(" ");
        detailStatus.setText(" ");
    }
}