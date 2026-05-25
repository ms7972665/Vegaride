package com.vegaride.service;

import com.vegaride.model.Vendor;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of simulated vendors across India.
 * All vendor distances are strictly restricted to a 1.5 km radius of the user's coordinates.
 */
public class VendorService {
    private static final List<Vendor> VENDORS = new ArrayList<>();

    static {
        // ==========================================
        // --- KARNATAKA, BENGALURU NEIGHBORHOODS ---
        // ==========================================
        
        // --- Indiranagar (All strictly <= 1.5 km) ---
        VENDORS.add(new Vendor("Indiranagar Elite Rentals", "+91 98765 11101", "Karnataka", "Bangalore", 450.0, 0.6, "Rent", 4.8, "Located near Indiranagar Metro Station. Scooters, commuter bikes & sanitized helmets."));
        VENDORS.add(new Vendor("Indiranagar Superbike Hire", "+91 98765 11103", "Karnataka", "Bangalore", 1400.0, 1.2, "Rent", 4.9, "Rent superbikes (KTM RC 390, Kawasaki Ninja) in Indiranagar."));
        VENDORS.add(new Vendor("Indiranagar Electric Scooters", "+91 98765 11104", "Karnataka", "Bangalore", 299.0, 0.4, "Rent", 4.6, "Clean, green electric mopeds. Perfect for quick hops near 100 Feet Road."));
        VENDORS.add(new Vendor("Metro Bike Doctors (Indiranagar)", "+91 98765 11102", "Karnataka", "Bangalore", 200.0, 0.3, "Mechanic", 4.7, "Immediate flat tyre puncture repair, oil top-ups and spark plug checks in Indiranagar."));
        VENDORS.add(new Vendor("Indiranagar Expressway Garage", "+91 98765 11105", "Karnataka", "Bangalore", 350.0, 0.9, "Mechanic", 4.8, "Emergency towing, clutch wire repair, and complete diagnostics near Indiranagar."));

        // --- Koramangala (All strictly <= 1.5 km) ---
        VENDORS.add(new Vendor("Koramangala Cruiser Hub", "+91 98765 22201", "Karnataka", "Bangalore", 850.0, 0.8, "Rent", 4.9, "Specialized in Royal Enfield, cruisers and long-ride adventure touring in Koramangala 5th Block."));
        VENDORS.add(new Vendor("Koramangala Daily Commuters", "+91 98765 22203", "Karnataka", "Bangalore", 399.0, 0.5, "Rent", 4.5, "Affordable scooters (Honda Activa, Suzuki Access) near Koramangala Forum Mall."));
        VENDORS.add(new Vendor("Koramangala Green Wheels", "+91 98765 22204", "Karnataka", "Bangalore", 250.0, 0.3, "Rent", 4.7, "Eco-friendly commuter mopeds for quick travel inside Koramangala."));
        VENDORS.add(new Vendor("5th Block Highway Mechanics", "+91 98765 22202", "Karnataka", "Bangalore", 250.0, 0.6, "Mechanic", 4.8, "Roadside breakdown towing, engine diagnostics and minor repair in Koramangala."));
        VENDORS.add(new Vendor("Koramangala Emergency Towing", "+91 98765 22205", "Karnataka", "Bangalore", 500.0, 1.3, "Mechanic", 4.6, "Heavy-duty towing & emergency flat-bed support for all two-wheelers."));

        // --- HSR Layout (All strictly <= 1.5 km) ---
        VENDORS.add(new Vendor("HSR Eco-Electric Rentals", "+91 98765 33301", "Karnataka", "Bangalore", 350.0, 1.1, "Rent", 4.6, "Clean, green electric scooters and smart commuters in HSR Layout Sector 3."));
        VENDORS.add(new Vendor("Sector 6 Quick Garage", "+91 98765 33302", "Karnataka", "Bangalore", 180.0, 0.7, "Mechanic", 4.5, "Fastest battery jumpstart, brake adjustments and chain lubing in HSR Layout."));

        // --- Whitefield (All strictly <= 1.5 km) ---
        VENDORS.add(new Vendor("ITPL Techie Wheels", "+91 98765 44401", "Karnataka", "Bangalore", 500.0, 1.4, "Rent", 4.4, "Rent premium daily commuters. Commute hassle-free near ITPL Whitefield."));
        VENDORS.add(new Vendor("Whitefield Breakdown Squad", "+91 98765 44402", "Karnataka", "Bangalore", 400.0, 0.9, "Mechanic", 4.9, "24/7 heavy bike towing and emergency roadside assistance in Whitefield and Hope Farm."));
    }

    /**
     * Finds and filters vendors based on user location (State, City, or neighborhood locality) 
     * and service type (Rent/Mechanic).
     */
    public static List<Vendor> searchVendors(String locationQuery, String serviceType) {
        List<Vendor> results = new ArrayList<>();
        String normalizedQuery = (locationQuery == null) ? "" : locationQuery.trim().toLowerCase();

        for (Vendor v : VENDORS) {
            // Check service type match first
            if (serviceType != null && !serviceType.equalsIgnoreCase(v.getServiceType())) {
                continue;
            }

            // Check location match
            if (!normalizedQuery.isEmpty()) {
                boolean stateMatch = v.getState().toLowerCase().contains(normalizedQuery);
                boolean cityMatch = v.getCity().toLowerCase().contains(normalizedQuery);
                boolean nameMatch = v.getName().toLowerCase().contains(normalizedQuery);
                boolean descMatch = v.getDescription().toLowerCase().contains(normalizedQuery);
                
                if (!stateMatch && !cityMatch && !nameMatch && !descMatch) {
                    continue; // Skip if no match found
                }
            }

            results.add(v);
        }

        // Fallback: If no vendor matches, dynamically generate custom simulated vendors for that query!
        if (results.isEmpty() && !normalizedQuery.isEmpty()) {
            results = generateCustomVendorsForQuery(locationQuery, serviceType);
        }

        return results;
    }

    /**
     * Generates a rich selection of 5 simulated vendors in real-time, strictly within 1.5 km.
     */
    private static List<Vendor> generateCustomVendorsForQuery(String query, String serviceType) {
        List<Vendor> customList = new ArrayList<>();
        String location = capitalize(query.trim());
        
        if ("Rent".equalsIgnoreCase(serviceType)) {
            // 5 Diverse Rental Options (All strictly <= 1.5 km)
            customList.add(new Vendor(location + " Quick Rentals", "+91 99999 88811", "Search Hub", location, 399.0, 0.4, "Rent", 4.6, "Affordable daily gearless scooters (Honda Activa, TVS Jupiter) in " + location + ". Helmet included."));
            customList.add(new Vendor("Royal Cruiser Fleet (" + location + ")", "+91 99999 88822", "Search Hub", location, 850.0, 0.9, "Rent", 4.9, "Specialized classic cruiser rentals (Royal Enfield Classic 350, Himalayan) in " + location + "."));
            customList.add(new Vendor(location + " High-Octane Sports", "+91 99999 88823", "Search Hub", location, 1200.0, 1.3, "Rent", 4.8, "Rent high-performance sport models (Yamaha R15, KTM Duke 250) for highway travels."));
            customList.add(new Vendor(location + " Eco-Electric Rides", "+91 99999 88824", "Search Hub", location, 299.0, 0.3, "Rent", 4.7, "Eco-friendly, lightweight green electric mopeds perfect for local commuting inside " + location + "."));
            customList.add(new Vendor("Standard Commuter Bikes", "+91 99999 88825", "Search Hub", location, 499.0, 1.2, "Rent", 4.5, "Daily commuter geared motorcycles (Bajaj Pulsar, TVS Raider) with superb mileage."));
        } else {
            // 5 Diverse Mechanic Options (All strictly <= 1.5 km)
            customList.add(new Vendor(location + " Puncture Point", "+91 99999 88833", "Search Hub", location, 150.0, 0.2, "Mechanic", 4.7, "Superfast tubeless puncture repair, tube replacements, and air pressure adjustments in " + location + "."));
            customList.add(new Vendor(location + " 24/7 Breakdown Rescue", "+91 99999 88844", "Search Hub", location, 300.0, 0.7, "Mechanic", 4.8, "Roadside breakdown repairs, chain tightening, spark plug cleaning, and quick diagnostics."));
            customList.add(new Vendor("Bullet Specialist & Tuning", "+91 99999 88845", "Search Hub", location, 450.0, 1.1, "Mechanic", 4.9, "Expert garage specialized in classic Royal Enfield thumping, clutch plate adjustments and engine repairs."));
            customList.add(new Vendor(location + " Towing & Recovery Support", "+91 99999 88846", "Search Hub", location, 500.0, 1.5, "Mechanic", 4.6, "Emergency flat-bed towing and recovery truck service for broken down two-wheelers in " + location + "."));
            customList.add(new Vendor("City Express Auto Care", "+91 99999 88847", "Search Hub", location, 250.0, 1.0, "Mechanic", 4.5, "Scheduled service packages, general oil replacements, carburetor cleaning, and brake checks."));
        }
        return customList;
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)))
              .append(w.substring(1).toLowerCase())
              .append(" ");
        }
        return sb.toString().trim();
    }
}
