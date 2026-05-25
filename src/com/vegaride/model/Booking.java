package com.vegaride.model;

/**
 * Encapsulates the details of a booking or emergency help request.
 */
public class Booking {
    private String userEmail;
    private String vendorName;
    private String vendorContact;
    private String date;
    private String serviceType;
    private double cost;
    private String details; // Problem description or vehicle rented
    private String status;

    public Booking() {}

    public Booking(String userEmail, String vendorName, String vendorContact, String date, String serviceType, double cost, String details, String status) {
        this.userEmail = userEmail;
        this.vendorName = vendorName;
        this.vendorContact = vendorContact;
        this.date = date;
        this.serviceType = serviceType;
        this.cost = cost;
        this.details = details;
        this.status = status;
    }

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorContact() {
        return vendorContact;
    }

    public void setVendorContact(String vendorContact) {
        this.vendorContact = vendorContact;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Serializes booking to a CSV line for text storage.
     */
    public String toFileString() {
        return escape(userEmail) + "," + 
               escape(vendorName) + "," + 
               escape(vendorContact) + "," + 
               escape(date) + "," + 
               escape(serviceType) + "," + 
               cost + "," + 
               escape(details) + "," + 
               escape(status);
    }

    /**
     * Deserializes booking from a CSV line.
     */
    public static Booking fromFileString(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 8) {
            String userEmail = unescape(parts[0]);
            String vendorName = unescape(parts[1]);
            String vendorContact = unescape(parts[2]);
            String date = unescape(parts[3]);
            String serviceType = unescape(parts[4]);
            double cost = Double.parseDouble(parts[5]);
            String details = unescape(parts[6]);
            String status = unescape(parts[7]);
            return new Booking(userEmail, vendorName, vendorContact, date, serviceType, cost, details, status);
        }
        return null;
    }

    /**
     * Custom JSON formatter for basic transmission.
     */
    public String toJsonString() {
        return "{" +
                "\"userEmail\":\"" + escapeJson(userEmail) + "\"," +
                "\"vendorName\":\"" + escapeJson(vendorName) + "\"," +
                "\"vendorContact\":\"" + escapeJson(vendorContact) + "\"," +
                "\"date\":\"" + escapeJson(date) + "\"," +
                "\"serviceType\":\"" + escapeJson(serviceType) + "\"," +
                "\"cost\":" + cost + "," +
                "\"details\":\"" + escapeJson(details) + "\"," +
                "\"status\":\"" + escapeJson(status) + "\"" +
                "}";
    }

    private static String escape(String val) {
        if (val == null) return "";
        return val.replace(",", "\\comma");
    }

    private static String unescape(String val) {
        if (val == null) return "";
        return val.replace("\\comma", ",");
    }

    private String escapeJson(String val) {
        if (val == null) return "";
        return val.replace("\"", "\\\"");
    }
}
