package com.outsera.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.service.ProducerService;

@RestController
@RequestMapping("/api/v1/producers")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }
    
    @GetMapping("/award-intervals")
    public ResponseEntity<AwardIntervalsResponseDTO> getAwardIntervals() {
        return ResponseEntity.ok(producerService.getAwardIntervals());
    }

}
