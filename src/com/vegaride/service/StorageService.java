package com.vegaride.service;

import com.vegaride.model.User;
import com.vegaride.model.Booking;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles thread-safe local text-file database persistence for Users and Bookings.
 * Adheres to pure Java principles with no external libraries.
 */
public class StorageService {
    private static final String USERS_FILE = "users.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static StorageService instance;

    private StorageService() {
        // Create files if they do not exist
        try {
            File uf = new File(USERS_FILE);
            if (!uf.exists()) uf.createNewFile();

            File bf = new File(BOOKINGS_FILE);
            if (!bf.exists()) bf.createNewFile();
        } catch (IOException e) {
            System.err.println("Error initializing storage files: " + e.getMessage());
        }
    }

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    /**
     * Registers a new user. Returns false if email is already taken.
     */
    public synchronized boolean registerUser(User user) {
        if (getUser(user.getEmail()) != null) {
            return false; // Email already registered
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(USERS_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(user.toFileString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user by their email address.
     */
    public synchronized User getUser(String email) {
        if (email == null || email.trim().isEmpty()) return null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(USERS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User u = User.fromFileString(line);
                if (u != null && email.equalsIgnoreCase(u.getEmail())) {
                    return u;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read users: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the user's vehicle profile.
     */
    public synchronized boolean updateUserVehicle(String email, String vehicle) {
        User target = getUser(email);
        if (target == null) return false;

        target.setVehicle(vehicle);
        List<User> allUsers = new ArrayList<>();

        // Read all users
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(USERS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User u = User.fromFileString(line);
                if (u != null) {
                    if (email.equalsIgnoreCase(u.getEmail())) {
                        allUsers.add(target);
                    } else {
                        allUsers.add(u);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read users during update: " + e.getMessage());
            return false;
        }

        // Rewrite entire file
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(USERS_FILE, false), StandardCharsets.UTF_8))) {
            for (User u : allUsers) {
                writer.write(u.toFileString());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to rewrite users file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves a booking to the log file.
     */
    public synchronized boolean addBooking(Booking booking) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(BOOKINGS_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(booking.toFileString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write booking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all bookings associated with a specific user email.
     */
    public synchronized List<Booking> getBookingsByUser(String email) {
        List<Booking> userBookings = new ArrayList<>();
        if (email == null) return userBookings;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(BOOKINGS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Booking b = Booking.fromFileString(line);
                if (b != null && email.equalsIgnoreCase(b.getUserEmail())) {
                    userBookings.add(b);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read bookings: " + e.getMessage());
        }
        return userBookings;
    }
}
