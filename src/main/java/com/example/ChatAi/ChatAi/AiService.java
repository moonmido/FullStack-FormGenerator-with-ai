package com.example.ChatAi.ChatAi;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AiService {

@Value("${gemini.api.key}")
private String apikey;

private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");

    public String ReplyUserQst(String userQuery) {
        String prompt = "you are a chatbot pro but act as emotional normal human, reply to this: ";

        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + userQuery + "\" }] }] }";

        String response = webClient.post()
                .uri("?key=" + apikey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null || response.isEmpty()) {
            return "Sorry, I couldn't process your request.";
        }

        JSONObject jsonObject = new JSONObject(response);
        if (!jsonObject.has("candidates")) {
            return "Error: Invalid response from AI.";
        }

        JSONArray candidates = jsonObject.getJSONArray("candidates");
        if (candidates.isEmpty()) {
            return "No response from AI.";
        }

        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");

        return parts.getJSONObject(0).getString("text");
    }

    public JSONObject generateForm(String userPrompt) {
        // Instruction for Gemini API
        String systemPrompt = "Generate a JSON form structure. The structure should include relevant fields while maintaining a logical and user-friendly format. " +
                "each form has a forme title , description .put all fields in fields array and for Each field should have: label, name, type, required, and options if applicable. Ensure it's in valid JSON format based on the title: ";

        // Construct request payload
        String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + systemPrompt + userPrompt + "\" }] }] }";

        // Call Gemini API
        String response = webClient.post()
                .uri("?key=" + apikey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("{\"error\": \"Failed to generate form.\"}"))
                .block();

        // Parse and validate the response
        if (response == null || response.isEmpty()) {
            return new JSONObject().put("error", "No response from AI.");
        }

        try {
            // Try to parse the response as a JSON object
            JSONObject jsonObject = new JSONObject(response);

            // Check if the response contains "candidates"
            if (!jsonObject.has("candidates")) {
                return new JSONObject().put("error", "Invalid response from AI: No candidates found.");
            }

            JSONArray candidates = jsonObject.getJSONArray("candidates");
            if (candidates.isEmpty()) {
                return new JSONObject().put("error", "No candidates found in AI response.");
            }

            // Extract the content
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");

            // Extract the generated form JSON
            String formText = parts.getJSONObject(0).getString("text");

            // Try to parse the form text as JSON
            try {
                return new JSONObject(formText);
            } catch (Exception e) {
                // If parsing fails, search for a JSON-like structure in the text
                String jsonString = extractJsonFromText(formText);
                if (jsonString != null) {
                    return new JSONObject(jsonString);
                } else {
                    return new JSONObject().put("error", "Failed to parse AI response: No valid JSON found.");
                }
            }
        } catch (Exception e) {
            return new JSONObject().put("error", "Failed to parse AI response: " + e.getMessage());
        }
    }

    // Helper method to extract JSON from plain text
    private String extractJsonFromText(String text) {
        int startIndex = text.indexOf("{");
        int endIndex = text.lastIndexOf("}");
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1);
        }
        return null;
    }

}
