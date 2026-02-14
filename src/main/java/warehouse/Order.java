package warehouse;

public class Order {
    private int id;
    private String productName;
    private int quantity;
    private double priceNetto;
    private double priceBrutto;
    private String orderNumber;
    private String orderDate;

    public Order(int id, String productName, int quantity, double priceNetto, double priceBrutto, String orderNumber, String orderDate) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.priceNetto = priceNetto;
        this.priceBrutto = priceBrutto;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
    }

    //GETTERY
    public int getId() { return id; }
    public String getProductName() {return productName;}
    public int getQuantity() { return quantity; }
    public double getPriceNetto() { return priceNetto; }
    public double getPriceBrutto() { return priceBrutto; }
    public String getOrderNumber() { return orderNumber; }
    public String getOrderDate() { return orderDate; }
}
