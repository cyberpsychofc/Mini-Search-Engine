package com.cyberpsych.mini_search_engine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    // This controller accepts ping requests from the frontend
    
    @GetMapping("/ping")
    public ResponseEntity<String> pingServer(){
        return ResponseEntity.ok("Server is awake!");
    }
}
