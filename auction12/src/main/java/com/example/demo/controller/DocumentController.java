package com.example.demo.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public DocumentController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files are allowed.");
        }

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            webClient.put()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return ResponseEntity.ok("PDF uploaded successfully as: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading the file: " + e.getMessage());
        }
    }
}
