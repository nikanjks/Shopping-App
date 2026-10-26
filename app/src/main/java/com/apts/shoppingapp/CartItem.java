package com.apts.shoppingapp;

public class CartItem {

    private long productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String imageName;

    public CartItem(long productId, String name,
                    String description, double price,
                    int quantity, String imageName) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageName = imageName;
    }

    public long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageName() {
        return imageName;
    }

    public double getSubtotal() {
        return price * quantity;
    }
}