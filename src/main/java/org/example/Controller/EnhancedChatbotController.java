package org.example.Controller;

import org.example.Service.EnhancedChatbotService;
import org.example.Service.EnhancedChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class EnhancedChatbotController {

    @Autowired
    private EnhancedChatbotService enhancedChatbotService;

    @Autowired
    private EnhancedChatService enhancedChatService;

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> processMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String mode = request.getOrDefault("mode", "structured"); // "structured" or "ai"

        if (message == null || message.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "Please provide a message.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String response;

        // Choose processing mode
        if ("ai".equalsIgnoreCase(mode)) {
            // Use AI-powered service with Gemini
            response = enhancedChatService.chatWithGemini(message);
        } else {
            // Use structured rule-based service
            response = enhancedChatbotService.processMessage(message);
        }

        Map<String, String> chatResponse = new HashMap<>();
        chatResponse.put("response", response);
        chatResponse.put("mode", mode);
        chatResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(chatResponse);
    }

    @PostMapping("/ai")
    public ResponseEntity<Map<String, String>> processWithAI(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "Please provide a message for AI processing.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String response = enhancedChatService.chatWithGemini(message);

        Map<String, String> chatResponse = new HashMap<>();
        chatResponse.put("response", response);
        chatResponse.put("mode", "ai");
        chatResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(chatResponse);
    }

    @PostMapping("/structured")
    public ResponseEntity<Map<String, String>> processStructured(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        if (message == null || message.trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "Please provide a message for structured processing.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String response = enhancedChatbotService.processMessage(message);

        Map<String, String> chatResponse = new HashMap<>();
        chatResponse.put("response", response);
        chatResponse.put("mode", "structured");
        chatResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(chatResponse);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> helpResponse = new HashMap<>();

        // Enhanced command categories
        Map<String, String[]> commandCategories = new HashMap<>();

        commandCategories.put("Product Management", new String[]{
                "CREATE: 'Create product with title [name] and description [desc]'",
                "READ: 'Show all products', 'Find product [name]', 'Get product [id]'",
                "UPDATE: 'Update product [id] with title [new title]'",
                "DELETE: 'Delete product [id]' or 'Delete product [title]'"
        });

        commandCategories.put("Inventory & Stock", new String[]{
                "STOCK: 'Check stock for [product]' or 'Stock for SKU [code]'",
                "LOW STOCK: 'Show low stock items' or 'Low stock alert'",
                "OVERVIEW: 'Inventory overview' or 'Stock status'"
        });

        commandCategories.put("Pricing", new String[]{
                "PRICE INFO: 'Show price for [product]' or 'Price of [product]'",
                "PRICE RANGE: 'Products between [min] and [max]'",
                "ANALYSIS: 'Price analysis for [product]'"
        });

        commandCategories.put("Categories", new String[]{
                "LIST: 'Show categories' or 'List all categories'",
                "SEARCH: 'Category [name]' or 'Find category [name]'",
                "HIERARCHY: 'Main categories' or 'Root categories'"
        });

        commandCategories.put("Comprehensive Queries", new String[]{
                "COMPLETE INFO: 'Complete info for [product]' or 'Full details of [product]'",
                "SYSTEM STATUS: 'Status' or 'System overview'"
        });

        helpResponse.put("commandCategories", commandCategories);

        // Processing modes
        Map<String, String> modes = new HashMap<>();
        modes.put("structured", "Rule-based processing for specific commands (faster, more predictable)");
        modes.put("ai", "AI-powered processing with natural language understanding (more flexible)");
        helpResponse.put("processingModes", modes);

        // Examples for different modes
        helpResponse.put("structuredExamples", new String[]{
                "Create product with title 'Premium T-Shirt' and description 'High quality cotton t-shirt'",
                "Check stock for Premium T-Shirt",
                "Show price for Premium T-Shirt",
                "Low stock alert",
                "Complete info for Premium T-Shirt"
        });

        helpResponse.put("aiExamples", new String[]{
                "What products do we have that are running low on stock?",
                "Show me everything about our most expensive products",
                "Which categories have the most products?",
                "What's the price range for t-shirts in our inventory?",
                "I need a summary of our inventory status"
        });

        // API endpoints
        helpResponse.put("endpoints", new String[]{
                "POST /api/chatbot/message - General processing (specify mode: 'structured' or 'ai')",
                "POST /api/chatbot/structured - Force structured processing",
                "POST /api/chatbot/ai - Force AI processing",
                "GET /api/chatbot/help - This help message",
                "GET /api/chatbot/status - System status"
        });

        return ResponseEntity.ok(helpResponse);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            // Get system statistics using the enhanced chatbot service
            String statusMessage = enhancedChatbotService.processMessage("status");

            Map<String, Object> statusResponse = new HashMap<>();
            statusResponse.put("status", "operational");
            statusResponse.put("details", statusMessage);
            statusResponse.put("availableModes", new String[]{"structured", "ai"});
            statusResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

            return ResponseEntity.ok(statusResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error retrieving system status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();

        // Data sources
        capabilities.put("dataSources", new String[]{
                "Products with variants",
                "Inventory items and stock levels",
                "Product categories and hierarchy",
                "Price lists and pricing information"
        });

        // Query types
        capabilities.put("queryTypes", new String[]{
                "Product CRUD operations",
                "Stock and inventory checks",
                "Price analysis and comparisons",
                "Category browsing and search",
                "Low stock alerts",
                "Comprehensive product information",
                "System status and statistics"
        });

        // Processing features
        capabilities.put("processingFeatures", new String[]{
                "Natural language understanding",
                "Structured command processing",
                "Multi-table data integration",
                "Real-time inventory tracking",
                "Dynamic pricing support",
                "Category hierarchy navigation"
        });

        return ResponseEntity.ok(capabilities);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> processBulkQueries(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) request.get("messages");
        String mode = (String) request.getOrDefault("mode", "structured");

        if (messages == null || messages.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Please provide a list of messages.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> bulkResponse = new HashMap<>();
        Map<String, String> responses = new HashMap<>();

        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            String response;

            try {
                if ("ai".equalsIgnoreCase(mode)) {
                    response = enhancedChatService.chatWithGemini(message);
                } else {
                    response = enhancedChatbotService.processMessage(message);
                }
                responses.put("query_" + (i + 1), response);
            } catch (Exception e) {
                responses.put("query_" + (i + 1), "Error processing query: " + e.getMessage());
            }
        }

        bulkResponse.put("responses", responses);
        bulkResponse.put("mode", mode);
        bulkResponse.put("processedCount", messages.size());
        bulkResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(bulkResponse);
    }
}