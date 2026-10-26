package com.apts.shoppingapp;

public class OrderSummary {

    private long id;
    private String orderDate;
    private double total;

    public OrderSummary(long id, String orderDate, double total) {
        this.id = id;
        this.orderDate = orderDate;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public double getTotal() {
        return total;
    }
}