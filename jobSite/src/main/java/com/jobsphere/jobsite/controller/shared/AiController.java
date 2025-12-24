package com.jobsphere.jobsite.controller.shared;

import com.jobsphere.jobsite.service.shared.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithEma(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        String response = aiService.getChatResponse(message, history);

        return ResponseEntity.ok(Map.of("response", response));
    }
}
