package dao;

import model.Customer;
import java.util.List;

public interface ICustomer {
    void addCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomer(int customerId);
    List<Customer> getAllCustomers();
    Customer getCustomerById(int customerId);
    boolean isCustomerExists(String customerName, int excludeId);
}
