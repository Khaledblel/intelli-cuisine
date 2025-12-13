package com.khaled.intellicuisine.models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Recipe implements Serializable {
    private String id;
    private String title;
    private String difficulty;
    private int timeMinutes;
    private String servings;
    private List<Map<String, Object>> ingredients;
    private List<Map<String, Object>> steps;
    private List<String> tips;
    private String imageUrl;
    private long createdAt;

    public Recipe() { }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTimeMinutes() { return timeMinutes; }
    public void setTimeMinutes(int timeMinutes) { this.timeMinutes = timeMinutes; }

    public String getServings() { return servings; }
    public void setServings(String servings) { this.servings = servings; }

    public List<Map<String, Object>> getIngredients() { return ingredients; }
    public void setIngredients(List<Map<String, Object>> ingredients) { this.ingredients = ingredients; }

    public List<Map<String, Object>> getSteps() { return steps; }
    public void setSteps(List<Map<String, Object>> steps) { this.steps = steps; }

    public List<String> getTips() { return tips; }
    public void setTips(List<String> tips) { this.tips = tips; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
