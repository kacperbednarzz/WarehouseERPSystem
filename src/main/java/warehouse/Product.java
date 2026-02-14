package warehouse;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private double priceNetto;
    private double priceBrutto;
    private int supplierId;
    private String supplierName;
    private String orderNumber;

    public Product(int id, String name, int quantity, double priceNetto, double priceBrutto, int supplierId, String supplierName, String orderNumber) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.priceNetto = priceNetto;
        this.priceBrutto = priceBrutto;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.orderNumber = orderNumber;
    }

    // GETTERY
    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPriceNetto() { return priceNetto; }
    public double getPriceBrutto() { return priceBrutto; }
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getOrderNumber() { return orderNumber; }

    // SETTERY
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPriceNetto(double priceNetto) { this.priceNetto = priceNetto; }
    public void setPriceBrutto(double priceBrutto) { this.priceBrutto = priceBrutto; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}