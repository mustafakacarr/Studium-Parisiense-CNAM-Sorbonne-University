package com.le_cnam.studiumParisiense;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonProcessor {

    /**
     * Processes a JSON file, extracts 'raw' values, and returns concatenated results.
     *
     * @param filePath the path to the JSON file
     * @return a concatenated string of 'raw' values from all objects
     * @throws IOException if an error occurs during file reading
     */
  /*  public String processJsonFile(String filePath) throws IOException {
        // Read the JSON file content as a String
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

        // Parse the file content into a JSONArray
        JSONArray jsonData = new JSONArray(fileContent);

        // Process the JSON array
        return processJsonArray(jsonData);
    }
*/
    public List<String> processJsonFile(String filePath) throws IOException {
        // Read the JSON file content as a String
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

        // Parse the file content into a JSONArray
        JSONArray jsonData = new JSONArray(fileContent);

        // Process the JSON array
        return processJsonArray(jsonData);
    }
    /**
     * Processes a JSON array, extracts 'raw' values, and returns concatenated results.
     *
     * @param jsonData the JSON array containing data objects
     * @return a concatenated string of 'raw' values from all objects
     */
    private List<String> processJsonArray(JSONArray jsonData) {
        List<String> processedData = new ArrayList<>();

        // Iterate through each object in the JSON array
        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject item = jsonData.getJSONObject(i);

            // Extract the 'raw' value from the current object (a JSONArray)
            JSONArray rawValue = item.optJSONArray("raw");

            if (rawValue != null) {
                // Add the processed raw text to the list
                processedData.add(concatenateRawArray(rawValue));
            }
        }

        return processedData;
    }

    /**
     * Concatenates the raw array elements into a single string (with newline separation).
     *
     * @param rawArray the JSON array containing raw elements
     * @return a concatenated string of raw elements
     */
    private String concatenateRawArray(JSONArray rawArray) {
        StringBuilder rawTextBuilder = new StringBuilder();

        for (int i = 0; i < rawArray.length(); i++) {
            rawTextBuilder.append(rawArray.getString(i)).append("\n");
        }

        return rawTextBuilder.toString();
    }
}
