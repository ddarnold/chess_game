package main;

import java.io.*;
import java.util.*;

public class JsonHandler {
    private static final String PREFERENCES_PATH = "preferences.json";

    static String readJson(String key) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PREFERENCES_PATH))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonString = jsonBuilder.toString();
            Map<String, String> jsonObject = parseJson(jsonString);
            return jsonObject.getOrDefault(key, null);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the JSON file", e);
        }
    }

    static void writeJson(String key, String value) {
        try {
            // Read and parse existing JSON
            Map<String, String> jsonObject = new HashMap<>();
            File file = new File(PREFERENCES_PATH);

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    jsonObject = parseJson(jsonBuilder.toString());
                }
            }

            // Update the value
            jsonObject.put(key, value);

            // Write updated JSON back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(PREFERENCES_PATH))) {
                writer.write(serializeJson(jsonObject));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating the JSON file", e);
        }
    }

    private static Map<String, String> parseJson(String jsonString) {
        Map<String, String> jsonObject = new HashMap<>();
        jsonString = jsonString.trim();

        if (!jsonString.startsWith("{") || !jsonString.endsWith("}")) {
            throw new RuntimeException("Invalid JSON format");
        }

        jsonString = jsonString.substring(1, jsonString.length() - 1).trim(); // Remove curly braces
        if (jsonString.isEmpty()) return jsonObject;

        String[] pairs = jsonString.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by ',' outside quotes
        for (String pair : pairs) {
            String[] keyValue = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by ':' outside quotes
            if (keyValue.length != 2) {
                throw new RuntimeException("Invalid JSON key-value pair: " + pair);
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            // Remove quotes from keys and values
            key = key.replaceAll("^\"|\"$", "");
            value = value.replaceAll("^\"|\"$", "");

            jsonObject.put(key, value);
        }

        return jsonObject;
    }

    private static String serializeJson(Map<String, String> jsonObject) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        boolean first = true;
        for (Map.Entry<String, String> entry : jsonObject.entrySet()) {
            if (!first) {
                jsonBuilder.append(",");
            }
            first = false;

            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}
