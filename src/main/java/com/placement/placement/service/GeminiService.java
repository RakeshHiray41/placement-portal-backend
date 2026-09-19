package com.placement.placement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    // FIX: added real college/placement-cell details so Gemini fills the
    // email in completely, instead of leaving [Your Name], [Your College]
    // style placeholders for you to fill manually.
    // Set these values in application.properties (see bottom of this file for the keys).
    @Value("${college.name}")
    private String collegeName;

    @Value("${college.placement-officer-name}")
    private String officerName;

    @Value("${college.placement-officer-title}")
    private String officerTitle;

    @Value("${college.contact-email}")
    private String contactEmail;

    @Value("${college.contact-phone}")
    private String contactPhone;

    @Value("${college.portal-url}")
    private String portalUrl;

    private static final String MODEL = "gemini-2.5-flash";
    private final ObjectMapper mapper = new ObjectMapper();

    public String generatePromotionalContent(String companyName) {
        return generatePromotionalContent(companyName, null);
    }

    public String generatePromotionalContent(String companyName, String extraInstructions) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Write a complete, ready-to-send promotional email inviting ")
                .append(companyName)
                .append(" to recruit students from our college placement cell.\n\n")
                .append("Use exactly these real details -- do NOT invent, and do NOT leave any ")
                .append("placeholder text like [Your Name] or [Company] anywhere in the output:\n")
                .append("- College name: ").append(collegeName).append("\n")
                .append("- Sign the email as: ").append(officerName).append(", ").append(officerTitle).append("\n")
                .append("- Contact email: ").append(contactEmail).append("\n")
                .append("- Contact phone: ").append(contactPhone).append("\n")
                .append("- Placement portal link: ").append(portalUrl).append("\n\n")
                .append("The email must be fully finished and ready to send as-is, with no brackets, ")
                .append("no blanks, and no instructions left in it.");

        if (extraInstructions != null && !extraInstructions.isBlank()) {
            prompt.append("\n\nAdditional instructions from the sender: ").append(extraInstructions);
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt.toString())))
                    )
            );
            String json = mapper.writeValueAsString(requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://generativelanguage.googleapis.com/v1beta/models/" +
                                    MODEL + ":generateContent?key=" + apiKey
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            if (response.statusCode() != 200) {
                String message = root.path("error").path("message").asText("Unknown error from Gemini API.");
                return "Gemini API error (" + response.statusCode() + "): " + message;
            }

            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                return "Gemini returned no content. Please try again.";
            }

            return textNode.asText();

        } catch (Exception e) {
            return "Gemini Error: " + e.getMessage();
        }
    }
}