package dao;
import java.util.List;

import model.Product;

public interface IProduct {
    boolean addProduct(Product product);
    boolean updateProduct(Product product);
    boolean deleteProduct(int productId);
    List<Product> getAllProducts();
    boolean isProductNameExists(String productName);
}
