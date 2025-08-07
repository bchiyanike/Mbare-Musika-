package com.lionico.mbaremusika;

public class Commodity {
    private String name;
    private String quantity;
    private String usdPrice;
    private String category;
    private boolean isFavorite;
    private String id;

    public Commodity(String name, String quantity, String usdPrice) {
        this.name = name != null ? name : "";
        this.quantity = quantity != null ? quantity : "";
        this.usdPrice = usdPrice != null ? usdPrice : "";
        this.category = categorizeItem(name);
        this.isFavorite = false;
        this.id = generateId(name, quantity);
    }

    public String getName() { return name; }
    public String getQuantity() { return quantity; }
    public String getUsdPrice() { return usdPrice; }
    public String getCategory() { return category; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    private String categorizeItem(String name) {
        if (name == null) return "Other";
        String lowerName = name.toLowerCase();

        if (lowerName.contains("apple") || lowerName.contains("banana") || 
            lowerName.contains("orange") || lowerName.contains("mango")) {
            return "Fruits";
        } else if (lowerName.contains("tomato") || lowerName.contains("cabbage") || 
                   lowerName.contains("onion") || lowerName.contains("potato") ||
                   lowerName.contains("carrot") || lowerName.contains("beans")) {
            return "Vegetables";
        } else if (lowerName.contains("beef") || lowerName.contains("chicken") || 
                   lowerName.contains("pork") || lowerName.contains("fish")) {
            return "Meat";
        } else if (lowerName.contains("egg") || lowerName.contains("milk") || 
                   lowerName.contains("cheese") || lowerName.contains("yogurt")) {
            return "Dairy";
        } else {
            return "Other";
        }
    }

    private String generateId(String name, String quantity) {
        return (name + "_" + quantity).toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z0-9_]", "");
    }
}
