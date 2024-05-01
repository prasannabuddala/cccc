package servlets;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


public class ProductOrdersDAL {
    private DatabaseConnection dbConnection;

    public ProductOrdersDAL() {
        dbConnection = new DatabaseConnection();
    }

    public boolean insertIntoProductOrder(int orderid, List<CartItem> cartitems) {
        // Initialize the productorders list
        List<ProductOrder> productorders = new ArrayList<>();

        for (CartItem item : cartitems) {
            Products product = new GetProductsDAL().getProductById(item.getPid());
            ProductOrder po = new ProductOrder(orderid, item.getPid(), item.getQuantity(), product.getPprice());
            productorders.add(po);
        }

        try (Connection cn = dbConnection.getConnection()) {
            // Prepare the array to pass to the stored procedure
            Array array = cn.createArrayOf("productorder_type", productorders.toArray());

            // Call the stored procedure with the array parameter
            CallableStatement cst = cn.prepareCall("{call insert_product_orders(?)}");
            cst.setArray(1, array);
            cst.execute();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return false; // Return false if there is an SQL exception
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for IOException
            return false; // Return false if there is an IO exception
        }

        return true; // Return true if insertion is successful
    }

}
