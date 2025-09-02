package org.example.Controller;

import org.example.Service.EnhancedChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {
    private final EnhancedChatService service;

    public ChatController(EnhancedChatService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        try {
            String response = service.chatWithGemini(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing chat request");
        }
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ChatController is working!");
    }
}