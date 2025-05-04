package com.maxeriksson.lab_02.model;

import lombok.Data;

@Data
public class Category {

    private String name;

    public Category() {}

    public Category(String name) {
        this.name = name;
    }
}
