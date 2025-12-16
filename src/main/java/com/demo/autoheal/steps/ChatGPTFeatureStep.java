package com.demo.autoheal.steps;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import okhttp3.*;

import java.util.*;
import java.util.stream.Collectors;

public class ChatGPTFeatureStep {

    private static final String API_KEY =
           "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"; 

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/responses";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * FEATURE-STYLE entry point
     */
    public static List<String> getCandidateXPaths(
            String failedXpath,
            String parentElementHtml,
            int limit) {

        System.out.println("ChatGPT feature step invoked");

        try {
            String prompt = buildFeaturePrompt(
                    failedXpath, parentElementHtml);

            String payload = buildRequest(prompt);

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(RequestBody.create(
                            payload,
                            MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = CLIENT.newCall(request).execute();
            String body = response.body().string();

            return parseAndLimit(body, limit);

        } catch (Exception e) {
            throw new RuntimeException(
                    "ChatGPT feature autoheal failed", e);
        }
    }

    private static String buildFeaturePrompt(
            String failedXpath,
            String parentElement) {
    	
    	System.out.println("ParentElement: "+ parentElement);
        return "The following Selenium test failed due to an incorrect XPath:\n"
                + "Failing XPath: " + failedXpath + "\n\n"
                + "HTML snippet:\n" + parentElement + "\n\n"
                + "RULES:\n"
                + "- Provide ONLY alternative XPaths\n"
                + "- Do NOT add numbering or explanations\n"
                + "- Each XPath must be on a new line\n"
                + "- Do NOT include the failing XPath\n"
                + "- ONLY include valid, working XPaths\n\n"
                + "OUTPUT FORMAT:\n"
                + "//xpath1\n//xpath2\n//xpath3";
    }

    private static String buildRequest(String prompt)
            throws Exception {

        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", "gpt-4.1-mini");
        root.put("temperature", 0);

        ArrayNode input = root.putArray("input");
        ObjectNode msg = input.addObject();
        msg.put("role", "user");
        msg.put("content", prompt);

        return MAPPER.writeValueAsString(root);
    }

    private static List<String> parseAndLimit(
            String json,
            int limit) throws Exception {

        JsonNode root = MAPPER.readTree(json);
        String text = "";

        // Preferred field
        if (root.has("output_text")) {
            text = root.get("output_text").asText();
        }
        // Fallback for Responses API
        else if (root.has("output")
                && root.get("output").isArray()
                && root.get("output").size() > 0) {

            JsonNode content =
                    root.get("output").get(0).get("content");

            if (content != null && content.isArray()) {
                for (JsonNode c : content) {
                    if (c.has("text")) {
                        text = c.get("text").asText();
                        break;
                    }
                }
            }
        }

        if (text.isEmpty()) {
            throw new RuntimeException(
                    "Empty response from ChatGPT");
        }

        List<String> xpaths =
                Arrays.stream(text.split("\\R"))
                        .map(String::trim)
                        .filter(x -> x.startsWith("//"))
                        .distinct()
                        .limit(limit)
                        .collect(Collectors.toList());

        System.out.println("Candidate XPath count: " + xpaths.size());
        System.out.println("Candidate XPaths: " + xpaths + "\n");
        return xpaths;
    }
}
