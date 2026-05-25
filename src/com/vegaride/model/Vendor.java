package com.vegaride.model;

/**
 * Encapsulates the details of a service provider (Bike Renting or Bike Mechanic).
 */
public class Vendor {
    private String name;
    private String contact;
    private String state;
    private String city;
    private double basePrice;
    private double distanceKm; // How far they are or will travel
    private String serviceType; // "Rent" or "Mechanic"
    private double rating;
    private String description;

    // Constructors
    public Vendor() {}

    public Vendor(String name, String contact, String state, String city, double basePrice, double distanceKm, String serviceType, double rating, String description) {
        this.name = name;
        this.contact = contact;
        this.state = state;
        this.city = city;
        this.basePrice = basePrice;
        this.distanceKm = distanceKm;
        this.serviceType = serviceType;
        this.rating = rating;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Converts vendor properties into a simple clean JSON-like structure manually, 
     * adhering strictly to basic Java string formatting without dynamic libraries.
     */
    public String toJsonString() {
        return "{" +
                "\"name\":\"" + escapeJson(name) + "\"," +
                "\"contact\":\"" + escapeJson(contact) + "\"," +
                "\"state\":\"" + escapeJson(state) + "\"," +
                "\"city\":\"" + escapeJson(city) + "\"," +
                "\"price\":" + basePrice + "," +
                "\"distance\":" + String.format("%.1f", distanceKm) + "," +
                "\"serviceType\":\"" + escapeJson(serviceType) + "\"," +
                "\"rating\":" + rating + "," +
                "\"description\":\"" + escapeJson(description) + "\"" +
                "}";
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"");
    }
}
