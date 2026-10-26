package com.apts.shoppingapp;

public class Product {

    private long id;
    private String name;
    private String description;
    private double price;
    private String imageName;

    public Product(long id, String name, String description,
                   double price, String imageName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageName = imageName;
    }

    public long getId() {
        return id;
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

    public String getImageName() {
        return imageName;
    }
}