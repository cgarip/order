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

public class RegisterFrame extends JFrame {
    private JTextField usernameField, phoneField, emailField;
    private JPasswordField passwordField;
    private JButton registerButton, backToLoginButton;

    public RegisterFrame() {
        setTitle("Employee Registration");
        setSize(300, 350);  // Set the desired frame size
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set the layout to null (absolute positioning)
        getContentPane().setLayout(null);

        // Initialize fields and buttons
        usernameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        registerButton = new JButton("Register");
        backToLoginButton = new JButton("Back to Login");

        // Set bounds (x, y, width, height) for each component
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 80, 25);
        usernameField.setBounds(120, 30, 150, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 80, 25);  // Adjusted position for password field
        passwordField.setBounds(120, 70, 150, 25);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 110, 80, 25);
        phoneField.setBounds(120, 110, 150, 25);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 150, 80, 25);
        emailField.setBounds(120, 150, 150, 25);

        // Adjusted button sizes and positions to avoid overlapping
        backToLoginButton.setBounds(10, 200, 150, 30);  // Increased width to fit the text
        registerButton.setBounds(170, 200, 100, 30);  // Moved Register button down to avoid overlap

        // Add components to the frame
        getContentPane().add(usernameLabel);
        getContentPane().add(usernameField);
        getContentPane().add(passwordLabel);
        getContentPane().add(passwordField);
        getContentPane().add(phoneLabel);
        getContentPane().add(phoneField);
        getContentPane().add(emailLabel);
        getContentPane().add(emailField);
        getContentPane().add(registerButton);
        getContentPane().add(backToLoginButton);

        // Register button action
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerEmployee();
            }
        });

        // Back to Login button action
        backToLoginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backToLogin();
            }
        });
    }

    private void registerEmployee() {
        String username = usernameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // Validate data
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            JOptionPane.showMessageDialog(this, "Username must be between 3 and 50 characters.");
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }

        // Check if the username already exists
        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkQuery = "SELECT * FROM Employees WHERE UserName = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose another one.");
                return;
            }

            // Insert the new employee
            String insertQuery = "INSERT INTO Employees (UserName, Phone, Email, Password) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, username);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Registration successful!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email validation (you can improve this using regular expressions)
        return email.contains("@") && email.contains(".");
    }

    private void backToLogin() {
        new LoginFrame().setVisible(true);  // Open the Login Frame
        dispose();  // Close the register frame
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}
