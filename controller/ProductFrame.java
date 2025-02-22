package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.table.DefaultTableModel;

import util.DatabaseUtil;

public class ProductFrame extends JFrame {
    private JTextField productNameField;
    private JButton addButton, updateButton, deleteButton, backButton;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private String loggedInUsername;

    public ProductFrame(String username) {
        this.loggedInUsername = username;
        setTitle("Product Management");
        setSize(600, 500);  // Set frame size
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set layout to null (absolute positioning)
        getContentPane().setLayout(null);

        // Initialize fields and buttons
        productNameField = new JTextField(20);
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        backButton = new JButton("Back to Main Menu");

        // Set bounds for components
        JLabel productNameLabel = new JLabel("Product Name:");
        productNameLabel.setBounds(30, 70, 120, 25);
        productNameField.setBounds(160, 70, 150, 25);

        addButton.setBounds(126, 150, 60, 30);
        updateButton.setBounds(184, 150, 77, 30);
        deleteButton.setBounds(261, 150, 77, 30);
        backButton.setBounds(160, 193, 150, 30);

        // Add components to the frame
        getContentPane().add(productNameLabel);
        getContentPane().add(productNameField);
        getContentPane().add(addButton);
        getContentPane().add(updateButton);
        getContentPane().add(deleteButton);
        getContentPane().add(backButton);

        // Product Table Setup
        String[] columnNames = {"Product ID", "Product Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Disable cell editing
            }
        };
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(30, 250, 500, 150);
        getContentPane().add(scrollPane);

        // Add button actions (implement CRUD logic)
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateProduct();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteProduct();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backToMainMenu();
            }
        });

        // Add MouseListener to capture row selection
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editProduct(); // When row is clicked, update text field with product name
            }
        });

        // Load product data into table
        loadProductData();
    }

    private void addProduct() {
        String productName = productNameField.getText();

        if (productName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the product name.");
            return;
        }

        // Check if product name already exists
        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductName = ?";
            PreparedStatement stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Product name already exists. Please choose a different name.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while checking product name.");
            return;
        }

        // If no duplicate, proceed with inserting the new product
        try (Connection conn = DatabaseUtil.getConnection()) {
            String insertQuery = "INSERT INTO Products (ProductName) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, productName);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added successfully.");
            loadProductData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
        }
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }

        int productId = (int) productTable.getValueAt(selectedRow, 0);
        String productName = productNameField.getText();

        if (productName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the product name.");
            return;
        }

        // Check if another product with the same name exists (excluding the current product)
        try (Connection conn = DatabaseUtil.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM Products WHERE ProductName = ? AND Id != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, productName);
            checkStmt.setInt(2, productId);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                // If a product with the same name already exists
                JOptionPane.showMessageDialog(this, "Product with this name already exists.");
                return;  // Don't proceed with the update
            }

            // Update product in the database
            String updateQuery = "UPDATE Products SET ProductName = ? WHERE Id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, productName);
            stmt.setInt(2, productId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully.");
            loadProductData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
        }
    }


    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int productId = (int) productTable.getValueAt(selectedRow, 0);

        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?");
        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection()) {
                String deleteQuery = "DELETE FROM Products WHERE Id = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setInt(1, productId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                loadProductData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Please delete order first");
            }
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            return; // No product selected, so do nothing
        }

        // Retrieve the product name from the selected row and display it in the text field
        String selectedProductName = (String) productTable.getValueAt(selectedRow, 1);
        productNameField.setText(selectedProductName);
    }

    private void loadProductData() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String selectQuery = "SELECT * FROM Products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);

            while (rs.next()) {
                int productId = rs.getInt("Id");
                String productName = rs.getString("ProductName");

                tableModel.addRow(new Object[]{productId, productName});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while loading product data.");
        }
    }

    private void backToMainMenu() {
        this.dispose();
        new MainMenuFrame(loggedInUsername).setVisible(true); // Navigate back to Main Menu
    }

    public static void main(String[] args) {
        new ProductFrame("test").setVisible(true);
    }
}
