package controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuFrame extends JFrame {
    private JButton editEmployeeButton, customerCrudButton, orderPageButton, productPageButton, logoutButton; // Added logoutButton
    private String loggedInUsername;

    public MainMenuFrame(String username) {
        this.loggedInUsername = username;

        setTitle("Main Menu");
        setSize(300, 300);  // Adjusted size to fit the new button
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout to null for absolute positioning
        getContentPane().setLayout(null);

        // Initialize buttons
        editEmployeeButton = new JButton("Edit Employee");
        customerCrudButton = new JButton("Edit Customer");
        orderPageButton = new JButton("Order");
        productPageButton = new JButton("Edit Product");
        logoutButton = new JButton("Logout");  // New logout button

        // Set bounds (x, y, width, height) for each button
        editEmployeeButton.setBounds(50, 30, 200, 30);
        customerCrudButton.setBounds(50, 70, 200, 30);
        orderPageButton.setBounds(50, 152, 200, 30);
        productPageButton.setBounds(50, 111, 200, 30);  // Adjusted position for the new button
        logoutButton.setBounds(50, 193, 200, 30);  // Position for the logout button

        // Add buttons to the frame
        getContentPane().add(editEmployeeButton);
        getContentPane().add(customerCrudButton);
        getContentPane().add(productPageButton);
        getContentPane().add(orderPageButton);
        getContentPane().add(logoutButton);  // Add logout button

        // Action listeners for each button
        editEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEmployeeFrame();
            }
        });

        customerCrudButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openCustomerCrudFrame();
            }
        });

        orderPageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openOrderPage();
            }
        });

        productPageButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {
                openProductPage();
            }
        });

        logoutButton.addActionListener(new ActionListener() {  // Action listener for logout button
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
    }

    private void openEmployeeFrame() {
        // Open the Employee Frame to edit employee details
        new EmployeeFrame(loggedInUsername).setVisible(true);
        dispose();  // Close the main menu frame
    }

    private void openCustomerCrudFrame() {
        // Open the Customer CRUD Frame
        new CustomerFrame(loggedInUsername).setVisible(true);
        dispose();  // Close the main menu frame
    }

    private void openOrderPage() {
        // Open the Order Page Frame
        new OrderFrame(loggedInUsername).setVisible(true);
        dispose();  // Close the main menu frame
    }

    private void openProductPage() {
        // Open the Product Page Frame
        new ProductFrame(loggedInUsername).setVisible(true);
        dispose();  // Close the main menu frame
    }

    private void logout() {
        // Return to the login screen
        new LoginFrame().setVisible(true);  // Open the login frame
        dispose();  // Close the main menu frame
    }

    public static void main(String[] args) {
        // Test with a sample username
        SwingUtilities.invokeLater(() -> new MainMenuFrame("sampleUser").setVisible(true));
    }
}
