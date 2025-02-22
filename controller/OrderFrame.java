package controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import util.DatabaseUtil;

public class OrderFrame extends JFrame {
    private JTable orderTable;
    private JTextField quantityField;
    private JComboBox<String> productDropdown, customerDropdown;
    private String loggedInUsername;

    public OrderFrame(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        setTitle("Order Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Header Panel (Login info)
        JPanel headerPanel = new JPanel();
        JLabel loginLabel = new JLabel("Logged in as: " + loggedInUsername);
        headerPanel.add(loginLabel);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Control Panel (Add, Edit, Delete, Export, Chart, etc.)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JLabel productLabel = new JLabel("Product:");
        productDropdown = new JComboBox<>();
        loadProductData();
        
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityField = new JTextField(5);
        InputVerifier numberVerifier = new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField textField = (JTextField) input;
                String text = textField.getText().trim();
                // Check if the text is empty or a valid integer
                if (text.isEmpty()) {
                    return true;  // Allow empty field for validation if required
                }

                try {
                    Integer.parseInt(text);  // Try parsing as an integer
                    return true;  // Valid number
                } catch (NumberFormatException e) {
                    // Show an error message if not a valid number
                    JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                    return false;  // Invalid input
                }
            }
        };

        // Set the InputVerifier to the quantityField
        quantityField.setInputVerifier(numberVerifier);
        
        JLabel customerLabel = new JLabel("Customer:");
        customerDropdown = new JComboBox<>();
        loadCustomerData();

        // Adding ActionListener to update table when customer is selected
        customerDropdown.addActionListener(e -> loadOrderData());

        JButton addButton = new JButton("Add Order");
        addButton.addActionListener(e -> addOrder());
        
        JButton editButton = new JButton("Edit Order");
        editButton.addActionListener(e -> editOrder());

        JButton deleteButton = new JButton("Delete Order");
        deleteButton.addActionListener(e -> deleteOrder());

        JButton exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(e -> exportToExcel());

        JButton chartButton = new JButton("Show Chart");
        chartButton.addActionListener(e -> showBarChart());

        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> backToMainMenu());
        
        controlPanel.add(productLabel);
        controlPanel.add(productDropdown);
        controlPanel.add(quantityLabel);
        controlPanel.add(quantityField);
        controlPanel.add(customerLabel);
        controlPanel.add(customerDropdown);
        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(exportButton);
        controlPanel.add(chartButton);
        controlPanel.add(backButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Order Table Panel (Move it to the bottom)
        String[] columns = {"Order ID", "Customer Name", "Product Name", "Quantity"};
        orderTable = new JTable(new DefaultTableModel(
            new Object[]{"Order ID", "Customer Name", "Product Name", "Quantity"}, 0));  // Initial empty data (0 rows)
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.SOUTH);  // Place the table at the bottom

        setContentPane(panel);
        loadOrderData();
    }

    // Updated method to filter orders by customer
    private void loadOrderData() {
        String selectedCustomer = (String) customerDropdown.getSelectedItem();

        try {
            Connection conn = DatabaseUtil.getConnection();
            String query = "SELECT Orders.id, Customers.Name, Products.ProductName, Orders.Quantity " +
                           "FROM Orders " +
                           "JOIN Customers ON Orders.CustomerID = Customers.ID " +
                           "JOIN Products ON Orders.ProductID = Products.ID " +
                           "WHERE Customers.Name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, selectedCustomer);
            ResultSet rs = stmt.executeQuery();

            // Ensure the JTable has a DefaultTableModel
            DefaultTableModel model = (DefaultTableModel) orderTable.getModel();

            // Clear the existing rows before adding new ones
            model.setRowCount(0);

            while (rs.next()) {
                // Fetch data from ResultSet
                int orderId = rs.getInt("id");
                String customerName = rs.getString("Name");
                String productName = rs.getString("ProductName");
                int quantity = rs.getInt("Quantity");

                // Add the data to the table model
                model.addRow(new Object[]{orderId, customerName, productName, quantity});
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProductData() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT ProductName FROM Products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                productDropdown.addItem(rs.getString("ProductName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerData() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT Name FROM Customers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                customerDropdown.addItem(rs.getString("Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrder() {
        String selectedProduct = (String) productDropdown.getSelectedItem();
        String selectedCustomer = (String) customerDropdown.getSelectedItem();
        int quantity = 0;  // Default value

        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            // quantity remains 0
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Query to get CustomerId and EmployeeId from the respective tables
            String getCustomerIdQuery = "SELECT Id FROM Customers WHERE Name = ? LIMIT 1";
            PreparedStatement customerStmt = conn.prepareStatement(getCustomerIdQuery);
            customerStmt.setString(1, selectedCustomer);
            ResultSet customerRs = customerStmt.executeQuery();
            int customerId = -1;
            if (customerRs.next()) {
                customerId = customerRs.getInt("Id");
            }

            // Assuming the logged-in user is the employee handling the order, so we can get the EmployeeId from the loggedInUsername
            String getEmployeeIdQuery = "SELECT Id FROM Employees WHERE Username = ? LIMIT 1";
            PreparedStatement employeeStmt = conn.prepareStatement(getEmployeeIdQuery);
            employeeStmt.setString(1, loggedInUsername);
            ResultSet employeeRs = employeeStmt.executeQuery();
            int employeeId = -1;
            if (employeeRs.next()) {
                employeeId = employeeRs.getInt("Id");
            }

            // Query to get ProductId
            String getProductIdQuery = "SELECT Id FROM Products WHERE ProductName = ? LIMIT 1";
            PreparedStatement productStmt = conn.prepareStatement(getProductIdQuery);
            productStmt.setString(1, selectedProduct);
            ResultSet productRs = productStmt.executeQuery();
            int productId = -1;
            if (productRs.next()) {
                productId = productRs.getInt("Id");
            }

            // Insert order into the Orders table
            String insertQuery = "INSERT INTO Orders (CustomerId, EmployeeId, ProductId, Quantity) " +
                                 "VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, customerId);
            insertStmt.setInt(2, employeeId);
            insertStmt.setInt(3, productId);
            insertStmt.setInt(4, quantity);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order added successfully.");
            loadOrderData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void editOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to edit.");
            return;
        }

        int orderId = (int) orderTable.getValueAt(selectedRow, 0);
        String selectedProduct = (String) productDropdown.getSelectedItem();
        int quantity = 0;  // Default value

        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            // quantity remains 0
        }
        String selectedCustomer = (String) customerDropdown.getSelectedItem();

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Get the CustomerId and ProductId based on the selected names
            String getCustomerIdQuery = "SELECT Id FROM Customers WHERE Name = ? LIMIT 1";
            PreparedStatement customerStmt = conn.prepareStatement(getCustomerIdQuery);
            customerStmt.setString(1, selectedCustomer);
            ResultSet customerRs = customerStmt.executeQuery();
            int customerId = -1;
            if (customerRs.next()) {
                customerId = customerRs.getInt("Id");
            }

            String getProductIdQuery = "SELECT Id FROM Products WHERE ProductName = ? LIMIT 1";
            PreparedStatement productStmt = conn.prepareStatement(getProductIdQuery);
            productStmt.setString(1, selectedProduct);
            ResultSet productRs = productStmt.executeQuery();
            int productId = -1;
            if (productRs.next()) {
                productId = productRs.getInt("Id");
            }

            // Assuming employeeId is already available, otherwise you can retrieve it similarly
            int employeeId = 1; // Example: use the logged-in employee's ID, or fetch dynamically

            // Update the order
            String updateQuery = "UPDATE Orders " +
                                 "SET CustomerId = ?, ProductId = ?, Quantity = ?, EmployeeId = ? " +
                                 "WHERE Id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, employeeId);
            stmt.setInt(5, orderId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order updated successfully.");
            loadOrderData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete.");
            return;
        }

        int orderId = (int) orderTable.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String deleteQuery = "DELETE FROM Orders WHERE Id = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, orderId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order deleted successfully.");
            loadOrderData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


  
    private String getSelectedCustomerName() {
        if (customerDropdown.getSelectedItem() != null) {
            return customerDropdown.getSelectedItem().toString();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer.");
            return null;
        }
    }

    public void exportToExcel() {
        // Create a new Workbook (Excel file)
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Invoice");

        // Create Title Row and Merge Cells for Invoice Title
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Invoice for Customer");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // Add Company Info
        Row companyInfoRow = sheet.createRow(1);
        companyInfoRow.createCell(0).setCellValue("Company Name: ABC Corporation");
        companyInfoRow.createCell(1).setCellValue("Tax ID: 123456789");
        companyInfoRow.createCell(2).setCellValue("Address: 123 Business St, City, Country");


        // Create the Header for the Table
        Row headerRow = sheet.createRow(3);
        headerRow.createCell(0).setCellValue("Order ID");
        headerRow.createCell(1).setCellValue("Product Name");
        headerRow.createCell(2).setCellValue("Quantity");
        headerRow.createCell(3).setCellValue("Customer Name");
        headerRow.createCell(4).setCellValue("Customer Phone");
        headerRow.createCell(5).setCellValue("Customer Email");
        headerRow.createCell(6).setCellValue("Employee Name");
        headerRow.createCell(7).setCellValue("Employee Phone");
        headerRow.createCell(8).setCellValue("Employee Email");

        // Apply Bold Font to Header Row
        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(boldFont);
        for (int i = 0; i < 9; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // SQL query to fetch data from the InvoiceView for the selected customer
        String query = "SELECT OrderID, ProductName, Quantity, CustomerName, CustomerPhone, CustomerEmail, EmployeeName, EmployeePhone, EmployeeEmail " +
                       "FROM InvoiceView WHERE CustomerName = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set customer ID in the query
            stmt.setString(1, getSelectedCustomerName());
            ResultSet rs = stmt.executeQuery();

            int rowIndex = 4;  // Start from row 4, as rows 0-3 are used for title and headers
            while (rs.next()) {
                Row row = sheet.createRow(rowIndex++);

                // Populate data from ResultSet
                row.createCell(0).setCellValue(rs.getInt("OrderID"));
                row.createCell(1).setCellValue(rs.getString("ProductName"));
                row.createCell(2).setCellValue(rs.getInt("Quantity"));
                row.createCell(3).setCellValue(rs.getString("CustomerName"));
                row.createCell(4).setCellValue(rs.getString("CustomerPhone"));
                row.createCell(5).setCellValue(rs.getString("CustomerEmail"));
                row.createCell(6).setCellValue(rs.getString("EmployeeName"));
                row.createCell(7).setCellValue(rs.getString("EmployeePhone"));
                row.createCell(8).setCellValue(rs.getString("EmployeeEmail"));
            }

            // Add more information or footer if necessary (e.g., payment terms, footer message)
            Row footerRow = sheet.createRow(rowIndex++);
            footerRow.createCell(0).setCellValue("Thank you for your business!");

            // Set column width for better readability
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the data to an Excel file
            try (FileOutputStream fileOut = new FileOutputStream("invoice_for_customer_" + getSelectedCustomerName() + ".xlsx")) {
                workbook.write(fileOut);
                JOptionPane.showMessageDialog(null, "Invoice exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error exporting to Excel: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching invoice data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private int getSelectedCustomerId() {
        String selectedCustomerName = getSelectedCustomerName(); // Get customer name from dropdown
        if (selectedCustomerName == null) {
            return -1; // No customer selected
        }

        String query = "SELECT Id FROM Customers WHERE Name = ? LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, selectedCustomerName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Id"); // Return the customer ID
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching customer ID.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1; // Return -1 if no customer is found
    }

    
    public void showBarChart() {
        int selectedCustomerId = getSelectedCustomerId();
        if (selectedCustomerId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer first.", "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Query to fetch total quantity ordered per product for the selected customer
        String query = "SELECT p.ProductName, SUM(o.Quantity) AS TotalQuantity " +
                       "FROM Orders o " +
                       "JOIN Products p ON o.ProductId = p.Id " +
                       "WHERE o.CustomerId = ? " +
                       "GROUP BY p.ProductName " +
                       "ORDER BY TotalQuantity DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedCustomerId);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String productName = rs.getString("ProductName");
                int totalQuantity = rs.getInt("TotalQuantity");
                dataset.addValue(totalQuantity, "Order Quantity", productName);
                hasData = true;
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No order data available for the selected customer.", "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading chart data.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Total Orders by Product (Customer ID: " + selectedCustomerId + ")",
                "Product",
                "Quantity",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.BLACK);

        // Display the chart in a new window
        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame chartFrame = new JFrame("Order Statistics");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(800, 600);
        chartFrame.add(chartPanel);
        chartFrame.setVisible(true);
    }


    private void backToMainMenu() {
        // Close the current order frame and return to main menu or login screen
        this.dispose();
        new MainMenuFrame(loggedInUsername).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderFrame("test").setVisible(true));

    }
}

