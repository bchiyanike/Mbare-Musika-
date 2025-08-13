package com.lionico.mbaremusika;

import java.util.Locale;

public class Commodity {
    private String name;
    private String quantity;
    private String price;
    private String id;
    private boolean isFavorite;

    public Commodity(String name, String quantity, String price) {
        this.name = name != null ? name : "";
        this.quantity = quantity != null ? quantity : "";
        this.price = price != null ? price : "";
        this.id = generateId(name, quantity);
        this.isFavorite = false;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id : generateId(name, quantity);
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    private String categorizeItem(String name) {
        if (name == null) return "Other";
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (lowerName.contains("apple") || lowerName.contains("banana") ||
            lowerName.contains("orange") || lowerName.contains("mango")) {
            return "Fruit";
        } else if (lowerName.contains("potato") || lowerName.contains("carrot") ||
                   lowerName.contains("onion") || lowerName.contains("tomato")) {
            return "Vegetable";
        } else if (lowerName.contains("rice") || lowerName.contains("maize") ||
                   lowerName.contains("beans")) {
            return "Grain";
        }
        return "Other";
    }

    private String generateId(String name, String quantity) {
        if (name == null || quantity == null) return "";
        return (name + "_" + quantity).toLowerCase(Locale.ROOT).replaceAll("\\s+", "").replaceAll("[^a-z0-9_]", "");
    }
}
