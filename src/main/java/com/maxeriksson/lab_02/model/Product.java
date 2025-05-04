package com.maxeriksson.lab_02.model;

import lombok.Data;

@Data
public class Product {

    private String name;
    private int price;
    private Category category;
    private boolean isForSale;

    public Product() {}

    public Product(String name, int price, Category category) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.isForSale = true;
    }

    public Product(String name, int price, Category category, boolean isForSale) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.isForSale = isForSale;
    }
}
