package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/")
    public String home()
    {
        return "Spring Boot Application is running!";
    }

    @GetMapping("/debug")
    public String debug()
    {
        return "Debug endpoint is working!";
    }

    @GetMapping("/api/debug")
    public String apiDebug()
    {
        return "API Debug endpoint is working!";
    }
}