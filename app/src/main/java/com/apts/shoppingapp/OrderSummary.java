package com.apts.shoppingapp;

public class OrderSummary {

    private long id;
    private int orderNumber;
    private String orderDate;
    private double total;

    public OrderSummary(long id, int orderNumber, String orderDate, double total) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public double getTotal() {
        return total;
    }
}
