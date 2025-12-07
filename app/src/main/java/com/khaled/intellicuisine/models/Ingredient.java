package com.khaled.intellicuisine.models;
import com.google.firebase.firestore.Exclude;

public class Ingredient {
    private String id;
    private String name;
    private String quantity;

    public Ingredient() { }

    public Ingredient(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
