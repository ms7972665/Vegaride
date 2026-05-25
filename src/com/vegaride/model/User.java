package com.vegaride.model;

/**
 * Encapsulates the user credentials and profile details.
 * Demonstrates Object-Oriented principles (encapsulation).
 */
public class User {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String vehicle;

    // Default constructor
    public User() {
        this.vehicle = "None";
    }

    // Parameterized constructor for registration
    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.vehicle = "None"; // Default vehicle profile
    }

    // Constructor with vehicle
    public User(String name, String email, String phone, String password, String vehicle) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.vehicle = (vehicle == null || vehicle.trim().isEmpty()) ? "None" : vehicle;
    }

    // Getters and Setters (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * Serializes user details to a comma-separated format for text file storage.
     */
    public String toFileString() {
        return escape(name) + "," + escape(email) + "," + escape(phone) + "," + escape(password) + "," + escape(vehicle);
    }

    /**
     * Deserializes user details from a comma-separated line.
     */
    public static User fromFileString(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 4) {
            String name = unescape(parts[0]);
            String email = unescape(parts[1]);
            String phone = unescape(parts[2]);
            String password = unescape(parts[3]);
            String vehicle = parts.length > 4 ? unescape(parts[4]) : "None";
            return new User(name, email, phone, password, vehicle);
        }
        return null;
    }

    private static String escape(String val) {
        if (val == null) return "";
        return val.replace(",", "\\comma");
    }

    private static String unescape(String val) {
        if (val == null) return "";
        return val.replace("\\comma", ",");
    }
}
