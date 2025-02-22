package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import util.DatabaseUtil;

public class EmployeeFrame extends JFrame {
    private JTextField usernameField, phoneField, emailField;
    private JPasswordField passwordField;
    private JButton updateButton, deleteButton, backButton; // New button for going back
    private String loggedInUsername;

    public EmployeeFrame(String username) {
        this.loggedInUsername = username;

        setTitle("Employee Dashboard");
        setSize(400, 350);  // Increase size to fit new button
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set the layout
        getContentPane().setLayout(null);

        // Initialize fields and buttons
        usernameField = new JTextField(username);
        phoneField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();  // Initialize password field
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        backButton = new JButton("Back to Main Menu"); // Back button

        // Set bounds for components
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 80, 25);
        usernameField.setBounds(120, 30, 150, 25);
        usernameField.setEditable(false);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 80, 25);
        passwordField.setBounds(120, 70, 150, 25);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 110, 80, 25);
        phoneField.setBounds(120, 110, 150, 25);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 150, 80, 25);
        emailField.setBounds(120, 150, 150, 25);

        updateButton.setBounds(30, 200, 100, 30);
        deleteButton.setBounds(150, 200, 100, 30);
        backButton.setBounds(30, 240, 220, 30); // Position for back button

        // Add components to the frame
        getContentPane().add(usernameLabel);
        getContentPane().add(usernameField);
        getContentPane().add(passwordLabel);
        getContentPane().add(passwordField);  // Add password field to frame
        getContentPane().add(phoneLabel);
        getContentPane().add(phoneField);
        getContentPane().add(emailLabel);
        getContentPane().add(emailField);
        getContentPane().add(updateButton);
        getContentPane().add(deleteButton);
        getContentPane().add(backButton);  // Add back button

        // Load employee details from the database
        loadEmployeeDetails();

        // Add action listener for update button
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEmployee();
            }
        });

        // Add action listener for delete button
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteEmployee();
            }
        });

        // Action listener for back button
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();  // Close the current frame
                new MainMenuFrame(loggedInUsername).setVisible(true);  // Pass username to the MainMenuFrame
            }
        });
    }

    private void loadEmployeeDetails() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT * FROM Employees WHERE UserName = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, loggedInUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                phoneField.setText(rs.getString("Phone"));
                emailField.setText(rs.getString("Email"));
                // No need to pre-fill password for security reasons
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while loading your details.");
        }
    }

    private void updateEmployee() {
        String password = new String(passwordField.getPassword());  // Get the password from the password field
        String phone = phoneField.getText();
        String email = emailField.getText();

        // Validate inputs
        if (phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone and email cannot be empty.");
            return;
        }

        // If password is entered, update the password
        String updateQuery = "UPDATE Employees SET Phone = ?, Email = ?";
        if (!password.isEmpty()) {
            updateQuery += ", Password = ?";
        }
        updateQuery += " WHERE UserName = ?";

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, phone);
            stmt.setString(2, email);
            if (!password.isEmpty()) {
                stmt.setString(3, password);  // Add password parameter if it's provided
                stmt.setString(4, loggedInUsername);
            } else {
                stmt.setString(3, loggedInUsername);  // Skip password if not entered
            }
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Details updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while updating your details.");
        }
    }

    private void deleteEmployee() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete your account?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Check if the employee has any orders in the database
            try (Connection conn = DatabaseUtil.getConnection()) {
                String checkOrderQuery = "SELECT * FROM Orders WHERE EmployeeId = (SELECT Id FROM Employees WHERE UserName = ?)";
                PreparedStatement stmt = conn.prepareStatement(checkOrderQuery);
                stmt.setString(1, loggedInUsername);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "You cannot delete your account as there are orders associated with it.");
                    return;
                }

                // No orders associated, proceed with deletion
                String deleteQuery = "DELETE FROM Employees WHERE UserName = ?";
                stmt = conn.prepareStatement(deleteQuery);
                stmt.setString(1, loggedInUsername);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Your account has been deleted.");
                dispose();  // Close the EmployeeFrame
                new LoginFrame().setVisible(true);  // Open the login screen again
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while deleting your account.");
            }
        }
    }

    public static void main(String[] args) {
        // For testing, create a sample employee
        SwingUtilities.invokeLater(() -> new EmployeeFrame("sampleUser").setVisible(true));
    }
}
