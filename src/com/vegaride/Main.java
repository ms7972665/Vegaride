package com.vegaride;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.vegaride.model.Booking;
import com.vegaride.model.User;
import com.vegaride.model.Vendor;
import com.vegaride.service.StorageService;
import com.vegaride.service.VendorService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";

    public static void main(String[] args) {
        try {
            // Instantiate the StorageService early to create base flat files
            StorageService.getInstance();

            // Create HttpServer using core java.net libraries
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Set up a route handler for all requests
            server.createContext("/", new RequestRouter());
            
            // Use multi-threaded executor for handling parallel client bookings
            server.setExecutor(Executors.newFixedThreadPool(10));
            
            server.start();
            System.out.println("=================================================");
            System.out.println("   VEGARIDE STANDALONE JAVA SERVER STARTED      ");
            System.out.println("   Open your browser: http://localhost:" + PORT  );
            System.out.println("   Files database: users.txt & bookings.txt     ");
            System.out.println("=================================================");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    /**
     * Unified router that processes static assets and coordinates backend commands.
     */
    static class RequestRouter implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            // Direct route logging
            System.out.println("[" + method + "] " + path);

            // API Route: Registration
            if ("/api/register".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleRegister(exchange);
                return;
            }

            // API Route: Login
            if ("/api/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
                return;
            }

            // API Route: Vendor Search
            if ("/api/vendors".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetVendors(exchange);
                return;
            }

            // API Route: Book Bike / Mechanic / Emergency
            if ("/api/book".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleBook(exchange);
                return;
            }

            // API Route: Booking History
            if ("/api/history".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetHistory(exchange);
                return;
            }

            // API Route: Update profile vehicle
            if ("/api/update-profile".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleUpdateProfile(exchange);
                return;
            }

            // Serve Static UI Assets
            handleStaticFile(exchange, path);
        }

        /**
         * Parses and serves static web assets (HTML, CSS, JS, Images).
         */
        private void handleStaticFile(HttpExchange exchange, String path) throws IOException {
            // Default index route
            if (path.endsWith("/")) {
                path += "index.html";
            }

            // Sanitize path for Windows filesystem to prevent directory traversal
            String relativePath = path.replace("/", File.separator);
            if (relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(1);
            }

            File file = new File(WEB_DIR, relativePath);
            if (!file.exists() || file.isDirectory()) {
                sendErrorResponse(exchange, 404, "Page Not Found");
                return;
            }

            // Detect matching content-type
            String contentType = "text/plain";
            String name = file.getName().toLowerCase();
            if (name.endsWith(".html") || name.endsWith(".htm")) {
                contentType = "text/html; charset=utf-8";
            } else if (name.endsWith(".css")) {
                contentType = "text/css; charset=utf-8";
            } else if (name.endsWith(".js")) {
                contentType = "application/javascript; charset=utf-8";
            } else if (name.endsWith(".png")) {
                contentType = "image/png";
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (name.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        /**
         * Registers a new user.
         */
        private void handleRegister(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonBody(body);

            String name = params.get("name");
            String email = params.get("email");
            String phone = params.get("phone");
            String password = params.get("password");

            if (name == null || email == null || phone == null || password == null ||
                name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Please fill all fields.\"}");
                return;
            }

            User user = new User(name, email, phone, password);
            boolean success = StorageService.getInstance().registerUser(user);

            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account registered successfully!\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"This email is already in use.\"}");
            }
        }

        /**
         * Validates login credentials and returns profile information.
         */
        private void handleLogin(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonBody(body);

            String email = params.get("email");
            String password = params.get("password");

            if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Email and password are required.\"}");
                return;
            }

            User user = StorageService.getInstance().getUser(email);
            if (user != null && password.equals(user.getPassword())) {
                String response = "{" +
                        "\"success\":true," +
                        "\"message\":\"Log in successful!\"," +
                        "\"user\":{" +
                        "\"name\":\"" + escapeJson(user.getName()) + "\"," +
                        "\"email\":\"" + escapeJson(user.getEmail()) + "\"," +
                        "\"phone\":\"" + escapeJson(user.getPhone()) + "\"," +
                        "\"vehicle\":\"" + escapeJson(user.getVehicle()) + "\"" +
                        "}" +
                        "}";
                sendJsonResponse(exchange, 200, response);
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Incorrect email or password.\"}");
            }
        }

        /**
         * Fetches vendors matching city/state manual lookup or automatic locator.
         */
        private void handleGetVendors(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String location = queryParams.getOrDefault("location", "");
            String serviceType = queryParams.getOrDefault("serviceType", "Rent");

            List<Vendor> matching = VendorService.searchVendors(location, serviceType);

            // Construct JSON array manual formatted
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < matching.size(); i++) {
                sb.append(matching.get(i).toJsonString());
                if (i < matching.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            sendJsonResponse(exchange, 200, sb.toString());
        }

        /**
         * Submits a booking (pre-booking or quick mechanic support).
         */
        private void handleBook(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonBody(body);

            String email = params.get("email");
            String vendorName = params.get("vendorName");
            String vendorContact = params.get("vendorContact");
            String date = params.get("date");
            String serviceType = params.get("serviceType");
            String costStr = params.get("cost");
            String details = params.get("details");

            if (email == null || vendorName == null || serviceType == null || date == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing core booking details.\"}");
                return;
            }

            double cost = 0;
            try {
                if (costStr != null) cost = Double.parseDouble(costStr);
            } catch (NumberFormatException ignored) {}

            Booking booking = new Booking(email, vendorName, vendorContact, date, serviceType, cost, details, "Confirmed");
            boolean success = StorageService.getInstance().addBooking(booking);

            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Your booking has been registered!\"}");
            } else {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Failed to save booking. Please try again.\"}");
            }
        }

        /**
         * Loads booking history logs for user profile dashboard.
         */
        private void handleGetHistory(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String email = queryParams.get("email");

            if (email == null || email.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account identifier is missing.\"}");
                return;
            }

            List<Booking> bookings = StorageService.getInstance().getBookingsByUser(email);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < bookings.size(); i++) {
                sb.append(bookings.get(i).toJsonString());
                if (i < bookings.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");

            sendJsonResponse(exchange, 200, sb.toString());
        }

        /**
         * Updates customer's registered vehicle description in profile.
         */
        private void handleUpdateProfile(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonBody(body);

            String email = params.get("email");
            String vehicle = params.get("vehicle");

            if (email == null || vehicle == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required profile fields.\"}");
                return;
            }

            boolean success = StorageService.getInstance().updateUserVehicle(email, vehicle);
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Vehicle profile updated successfully!\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update profile.\"}");
            }
        }

        // --- HTTP Helper utilities ---

        private String readRequestBody(HttpExchange exchange) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] bytes = ("<h1>" + statusCode + " - " + message + "</h1>").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        /**
         * A simple JSON parser using manual string scanning.
         * Extracts key-value mappings for a single flat JSON object.
         */
        private Map<String, String> parseJsonBody(String body) {
            Map<String, String> map = new HashMap<>();
            if (body == null || body.trim().isEmpty()) return map;

            body = body.trim();
            if (body.startsWith("{")) body = body.substring(1);
            if (body.endsWith("}")) body = body.substring(0, body.length() - 1);

            // Simple parser: scanning for "key": "value" or "key": value
            boolean inString = false;
            StringBuilder currentKey = new StringBuilder();
            StringBuilder currentValue = new StringBuilder();
            boolean readingValue = false;
            char stringChar = 0;

            for (int i = 0; i < body.length(); i++) {
                char c = body.charAt(i);

                if (inString) {
                    if (c == '\\') { // Escape char
                        if (i + 1 < body.length()) {
                            currentValue.append(body.charAt(i + 1));
                            i++;
                        }
                    } else if (c == stringChar) {
                        inString = false;
                    } else {
                        if (readingValue) {
                            currentValue.append(c);
                        } else {
                            currentKey.append(c);
                        }
                    }
                } else {
                    if (c == '"' || c == '\'') {
                        inString = true;
                        stringChar = c;
                    } else if (c == ':') {
                        readingValue = true;
                    } else if (c == ',' || c == '\n') {
                        savePair(map, currentKey, currentValue);
                        currentKey.setLength(0);
                        currentValue.setLength(0);
                        readingValue = false;
                    } else if (!Character.isWhitespace(c)) {
                        if (readingValue) {
                            currentValue.append(c);
                        } else {
                            currentKey.append(c);
                        }
                    }
                }
            }
            savePair(map, currentKey, currentValue);
            return map;
        }

        private void savePair(Map<String, String> map, StringBuilder key, StringBuilder val) {
            String k = key.toString().trim();
            String v = val.toString().trim();
            if (!k.isEmpty()) {
                // If it was parsed as string, outer quotes are already stripped. If not, strip them if present.
                if (k.startsWith("\"") && k.endsWith("\"")) k = k.substring(1, k.length() - 1);
                if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
                map.put(k, v);
            }
        }

        /**
         * Query Parameter URL decoder.
         */
        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> map = new HashMap<>();
            if (query == null || query.isEmpty()) return map;

            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                        map.put(key, value);
                    } else {
                        String key = URLDecoder.decode(pair, StandardCharsets.UTF_8.name());
                        map.put(key, "");
                    }
                } catch (UnsupportedEncodingException ignored) {}
            }
            return map;
        }

        private String escapeJson(String val) {
            if (val == null) return "";
            return val.replace("\"", "\\\"");
        }
    }
}
