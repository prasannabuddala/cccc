package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CheckoutController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String cartItemsJson = request.getParameter("cartitems");
		Gson gson = new Gson();
		CartItem[] cartItemss = gson.fromJson(cartItemsJson, CartItem[].class);

		List<CartItem> cartItems = Arrays.asList(cartItemss);
		if (cartItems.isEmpty()) {
			JsonObject jsonResponse = new JsonObject();
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("error", "Cart is empty. Add items to cart before checkout.");
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(jsonResponse.toString());
			out.flush();
			return;
		}
		int customerId = 123;

		int orderId;

		double orderTotal = calculateTotalOrderCost(cartItems);
		System.out.println(orderTotal);

		OrdersDAL orders = new OrdersDAL();
		orderId = orders.insertOrder(orderTotal, customerId);
		if (orderId != 0)
			System.out.println("inserted into orders");

		boolean b2 = false;
		ProductOrdersDAL productorders = new ProductOrdersDAL();
		b2 = productorders.insertIntoProductOrder(orderId, cartItems);
		// for (CartItem item : cartItems) {
		// Products product = new GetProductsDAL().getProductById(item.getPid());
		// b2 = productorders.insertProductOrder(orderId, item.getPid(), item.getQuantity(), product.getPprice());
		// if (!b2) {
		// System.out.println("could not insert into productorders");
		// break;
		// }
		// }
		JsonObject jsonResponse = new JsonObject();
		if (orderId != 0 && b2) {
			HttpSession session = request.getSession();
			session.removeAttribute("cartitems");
			jsonResponse.addProperty("success", true);
			jsonResponse.addProperty("totalAmount", orderTotal);
		} else {
			jsonResponse.addProperty("success", false);
		}
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(jsonResponse.toString());
		out.flush();

	}

	private double calculateTotalOrderCost(List<CartItem> cartItems) {
		double totalCost = 0;
		for (CartItem item : cartItems) {
			Products product = new GetProductsDAL().getProductById(item.getPid());
			if (product != null) {
				totalCost += item.getQuantity() * product.getPprice();
			}
		}
		return totalCost;
	}

}
