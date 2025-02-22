package controller;

import javax.swing.*;
import java.awt.event.*;
import dao.EmployeeDAO;
import model.Employee;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("Employee Login");
        setSize(300, 250);
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout to null for absolute positioning
        getContentPane().setLayout(null);

        // Initialize fields and buttons
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Set bounds for each component
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 80, 25);
        usernameField.setBounds(120, 30, 150, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 80, 25);
        passwordField.setBounds(120, 70, 150, 25);

        loginButton.setBounds(50, 120, 90, 30);
        registerButton.setBounds(150, 120, 100, 30);

        // Add components to the frame
        getContentPane().add(usernameLabel);
        getContentPane().add(usernameField);
        getContentPane().add(passwordLabel);
        getContentPane().add(passwordField);
        getContentPane().add(loginButton);
        getContentPane().add(registerButton);

        // Login button action
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginEmployee();
            }
        });

        // Register button action (opens RegisterFrame)
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame().setVisible(true);  // Open RegisterFrame
                dispose();  // Close login frame
            }
        });
    }

    private void loginEmployee() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Validate credentials
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
            return;
        }

        // Use DAO to authenticate the employee
        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee employee = employeeDAO.authenticate(username, password);

        if (employee != null) {
            // Successful login, open MainMenuFrame and pass the username
            new MainMenuFrame(username).setVisible(true);
            dispose();  // Close login frame
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials, please try again.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
