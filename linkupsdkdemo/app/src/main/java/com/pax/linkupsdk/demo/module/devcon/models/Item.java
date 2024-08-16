package com.pax.linkupsdk.demo.module.devcon.models;

public class Item {
    public String name;
    public String description;
    public String price;
    public String sku;
    public int imgId;

    public Item(String name, String description, String price, String sku, int imgId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.sku = sku;
        this.imgId = imgId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}