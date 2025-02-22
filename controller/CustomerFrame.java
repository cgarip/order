package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import util.DatabaseUtil;

public class CustomerFrame extends JFrame {
    private JTextField customerNameField, phoneField, emailField;
    private JButton addButton, updateButton, deleteButton, backButton;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private String loggedInUsername;

    public CustomerFrame(String username) {
    	this.loggedInUsername = username;
        setTitle("Customer Management");
        setSize(500, 400);  // Set frame size
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set layout to null (absolute positioning)
        getContentPane().setLayout(null);

        // Initialize fields and buttons
        customerNameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        backButton = new JButton("Back to Main Menu");


        // Set bounds for components
        JLabel customerNameLabel = new JLabel("Customer Name:");
        customerNameLabel.setBounds(30, 30, 120, 25);
        customerNameField.setBounds(160, 30, 150, 25);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 70, 120, 25);
        phoneField.setBounds(160, 70, 150, 25);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 110, 120, 25);
        emailField.setBounds(160, 110, 150, 25);

        addButton.setBounds(126, 150, 60, 30);
        updateButton.setBounds(184, 150, 77, 30);
        deleteButton.setBounds(261, 150, 77, 30);
        backButton.setBounds(126, 180, 212, 30);  // Positioning "Back to Main Menu" button


        // Add components to the frame
        getContentPane().add(customerNameLabel);
        getContentPane().add(customerNameField);
        getContentPane().add(phoneLabel);
        getContentPane().add(phoneField);
        getContentPane().add(emailLabel);
        getContentPane().add(emailField);
        getContentPane().add(addButton);
        getContentPane().add(updateButton);
        getContentPane().add(deleteButton);
        getContentPane().add(backButton);


        // Customer Table Setup
        String[] columnNames = {"Customer ID", "Customer Name", "Phone", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Overriding isCellEditable method to make the table non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Disable cell editing
            }
        };
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBounds(30, 230, 400, 100);
        getContentPane().add(scrollPane);

        // Add button actions (implement CRUD logic)
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateCustomer();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteCustomer();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Close current window and go back to main menu
                dispose();  // Close current frame
                new MainMenuFrame(loggedInUsername).setVisible(true);  // Assuming MainMenuFrame is your main menu frame class
            }
        });
        
        // Load customer data into table
        loadCustomerData();

        // Add ListSelectionListener to update fields when a row is selected
        customerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow != -1) {
                    int customerId = (int) customerTable.getValueAt(selectedRow, 0);
                    String customerName = (String) customerTable.getValueAt(selectedRow, 1);
                    String phone = (String) customerTable.getValueAt(selectedRow, 2);
                    String email = (String) customerTable.getValueAt(selectedRow, 3);

                    // Set values in the fields
                    customerNameField.setText(customerName);
                    phoneField.setText(phone);
                    emailField.setText(email);
                }
            }
        });
    }

    private void addCustomer() {
        String customerName = customerNameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (customerName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // Check if the customer already exists in the database
        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM Customers WHERE Name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, customerName);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // If a customer with the same name already exists
                JOptionPane.showMessageDialog(this, "Customer with this name already exists.");
                return;  // Don't proceed with insertion
            }

            // Insert customer into the database
            String insertQuery = "INSERT INTO Customers (Name, Phone, Email) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, customerName);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer added successfully.");
            loadCustomerData();  // Reload customer data after adding
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
        }
    }


    private void updateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to update.");
            return;
        }

        // Get the customer details from the table
        int customerId = (int) customerTable.getValueAt(selectedRow, 0);
        String customerName = customerNameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (customerName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // Check if another customer with the same name exists (excluding the current customer)
        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM Customers WHERE Name = ? AND Id != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, customerName);
            checkStmt.setInt(2, customerId);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // If a customer with the same name already exists
                JOptionPane.showMessageDialog(this, "Customer with this name already exists.");
                return;  // Don't proceed with the update
            }

            // Update customer in the database
            String updateQuery = "UPDATE Customers SET Name = ?, Phone = ?, Email = ? WHERE Id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, customerName);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setInt(4, customerId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer updated successfully.");
            loadCustomerData();  // Reload customer data after update
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
        }
    }


    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
            return;
        }

        // Get the customer ID from the selected row
        int customerId = (int) customerTable.getValueAt(selectedRow, 0);

        // Confirm deletion
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?");
        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                String deleteQuery = "DELETE FROM Customers WHERE Id = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setInt(1, customerId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
                loadCustomerData();  // Reload customer data after deletion
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Please delete orders first.  ");
            }
        }
    }

    private void loadCustomerData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Load customer data from the database
        try (Connection conn = DatabaseUtil.getConnection()) {
            String selectQuery = "SELECT * FROM Customers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);

            while (rs.next()) {
                // Add each row to the table
                int id = rs.getInt("Id");
                String name = rs.getString("Name");
                String phone = rs.getString("Phone");
                String email = rs.getString("Email");

                tableModel.addRow(new Object[]{id, name, phone, email});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while loading customer data.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerFrame("test").setVisible(true));
    }
}
