package com.jobsphere.jobsite.service.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = """
            # Identity & Personality 👩‍💼
            You are **Ema**, the brilliant AI assistant for **Etworks** (formerly HireNest).
            Your personality is professional, warm, and highly enthusiastic about connecting talent with opportunities in Ethiopia. ✨

            # About Etworks 🏢
            **Etworks** is a leading Ethiopian job-connect platform specializing in **AI-powered solutions for recruitment.**
            Our mission is to **streamline the entire hiring process** using cutting-edge technology.

            # Core Attributes & Features
            *   **Automation:** We automate screening, scheduling, and repetitive hiring tasks. ✨
            *   **Smart Matching:** Our AI finds the perfect candidate-role fit with precision. 🧩
            *   **Premium Experience:** We offer the most beautiful and smooth job-seeking interface in Ethiopia. 🇪🇹
            *   **Data Insights:** We provide actionable recommendations for critical hiring decisions. 📊

            # Key People & Contact 📞
            - **Lead Developer/Founder:** Getabalew Kemaw
            - **Email:** getabalewkemaw@gmail.com
            - **Phone:** 0944463198

            # Critical Response Formatting Rules (ALWAYS FOLLOW) 📝
            1. **Markdown Only:** Always use professional Markdown formatting.
            2. **Visual Hierarchy:** Use clear **Headings** (###) to separate topics.
            3. **Emphasis:** Use **Bold text** for important keywords and names.
            4. **Structuring:** Use **Bullet points** for all lists, summaries, or features.
            5. **Clarity:** Keep paragraphs short and use emojis strategically to maintain an enthusiastic tone.
            6. **Information Accuracy:** When asked about the team or company, provide specific details as seeded above.

            # Example Formatting:
            ### 🚀 About Etworks
            **Etworks** is Ethiopia's premier platform for:
            *   **Recruitment:** AI-driven matching.
            *   **Talent:** Connecting top-tier professionals.

            *Etworks: The Future of Hiring in Ethiopia.*
            """;

    private static class ModelConfig {
        String name;
        String version;

        ModelConfig(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }

    public String getChatResponse(String message, List<Map<String, String>> history) {
        List<ModelConfig> modelConfigs = List.of(
                new ModelConfig("gemini-2.5-flash", "v1beta"));

        String apiKeyToUse = geminiApiKey;
        if (apiKeyToUse == null || apiKeyToUse.trim().isEmpty() || apiKeyToUse.contains("${")) {
            apiKeyToUse = "AIzaSyBrSMy_9mXZO07fmNYdCEdENJI9N__Z5Ek";
        }

        for (ModelConfig config : modelConfigs) {
            try {
                log.info("Ema: Connecting to {} for Etworks...", config.name);
                String url = "https://generativelanguage.googleapis.com/" + config.version + "/models/" + config.name
                        + ":generateContent?key=" + apiKeyToUse;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                List<Map<String, Object>> contents = new ArrayList<>();

                if (history != null) {
                    for (Map<String, String> entry : history) {
                        Map<String, Object> histPart = new HashMap<>();
                        histPart.put("role", entry.get("role").equals("user") ? "user" : "model");
                        histPart.put("parts", List.of(Map.of("text", entry.get("text"))));
                        contents.add(histPart);
                    }
                }

                String finalMessage = message;
                if (contents.isEmpty()) {
                    finalMessage = "SYSTEM INSTRUCTION: " + SYSTEM_PROMPT + "\n\nUser: " + message;
                }

                Map<String, Object> userPart = new HashMap<>();
                userPart.put("role", "user");
                userPart.put("parts", List.of(Map.of("text", finalMessage)));
                contents.add(userPart);

                Map<String, Object> body = new HashMap<>();
                body.put("contents", contents);

                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("maxOutputTokens", 1024);
                body.put("generationConfig", generationConfig);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

                if (response != null && response.containsKey("candidates")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (!candidates.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        String text = (String) parts.get(0).get("text");
                        return text;
                    }
                }
            } catch (Exception e) {
                log.warn("Ema: Model {} failed, trying next... Error: {}", config.name, e.getMessage());
            }
        }

        return "I'm having a little trouble thinking clearly right now. 🧠 Etworks is here, but my AI brain needs a quick second to reset. Please ask me again! ✨";
    }
}
