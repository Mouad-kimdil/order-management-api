package com.mouad.order_management_api.product.model;


import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Category() {
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Product> getProducts() {
        return products;
    }
}