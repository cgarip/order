package dao;

import java.sql.*;

import util.DatabaseUtil;
import model.Employee;

public class EmployeeDAO {

    public Employee authenticate(String username, String password) {
        String query = "SELECT * FROM Employees WHERE UserName = ? AND Password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Employee(rs.getInt("Id"), rs.getString("UserName"),
                        rs.getString("Phone"), rs.getString("Email"));
            } else {
                return null; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  
    }
}
